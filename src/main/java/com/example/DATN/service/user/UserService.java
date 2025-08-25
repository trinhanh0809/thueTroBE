package com.example.DATN.service.user;


import com.example.DATN.dto.user.CreateUserRequest;
import com.example.DATN.dto.user.UserResponse;

public interface UserService {
    UserResponse register(CreateUserRequest req);
}
