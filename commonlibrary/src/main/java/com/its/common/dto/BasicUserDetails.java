package com.its.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasicUserDetails {

    private Long id;

    @NotNull(message = "fullName must not be null")
    private String fullName;

    private String email;

    private boolean newUser;

    private String fcmToken;

    private String gender;

    private Long businessId;

    private String rollNumber;
    private String qualification;
    private Boolean skillrat;
    private Boolean yardly ;
    private Boolean eato ;
    private Boolean sancharalakshmi;
    private Boolean deliveryPartner;

    private String userType;

    private List<MultipartFile> mediaFiles;
    private List<String> mediaUrls;
    private List<MediaDTO> media;

    private MultipartFile profilePicture;
    private String branch;

    private Integer studentStartYear;
    private Boolean isStudentVerified;
    private Integer studentEndYear;
    private Long currentYear;
    private String password;
    private Boolean profileCompleted;


}
