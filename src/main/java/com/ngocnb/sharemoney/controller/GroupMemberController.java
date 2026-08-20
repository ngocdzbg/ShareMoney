package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.dto.request.GroupMemberBatchCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupMemberCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupMemberUpdateRequest;
import com.ngocnb.sharemoney.dto.response.GroupMemberResponse;
import com.ngocnb.sharemoney.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/members")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Group Member Management", description = "API để quản lý thành viên trong nhóm")
@SecurityRequirement(name = "Bearer Authentication")
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    @Operation(
            summary = "Thêm một thành viên mới vào nhóm",
            description = "Tạo một thành viên mới trong nhóm bằng cách cung cấp tên thành viên. Thành viên này hoàn toàn độc lập với tài khoản User hệ thống."
    )
    @PostMapping
    public ResponseEntity<GroupMemberResponse> addMember(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @Valid @RequestBody GroupMemberCreateRequest request
    ) {
        log.info("POST /api/groups/{}/members - Add member: {}", groupId, request.getName());
        GroupMemberResponse response = groupMemberService.addMemberToGroup(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Thêm hàng loạt thành viên vào nhóm (Batch Insert)",
            description = "Thêm cùng lúc danh sách nhiều thành viên vào nhóm thông qua mảng JSON, giúp giảm số lượng request gửi lên và tối ưu hiệu năng DB."
    )
    @PostMapping("/batch")
    public ResponseEntity<List<GroupMemberResponse>> addMembersBatch(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @Valid @RequestBody GroupMemberBatchCreateRequest request
    ) {
        log.info("POST /api/groups/{}/members/batch - Adding {} members", groupId, request.getMembers().size());
        List<GroupMemberResponse> response = groupMemberService.addMembersToGroup(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Lấy thông tin chi tiết của một thành viên",
            description = "Truy vấn thông tin chi tiết của thành viên dựa trên memberId và groupId."
    )
    @GetMapping("/{memberId}")
    public ResponseEntity<GroupMemberResponse> getMemberById(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @PathVariable
            @Positive(message = "MemberId must be greater than 0")
            Long memberId
    ) {
        log.info("GET /api/groups/{}/members/{}", groupId, memberId);
        GroupMemberResponse response = groupMemberService.getMemberById(groupId, memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Lấy danh sách thành viên trong nhóm (Phân trang)",
            description = "Trả về danh sách tất cả các thành viên thuộc nhóm chỉ định, có hỗ trợ phân trang (page, size) và sắp xếp (sortBy, direction)."
    )
    @GetMapping
    public ResponseEntity<Page<GroupMemberResponse>> getMembersByGroupId(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,

            @Parameter(description = "Số trang (zero-based, mặc định 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng items per trang (max 30)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Trường sắp xếp: id, name")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Hướng sắp xếp: ASC hoặc DESC")
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/groups/{}/members - page: {}, size: {}, sortBy: {}, direction: {}",
                groupId, page, size, sortBy, direction);
        Page<GroupMemberResponse> members = groupMemberService.getMembersByGroupId(groupId, page, size, sortBy, direction);
        return ResponseEntity.ok(members);
    }

    @Operation(
            summary = "Cập nhật thông tin thành viên",
            description = "Cập nhật tên hiển thị của một thành viên cụ thể trong nhóm theo memberId."
    )
    @PatchMapping("/{memberId}")
    public ResponseEntity<GroupMemberResponse> updateMember(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @PathVariable
            @Positive(message = "MemberId must be greater than 0")
            Long memberId,
            @Valid @RequestBody GroupMemberUpdateRequest request
    ) {
        log.info("PATCH /api/groups/{}/members/{} - Update member", groupId, memberId);
        GroupMemberResponse response = groupMemberService.updateMember(groupId, memberId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Xóa thành viên khỏi nhóm",
            description = "Xóa thành viên ra khỏi nhóm bằng memberId."
    )
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable
            @Positive(message = "GroupId must be greater than 0")
            Long groupId,
            @PathVariable
            @Positive(message = "MemberId must be greater than 0")
            Long memberId
    ) {
        log.info("DELETE /api/groups/{}/members/{} - Delete member", groupId, memberId);
        groupMemberService.deleteMember(groupId, memberId);
        return ResponseEntity.noContent().build();
    }
}
