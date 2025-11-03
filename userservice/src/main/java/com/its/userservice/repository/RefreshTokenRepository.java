package com.its.userservice.repository;

import com.its.userservice.model.RefreshTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RefreshTokenModel
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, Long> {
    
    Optional<RefreshTokenModel> findByToken(String token);
    
    @Modifying
    @Query("DELETE FROM RefreshTokenModel rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE RefreshTokenModel rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);
}
