package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateRequest {
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String groupName;
}
