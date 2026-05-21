package com.lexicon.prestamo.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lexicon.prestamo.entity.User;
import com.lexicon.prestamo.repository.UserRepository;

@TestConfiguration
public class TestDataConfig {

    @Bean
    public TestDataInitializer testDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new TestDataInitializer(userRepository, passwordEncoder);
    }

    public static class TestDataInitializer {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public TestDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            initializeTestData();
        }

        public void initializeTestData() {
            if (userRepository.findByUsername("claudio").isEmpty()) {
                User defaultUser = User.builder()
                        .username("claudio")
                        .passwordHash(passwordEncoder.encode("1234"))
                        .build();
                userRepository.save(defaultUser);
            }
        }
    }
}
