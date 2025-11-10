package com.its.userservice.model;

import com.its.commonservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "ROLES",
        uniqueConstraints = @UniqueConstraint(columnNames = "NAME")
)
@Getter
@Setter
public class RoleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "NAME", length = 50, nullable = false, unique = true)
    private UserRole role;

    @ManyToMany(mappedBy = "roles")
    private Set<UserModel> users = new HashSet<>();
}
