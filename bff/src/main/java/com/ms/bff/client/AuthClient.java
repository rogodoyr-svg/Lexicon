package com.ms.bff.client;

import com.ms.bff.dto.AuthResponse;
import com.ms.bff.dto.LoginRequest;
import com.ms.bff.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
@Component
public class AuthClient {

    private final RestClient restClient;
    private final String authBaseUrl;

    public AuthClient(RestClient restClient, @Value("${auth.base.url}") String authBaseUrl) {
        this.restClient = restClient;
        this.authBaseUrl = authBaseUrl;
    }

    public AuthResponse login(LoginRequest request) {
		return restClient.post()
			.uri(authBaseUrl + "/login")
			.body(request)
			.retrieve()
			.body(AuthResponse.class);
	}

    public AuthResponse register(RegisterRequest request) {
		return restClient.post()
			.uri(authBaseUrl + "/register")
			.body(request)
			.retrieve()
			.body(AuthResponse.class);
	} 


}
