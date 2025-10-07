package com.example.DATN.controller;

import com.example.DATN.dao.HostRequestRepository;
import com.example.DATN.dao.RoleRepository;
import com.example.DATN.dao.UserRepository;
import com.example.DATN.dto.user.RegisterRequest;
import com.example.DATN.entity.HostRequest;
import com.example.DATN.entity.Role;
import com.example.DATN.entity.User;
import com.example.DATN.enums.HostRequestStatus;
import com.example.DATN.security.JwtResponse;
import com.example.DATN.security.LoginRequest;
import com.example.DATN.service.JWT.JwtService;
import com.example.DATN.service.email.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final HostRequestRepository hostRequestRepo;
    private final MailService mailService;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String feBaseUrl;

    @Value("${app.admin.emails:}")
    private String adminEmailsCsv;

    /* ======================= HELPERS DÙNG CHUNG ======================= */

    private static String str(Object v) { return v == null ? null : v.toString(); }
    private static String orEmpty(String s) { return s == null ? "" : s; }
    private static Boolean toBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    /** Áp dụng các trường profile cơ bản (firstName, lastName, phoneNumber, avatar) */
    private void applyBasicProfile(User u, Map<String, ?> body) {
        if (body.containsKey("firstName"))   u.setFirstName(str(body.get("firstName")));
        if (body.containsKey("lastName"))    u.setLastName(str(body.get("lastName")));
        if (body.containsKey("phoneNumber")) u.setPhoneNumber(str(body.get("phoneNumber")));
        if (body.containsKey("avatar"))      u.setAvatar(str(body.get("avatar")));
    }

    /** Convert User -> Map JSON (an toàn null, giữ thứ tự field) */
    private Map<String, Object> toUserJson(User u) {
        var roles = (u.getListRoles() == null ? Collections.<Role>emptyList() : u.getListRoles())
                .stream().map(Role::getNameRole).filter(Objects::nonNull).toList();

        var m = new LinkedHashMap<String, Object>();
        m.put("id", u.getIdUser());
        m.put("username", orEmpty(u.getUsername()));
        m.put("firstName", orEmpty(u.getFirstName()));
        m.put("lastName", orEmpty(u.getLastName()));
        m.put("email", orEmpty(u.getEmail()));
        m.put("phoneNumber", orEmpty(u.getPhoneNumber()));
        m.put("avatar", orEmpty(u.getAvatar()));
        m.put("enabled", u.isEnabled());
        m.put("isHost", u.isHost());
        m.put("roles", roles);
        return m;
    }

    private static String url(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    /* ============================ USERS (ADMIN) ============================ */

    /** Lấy tất cả tài khoản (ADMIN) */
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepo.findAll();
        List<Map<String, Object>> result = users.stream().map(this::toUserJson).toList();
        return ResponseEntity.ok(result);
    }

    /** Cập nhật một phần user (ADMIN) */
    @PatchMapping("/{id}")
    public ResponseEntity<?> adminUpdateUser(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body
    ) {
        var uOpt = userRepo.findById(id);
        if (uOpt.isEmpty()) return ResponseEntity.status(404).body("Không tìm thấy người dùng");
        var u = uOpt.get();

        // Dùng lại phần profile cơ bản
        applyBasicProfile(u, body);

        // email
        if (body.containsKey("email")) {
            String newEmail = str(body.get("email"));
            if (newEmail != null && !newEmail.equals(u.getEmail())
                    && userRepo.findByEmail(newEmail).isPresent()) {
                return ResponseEntity.badRequest().body("Email đã tồn tại");
            }
            if (newEmail != null && !newEmail.isBlank()) u.setEmail(newEmail);
        }

        // username
        if (body.containsKey("username")) {
            String newUsername = str(body.get("username"));
            if (newUsername != null && !newUsername.equals(u.getUsername())
                    && userRepo.findByUsername(newUsername).isPresent()) {
                return ResponseEntity.badRequest().body("Username đã tồn tại");
            }
            if (newUsername != null && !newUsername.isBlank()) u.setUsername(newUsername);
        }

        // enabled
        if (body.containsKey("enabled")) {
            Boolean val = toBool(body.get("enabled"));
            if (val != null) u.setEnabled(val);
        }

        // isHost
        if (body.containsKey("isHost")) {
            Boolean val = toBool(body.get("isHost"));
            if (val != null) u.setHost(val);
        }

        // roles (nhận chuỗi đơn "ADMIN" hoặc mảng ["ADMIN","HOST"])
        if (body.containsKey("roles")) {
            Object rv = body.get("roles");
            List<String> names;
            if (rv instanceof List<?> list) {
                names = list.stream().map(Object::toString).toList();
            } else {
                names = List.of(rv.toString());
            }

            // map tên -> Role entity (bỏ null), tạo list MUTABLE
            ArrayList<Role> newRoles = names.stream()
                    .map(roleRepo::findByNameRole)
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

            if (newRoles.isEmpty()) {
                return ResponseEntity.badRequest().body("Danh sách roles không hợp lệ");
            }

            // Cập nhật IN-PLACE để tránh collection bất biến
            if (u.getListRoles() == null) {
                u.setListRoles(new ArrayList<>());
            } else {
                u.getListRoles().clear();
            }
            u.getListRoles().addAll(newRoles);
        }

        userRepo.save(u);
        return ResponseEntity.ok(toUserJson(u));
    }

    /** Bật/tắt tài khoản (ADMIN) */
    @PatchMapping("/{id}/toggle-enabled")
    public ResponseEntity<?> toggleEnabled(
            @PathVariable Integer id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User me
    ) {
        var opt = userRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Không tìm thấy người dùng");
        var u = opt.get();

        // khuyến nghị: chặn tự đổi trạng thái chính mình
        if (me != null && me.getUsername().equals(u.getUsername())) {
            return ResponseEntity.badRequest().body("Không thể tự thay đổi trạng thái tài khoản của chính mình");
        }

        boolean newVal = !u.isEnabled();
        u.setEnabled(newVal);
        userRepo.save(u);

        return ResponseEntity.ok(Map.of(
                "id", u.getIdUser(),
                "enabled", u.isEnabled(),
                "message", newVal ? "Đã bật tài khoản" : "Đã vô hiệu hoá tài khoản"
        ));
    }

    /* ============================== AUTH ============================== */

    /** Đăng ký tài khoản (có thể kèm applyHost=true) */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username đã tồn tại");
        }
        if (userRepo.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        Role customer = roleRepo.findByNameRole("CUSTOMER");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ROLE CUSTOMER chưa được khởi tạo");
        }

        User u = new User();
        u.setUsername(req.username());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setEmail(req.email());
        u.setFirstName(req.firstName());
        u.setAvatar("");

        // Tuỳ bạn chọn flow kích hoạt email:
        u.setEnabled(true); // nếu không cần kích hoạt email
        // u.setEnabled(false); // nếu cần kích hoạt email
        u.setActivationCode(java.util.UUID.randomUUID().toString());

        u.setHost(false);

        // GÁN ROLES BẰNG LIST MUTABLE để tránh lỗi Hibernate
        ArrayList<Role> initRoles = new ArrayList<>();
        initRoles.add(customer);
        u.setListRoles(initRoles);

        userRepo.save(u);

        // Email kích hoạt (nếu dùng flow kích hoạt)
        String activeLink = feBaseUrl + "/activate?email=" + url(u.getEmail()) + "&code=" + url(u.getActivationCode());
        mailService.sendHtml(
                u.getEmail(),
                "[Thuê Trọ] Kích hoạt tài khoản",
                """
                <p>Chào %s,</p>
                <p>Cảm ơn bạn đã đăng ký. Nhấn vào liên kết sau để kích hoạt tài khoản:</p>
                <p><a href="%s">%s</a></p>
                <p>Trân trọng,</p><p>Thuê Trọ Team</p>
                """.formatted(u.getUsername(), activeLink, activeLink)
        );

        // Nếu apply host → tạo request & gửi mail
        if (req.applyHost()) {
            HostRequest hr = new HostRequest();
            hr.setUser(u);
            hr.setStatus(HostRequestStatus.PENDING);
            hostRequestRepo.save(hr);

            List<String> admins = com.example.DATN.service.email.MailServiceImpl.parseEmails(adminEmailsCsv);
            if (!admins.isEmpty()) {
                String link = feBaseUrl + "/admin/host-requests/" + hr.getId();
                mailService.sendHtml(
                        admins,
                        "[Thuê Trọ] Yêu cầu duyệt chủ trọ: " + u.getUsername(),
                        """
                        <p>Có yêu cầu làm chủ trọ mới:</p>
                        <ul>
                          <li>User: %s (%s)</li>
                          <li>Request ID: %d</li>
                        </ul>
                        <p>Mở trang quản trị: <a href="%s">%s</a></p>
                        """.formatted(u.getUsername(), u.getEmail(), hr.getId(), link, link)
                );
            }
            mailService.sendHtml(
                    u.getEmail(),
                    "[Thuê Trọ] Đã nhận yêu cầu làm chủ trọ",
                    """
                    <p>Chào %s,</p>
                    <p>Hệ thống đã nhận yêu cầu làm chủ trọ. Vui lòng chờ Admin xét duyệt.</p>
                    """.formatted(u.getUsername())
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(req.applyHost()
                        ? "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản. Yêu cầu làm chủ trọ đã được gửi."
                        : "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
    }

    /** Kích hoạt tài khoản: GET /user/activate?email=...&code=... */
    @GetMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String email, @RequestParam String code) {
        var u = userRepo.findByEmail(email).orElse(null);
        if (u == null) return ResponseEntity.badRequest().body("Người dùng không tồn tại");
        if (u.isEnabled()) return ResponseEntity.badRequest().body("Tài khoản đã kích hoạt");
        if (!code.equals(u.getActivationCode())) return ResponseEntity.badRequest().body("Mã kích hoạt không đúng");

        u.setEnabled(true);
        u.setActivationCode(null);
        userRepo.save(u);
        return ResponseEntity.ok("Kích hoạt thành công");
    }

    /** Đăng nhập -> JWT */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            if (!auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Xác thực không thành công");
            }

            var u = userRepo.findByUsername(req.getUsername()).orElseThrow();

            var roles = u.getListRoles().stream()
                    .map(Role::getNameRole)
                    .toList();

            String token = jwtService.generateToken(u.getUsername(), roles, null);
            return ResponseEntity.ok(new JwtResponse(token));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tên đăng nhập hoặc mật khẩu không đúng!");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không thể xác thực: " + e.getMessage());
        }
    }

    /* ============================ ME / PROFILE ============================ */

    /** Thông tin user hiện tại */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails me) {
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        var u = userRepo.findByUsername(me.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.status(404).body("Không tìm thấy người dùng");
        return ResponseEntity.ok(toUserJson(u));
    }

    /** Đổi mật khẩu */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User me,
            @RequestBody Map<String, String> body
    ) {
        String current = body.getOrDefault("currentPassword", "");
        String next = body.getOrDefault("newPassword", "");
        if (next == null || next.length() < 6) {
            return ResponseEntity.badRequest().body("Mật khẩu mới tối thiểu 6 ký tự");
        }
        var u = userRepo.findByUsername(me.getUsername()).orElseThrow();
        if (!passwordEncoder.matches(current, u.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không đúng");
        }
        u.setPassword(passwordEncoder.encode(next));
        userRepo.save(u);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    /** Quên mật khẩu: gửi mật khẩu tạm vào email */
    @PutMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("Thiếu email");
        var u = userRepo.findByEmail(email).orElse(null);
        if (u == null) return ResponseEntity.badRequest().body("Email không tồn tại");

        String temp = RandomStringUtils.randomAlphanumeric(10);
        u.setPassword(passwordEncoder.encode(temp));
        userRepo.save(u);

        mailService.sendHtml(email, "[Thuê Trọ] Mật khẩu tạm thời",
                """
                <p>Mật khẩu tạm thời của bạn là: <b>%s</b></p>
                <p>Vui lòng đăng nhập và đổi lại mật khẩu.</p>
                """.formatted(temp));
        return ResponseEntity.ok("Đã gửi mật khẩu tạm thời vào email");
    }

    /** Đổi avatar (nhận URL ảnh đã upload Cloudinary) */
    @PutMapping("/change-avatar")
    public ResponseEntity<?> changeAvatar(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User me,
            @RequestBody Map<String, String> body
    ) {
        String url = body.get("url");
        if (url == null || url.isBlank()) return ResponseEntity.badRequest().body("Thiếu url");
        var u = userRepo.findByUsername(me.getUsername()).orElseThrow();
        u.setAvatar(url);
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("avatar", url));
    }

    /** Cập nhật hồ sơ cơ bản (user tự sửa) */
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User me,
            @RequestBody Map<String, Object> body
    ) {
        var u = userRepo.findByUsername(me.getUsername()).orElseThrow();
        applyBasicProfile(u, body);
        userRepo.save(u);
        return ResponseEntity.ok("Cập nhật hồ sơ thành công");
    }

    /** Xem trạng thái host của chính mình */
    @GetMapping("/me/host-status")
    public ResponseEntity<?> myHostStatus(@AuthenticationPrincipal org.springframework.security.core.userdetails.User me) {
        var u = userRepo.findByUsername(me.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        var latest = hostRequestRepo.findTopByUserOrderByCreatedAtDesc(u).orElse(null);
        String status = u.isHost() ? "APPROVED" : (latest == null ? "NONE" : latest.getStatus().name());
        return ResponseEntity.ok(Map.of(
                "isHost", u.isHost(),
                "latestRequestStatus", status
        ));
    }

    private <T, R> Map<String, Object> pageResponse(Page<T> p, Function<T, R> mapper) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("content", p.map(mapper).getContent());
        res.put("totalPages", p.getTotalPages());
        res.put("totalElements", p.getTotalElements());
        res.put("pageNumber", p.getNumber());
        res.put("pageSize", p.getSize());
        return res;
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean isHost,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idUser") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction dir
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<User> p = userRepo.searchUsers(q, enabled, isHost, role, pageable);
        return ResponseEntity.ok(pageResponse(p, this::toUserJson));
    }

}
