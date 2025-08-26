package com.example.DATN.service.user;

import com.example.DATN.dao.RoleRepository;
import com.example.DATN.dao.UserRepository;
import com.example.DATN.dto.user.CreateUserRequest;
import com.example.DATN.dto.user.UserResponse;
import com.example.DATN.entity.Role;
import com.example.DATN.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(CreateUserRequest req) {
        // validate trùng username/email
        userRepo.findByUsername(req.username()).ifPresent(u -> {
            throw new IllegalArgumentException("Username đã tồn tại");
        });
        userRepo.findByEmail(req.email()).ifPresent(u -> {
            throw new IllegalArgumentException("Email đã tồn tại");
        });

        // lấy role CUSTOMER (nhớ seed trước: ADMIN/CUSTOMER/HOST)
        Role customer = roleRepo.findByNameRole("CUSTOMER");

        // build entity
        User u = new User();
        u.setUsername(req.username());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setEmail(req.email());
        u.setFirstName(req.firstName());
        u.setLastName(req.lastName());
        u.setPhoneNumber(req.phoneNumber());
        u.setAvatar("");
        u.setEnabled(true);      // nếu cần kích hoạt email → để false và set activationCode
        u.setHost(false);
        u.setListRoles(List.of(customer));

        // save
        User saved = userRepo.save(u);

        // map -> response
        return new UserResponse(
                saved.getIdUser(),
                saved.getUsername(),
                saved.getEmail(),
                ((saved.getFirstName()!=null?saved.getFirstName():"") + " " +
                        (saved.getLastName()!=null?saved.getLastName():"")).trim(),
                saved.getPhoneNumber(),
                saved.getAvatar(),
                saved.isEnabled(),
                saved.isHost(),
                saved.getListRoles().stream().map(Role::getNameRole).toList(),
                saved.getCreatedAt()
        );
    }
}
