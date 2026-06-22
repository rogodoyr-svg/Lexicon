package com.lexicon.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lexicon.auth.model.User;



public interface UserRepository  extends JpaRepository<User, Long> {
    
    Optional<User> findByEmailAndActiveTrue(String email);

}
