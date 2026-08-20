package com.ngocnb.sharemoney.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberBatchCreateRequest {
    @NotEmpty(message = "Danh sách thành viên không được để trống")
    @Valid
    private List<GroupMemberCreateRequest> members;
}
