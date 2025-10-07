package com.example.DATN.service.hostRequest;

import com.example.DATN.dao.HostRequestRepository;
import com.example.DATN.dao.RoleRepository;
import com.example.DATN.dao.UserRepository;
import com.example.DATN.entity.HostRequest;
import com.example.DATN.entity.Role;
import com.example.DATN.entity.User;
import com.example.DATN.enums.HostRequestStatus;
import com.example.DATN.service.email.MailService;
import com.example.DATN.service.hostRequest.HostRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HostRequestServiceImpl implements HostRequestService {

    private final HostRequestRepository hostRequestRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final MailService mailService;

    private static final String ROLE_HOST = "HOST";

    @Override
    public void approve(Long requestId, String adminUsername, String note) {
        HostRequest hr = hostRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

        if (hr.getStatus() != HostRequestStatus.PENDING) {
            throw new IllegalStateException("Chỉ duyệt yêu cầu ở trạng thái PENDING");
        }

        User user = hr.getUser();
        ensureRolesCollection(user);

        // Lấy/tạo ROLE_HOST
        Role hostRole = getOrCreateRole(ROLE_HOST);

        // Thêm ROLE_HOST nếu chưa có
        addRoleIfMissing(user, hostRole);

        // Bật cờ host (map tới cột id_host nếu bạn đang dùng)
        user.setHost(true);
        userRepo.save(user);

        // Cập nhật request
        hr.setStatus(HostRequestStatus.APPROVED);
        hr.setNote(note);
        // Nếu có field reviewedBy / reviewedAt thì mở 2 dòng dưới
        // hr.setReviewedBy(adminUsername);
        // hr.setReviewedAt(Instant.now());
        hostRequestRepo.save(hr);

        // Gửi mail
        mailService.sendHtml(
                user.getEmail(),
                "[Thuê Trọ] Yêu cầu làm chủ trọ đã được duyệt",
                """
                <p>Chào %s,</p>
                <p>Yêu cầu làm chủ trọ của bạn đã được <b>DUYỆT</b>.</p>
                <p>Bạn có thể đăng phòng tại khu vực Chủ trọ.</p>
                <p>Trân trọng,<br/>Thuê Trọ Team</p>
                """.formatted(user.getUsername())
        );
    }

    @Override
    public void reject(Long requestId, String adminUsername, String note) {
        HostRequest hr = hostRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

        if (hr.getStatus() != HostRequestStatus.PENDING) {
            throw new IllegalStateException("Chỉ từ chối yêu cầu ở trạng thái PENDING");
        }

        User user = hr.getUser();
        ensureRolesCollection(user);

        // Bỏ ROLE_HOST nếu có
        removeRoleByName(user, ROLE_HOST);

        // Tắt cờ host (id_host = 0)
        user.setHost(false);
        userRepo.save(user);

        // Cập nhật request
        hr.setStatus(HostRequestStatus.REJECTED);
        hr.setNote(note);
        // hr.setReviewedBy(adminUsername);
        // hr.setReviewedAt(Instant.now());
        hostRequestRepo.save(hr);

        // Gửi mail
        String reasonHtml = (note == null || note.isBlank()) ? "" : ("<p>Lý do: " + note + "</p>");
        mailService.sendHtml(
                user.getEmail(),
                "[Thuê Trọ] Yêu cầu làm chủ trọ bị từ chối",
                """
                <p>Chào %s,</p>
                <p>Rất tiếc, yêu cầu làm chủ trọ của bạn đã bị <b>TỪ CHỐI</b>.</p>
                %s
                <p>Nếu có thắc mắc, vui lòng phản hồi email này.</p>
                <p>Trân trọng,<br/>Thuê Trọ Team</p>
                """.formatted(user.getUsername(), reasonHtml)
        );
    }

    /* ==================== Helpers ==================== */

    private Role getOrCreateRole(String roleName) {
        Role role = roleRepo.findByNameRole(roleName);
        if (role != null) return role;
        Role newRole = new Role();
        newRole.setNameRole(roleName);
        return roleRepo.save(newRole);
    }

    private void ensureRolesCollection(User user) {
        if (user.getListRoles() == null) {
            user.setListRoles(new java.util.ArrayList<>());
        }
    }

    private void addRoleIfMissing(User user, Role role) {
        boolean exists = user.getListRoles().stream()
                .anyMatch(r -> r.getNameRole().equalsIgnoreCase(role.getNameRole()));
        if (!exists) {
            user.getListRoles().add(role);
        }
    }

    private void removeRoleByName(User user, String roleName) {
        if (user.getListRoles() == null) return;
        user.getListRoles().removeIf(r -> roleName.equalsIgnoreCase(r.getNameRole()));
    }
}
