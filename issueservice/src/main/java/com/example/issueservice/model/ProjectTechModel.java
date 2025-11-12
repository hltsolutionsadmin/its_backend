package com.example.issueservice.model;

import com.its.commonservice.enums.GenericModel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PROJECT_TECH_STACK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ProjectTechModel extends GenericModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private ProjectModel project;

    @Column(name = "TECHNOLOGY_NAME", nullable = false)
    private String technologyName;

    @Column(name = "VERSION")
    private String version;
}
