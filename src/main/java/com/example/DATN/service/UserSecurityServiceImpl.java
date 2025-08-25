package com.example.DATN.service;

import com.example.DATN.dao.UserRepository;
import com.example.DATN.entity.Role;
import com.example.DATN.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSecurityServiceImpl implements UserSecurityService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        // UserRepository NÊN là: Optional<User> findByUsername(String username);
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại"));

        if (!user.isEnabled()) {
            throw new DisabledException("Tài khoản chưa được kích hoạt");
        }

        Collection<? extends GrantedAuthority> authorities = user.getListRoles().stream()
                .map(Role::getNameRole)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());


        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
