package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.dto.request.GroupMemberRequest;
import com.ngocnb.sharemoney.dto.response.GroupMemberResponse;
import com.ngocnb.sharemoney.service.GroupMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-user")
@RequiredArgsConstructor
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    /* * POST /api/groups/{groupId}/members * * Thêm thành viên vào Group. */
    @PostMapping
    public ResponseEntity<GroupMemberResponse> createMember(@PathVariable Long groupId, @Valid @RequestBody GroupMemberRequest request) {
        GroupMemberResponse response = groupMemberService.createMember(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /* * GET /api/groups/{groupId}/members * * Lấy danh sách thành viên của Group. */
    @GetMapping
    public ResponseEntity<List<GroupMemberResponse>> getMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupMemberService.getMembersByGroupId(groupId));
    }

    /* * GET /api/groups/{groupId}/members/{memberId} * * Lấy một thành viên cụ thể. */
    @GetMapping("/{memberId}")
    public ResponseEntity<GroupMemberResponse> getMember(@PathVariable Long groupId, @PathVariable Long memberId) {
        return ResponseEntity.ok(groupMemberService.getMemberById(groupId, memberId));
    }

    /* * PUT /api/groups/{groupId}/members/{memberId} * * Cập nhật tên thành viên. */
    @PutMapping("/{memberId}")
    public ResponseEntity<GroupMemberResponse> updateMember(@PathVariable Long groupId, @PathVariable Long memberId, @Valid @RequestBody GroupMemberRequest request) {
        return ResponseEntity.ok(groupMemberService.updateMember(groupId, memberId, request));
    }

    /* * DELETE /api/groups/{groupId}/members/{memberId} * * Xóa thành viên khỏi Group. */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long groupId, @PathVariable Long memberId) {
        groupMemberService.deleteMember(groupId, memberId);
        return ResponseEntity.noContent().build();
    }
}
