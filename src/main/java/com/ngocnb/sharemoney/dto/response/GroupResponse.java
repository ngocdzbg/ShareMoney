package com.ngocnb.sharemoney.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupResponse {
    private Long id;
    private String groupName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
