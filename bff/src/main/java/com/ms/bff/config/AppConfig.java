package com.ms.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {


   @Bean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

    @Bean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}

}

