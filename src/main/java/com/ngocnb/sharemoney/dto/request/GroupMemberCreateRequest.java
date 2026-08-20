package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberCreateRequest {

    @NotBlank(message = "Tên thành viên không được để trống")
    private String name;
}