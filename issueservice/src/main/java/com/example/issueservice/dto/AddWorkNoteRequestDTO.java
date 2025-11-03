package com.example.issueservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddWorkNoteRequestDTO {
    @NotBlank(message = "Note text is required")
    private String note;
    // true: internal work note; false: public comment (handled by comments API)
    private boolean internal = true;
}
