package com.its.userservice.model;

import com.its.commonservice.enums.GenericModel;

import com.its.commonservice.enums.TicketPriority;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USER_GROUPS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserGroupModel extends GenericModel {

    @Column(name = "GROUP_NAME", nullable = false)
    private String groupName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PROJECT_ID")
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_LEAD_ID")
    private UserModel groupLead;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRIORITY", nullable = false)
    private TicketPriority priority;

    @ManyToMany
    @JoinTable(
            name = "user_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserModel> members = new HashSet<>();


}
