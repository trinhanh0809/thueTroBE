package com.example.DATN.service.hostRequest;


import com.example.DATN.dao.HostRequestRepository;
import com.example.DATN.dao.RoleRepository;
import com.example.DATN.dao.UserRepository;
import com.example.DATN.entity.HostRequest;
import com.example.DATN.entity.Role;
import com.example.DATN.entity.User;
import com.example.DATN.enums.HostRequestStatus;
import com.example.DATN.service.email.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;

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
        // đảm bảo listRoles không null
        if (user.getListRoles() == null) user.setListRoles(new java.util.ArrayList<>());

        // ensure ROLE_HOST tồn tại
        Role hostRole = roleRepo.findByNameRole(ROLE_HOST);
        if (hostRole == null) {
            hostRole = new Role();
            hostRole.setNameRole(ROLE_HOST);
            hostRole = roleRepo.save(hostRole);
        }

        // thêm ROLE_HOST nếu chưa có
        boolean hasHostRole = user.getListRoles().stream()
                .anyMatch(r -> ROLE_HOST.equalsIgnoreCase(r.getNameRole()));
        if (!hasHostRole) {
            user.getListRoles().add(hostRole);
        }

        // set host flag
        user.setHost(true);
        userRepo.save(user);

        // cập nhật request
        hr.setStatus(HostRequestStatus.APPROVED);
//        hr.setReviewedAt(Instant.now());
        hr.setNote(note);
        // nếu muốn set reviewedBy theo adminUsername thì load admin từ userRepo rồi set:
//        userRepo.findByUsername(adminUsername).ifPresent(hr::setReviewedBy);
        hostRequestRepo.save(hr);

        // email thông báo cho user
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

        // an toàn: bỏ quyền host nếu lỡ có và gỡ cờ isHost
        if (user.getListRoles() != null) {
            user.setListRoles(new java.util.ArrayList<>(
                    new HashSet<>(user.getListRoles()) // tránh trùng
            ));
            user.getListRoles().removeIf(r -> ROLE_HOST.equalsIgnoreCase(r.getNameRole()));
        }
        user.setHost(false);
        userRepo.save(user);

        hr.setStatus(HostRequestStatus.REJECTED);
//        hr.setReviewedAt(Instant.now());
        hr.setNote(note);
//        userRepo.findByUsername(adminUsername).ifPresent(hr::setReviewedBy);
        hostRequestRepo.save(hr);

        // email thông báo cho user
        String reason = (note == null || note.isBlank()) ? "" : ("<p>Lý do: " + note + "</p>");
        mailService.sendHtml(
                user.getEmail(),
                "[Thuê Trọ] Yêu cầu làm chủ trọ bị từ chối",
                """
                <p>Chào %s,</p>
                <p>Rất tiếc, yêu cầu làm chủ trọ của bạn đã bị <b>TỪ CHỐI</b>.</p>
                %s
                <p>Nếu có thắc mắc, vui lòng phản hồi email này.</p>
                <p>Trân trọng,<br/>Thuê Trọ Team</p>
                """.formatted(user.getUsername(), reason)
        );
    }
}

