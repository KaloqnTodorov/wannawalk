package com.wannawalk.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wannawalk.backend.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByConfirmationToken(String confirmationToken);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
