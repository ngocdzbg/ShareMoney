package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberRequest {
    @NotBlank(message = "Tên thành viên không được để trống")
    @Size(max = 100, message = "Tên thành viên không được vượt quá 100 ký tự")
    private String name;
}
