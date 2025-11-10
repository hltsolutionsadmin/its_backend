package com.its.common.dto;

import com.its.commonservice.enums.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequestDTO {
    
    @NotBlank(message = "Comment text is required")
    @Size(min = 1, max = 5000)
    private String text;
    
    @NotNull(message = "Comment type is required")
    private CommentType type;  // COMMENT, WORK_NOTE
    
    private Boolean isInternal = false;  // True for work notes
}
