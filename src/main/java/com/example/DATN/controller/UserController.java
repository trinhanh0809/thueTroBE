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
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

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

    /* ========== AUTH ========== */

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

        // Tạo người dùng
        User u = new User();
        u.setUsername(req.username());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setEmail(req.email());
        u.setFirstName(req.firstName());
        u.setLastName(req.lastName());
        u.setPhoneNumber(req.phoneNumber());
        u.setAvatar("");
        u.setEnabled(false); // cần kích hoạt email
        u.setHost(false);
        u.setActivationCode(java.util.UUID.randomUUID().toString());
        u.setListRoles(java.util.List.of(customer));
        userRepo.save(u);

        // Gửi email kích hoạt
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

        // Nếu user xin làm chủ trọ → tạo yêu cầu PENDING + bắn mail admin & user
        if (req.applyHost()) {
            HostRequest hr = new HostRequest();
            hr.setUser(u);
            hr.setStatus(HostRequestStatus.PENDING);
            hostRequestRepo.save(hr);

            // mail admin
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
            // mail user
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


            // Lấy user + roles từ DB
            var u = userRepo.findByUsername(req.getUsername()).orElseThrow();

            // roles KHÔNG prefix, ví dụ ["ADMIN","CUSTOMER"]
            var roles = u.getListRoles().stream()
                    .map(Role::getNameRole)
                    .toList();

            // (tuỳ chọn) claim phụ cho FE
            var extra = Map.of(
                    "id", u.getIdUser(),
                    "email", u.getEmail(),
                    "firstName", u.getFirstName(),
                    "lastName", u.getLastName()
            );

            // Phát hành JWT có roles
            String token = jwtService.generateToken(u.getUsername(), roles, null);

            // Giữ field FE đang đọc: jwtToken
            return ResponseEntity.ok(new JwtResponse(token));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tên đăng nhập hoặc mật khẩu không đúng!");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không thể xác thực: " + e.getMessage());
        }
    }


    /* ========== ME / PROFILE ========== */

    /** Thông tin user hiện tại */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails me) {
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        var u = userRepo.findByUsername(me.getUsername()).orElse(null);
        if (u == null) return ResponseEntity.status(404).body("Không tìm thấy người dùng");
        return ResponseEntity.ok(Map.of(
                "id", u.getIdUser(),
                "username", u.getUsername(),
                "email", u.getEmail(),
                "firstName", u.getFirstName(),
                "lastName", u.getLastName(),
                "phoneNumber", u.getPhoneNumber(),
                "avatar", u.getAvatar(),
                "enabled", u.isEnabled(),
                "isHost", u.isHost(),
                "roles", u.getListRoles().stream().map(Role::getNameRole).toList()
        ));
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

    /** Cập nhật hồ sơ cơ bản */
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User me,
            @RequestBody Map<String, String> body
    ) {
        var u = userRepo.findByUsername(me.getUsername()).orElseThrow();
        if (body.containsKey("firstName")) u.setFirstName(body.get("firstName"));
        if (body.containsKey("lastName"))  u.setLastName(body.get("lastName"));
        if (body.containsKey("phoneNumber")) u.setPhoneNumber(body.get("phoneNumber"));
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

    /* ========== helpers ========== */

    private static String url(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }


}
