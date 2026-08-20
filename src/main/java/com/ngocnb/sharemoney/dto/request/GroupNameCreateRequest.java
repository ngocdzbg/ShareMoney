package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupNameCreateRequest {
    /**
     * Tên nhóm
     */
    @NotBlank(message = "groupName không được để trống")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String groupName;
}
