package com.ms.bff.services;

import com.ms.bff.client.AuthClient;
import com.ms.bff.dto.AuthResponse;
import com.ms.bff.dto.LoginRequest;
import com.ms.bff.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthClient authClient;

    public AuthService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public AuthResponse login(LoginRequest request) {
        return authClient.login(request);
    }

    public AuthResponse register(RegisterRequest request) {
        return authClient.register(request);
    }


}
