package com.example.incuserservice.repository;

import com.example.incuserservice.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserModel
 */
@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    
    Optional<UserModel> findByUsername(String username);
    
    Optional<UserModel> findByEmail(String email);
    
    @Query("SELECT u FROM UserModel u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<UserModel> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
