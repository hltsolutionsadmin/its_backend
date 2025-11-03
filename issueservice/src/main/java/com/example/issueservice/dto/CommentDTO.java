package com.example.issueservice.dto;

import com.its.commonservice.enums.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private String text;
    private CommentType type;
    private Boolean isInternal;
    private Instant createdAt;
}
