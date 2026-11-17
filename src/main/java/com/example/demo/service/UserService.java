package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UserResponse;

public interface UserService {

    UserResponse create(CreateUserRequest request);
}
