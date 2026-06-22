package com.ms.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ms.bff.dto.AuthResponse;
import com.ms.bff.dto.LoginRequest;
import com.ms.bff.dto.RegisterRequest;

@Component
public class AuthClient {

    private final RestClient restClient;
    private final String authBaseUrl;

    public AuthClient(RestClient restClient, @Value("${auth.base.url}") String authBaseUrl) {
        this.restClient = restClient;
        this.authBaseUrl = authBaseUrl;
    }

    private record BackendAuthBody(String username, String email, String password) {}

    public AuthResponse login(LoginRequest request) {
        BackendAuthBody body = new BackendAuthBody(request.email(), request.email(), request.password());

        return restClient.post()
            .uri(authBaseUrl + "/login")
            .body(body)
            .retrieve()
            .body(AuthResponse.class);
    }

    public AuthResponse register(RegisterRequest request) {
        BackendAuthBody body = new BackendAuthBody(request.email(), request.email(), request.password());

        return restClient.post()
            .uri(authBaseUrl + "/register")
            .body(body)
            .retrieve()
            .body(AuthResponse.class);
    }
}
