package com.its.userservice.repository;

import com.its.commonservice.enums.UserRole;
import com.its.userservice.model.RoleModel;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleModel, Long> {
    Optional<RoleModel> findByRole(UserRole role);

    @Query("SELECT r FROM RoleModel r WHERE r.role = :role")
    Optional<RoleModel> findByName(@Param("role") UserRole role);

}
