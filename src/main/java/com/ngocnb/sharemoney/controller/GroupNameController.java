package com.ngocnb.sharemoney.controller;

import com.ngocnb.sharemoney.dto.request.GroupNameCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupNameUpdateRequest;
import com.ngocnb.sharemoney.dto.response.GroupNameResponse;
import com.ngocnb.sharemoney.service.GroupNameService;
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

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Group Management", description = "API để quản lý group")
@SecurityRequirement(name = "Bearer Authentication")
public class GroupNameController {

    private final GroupNameService groupNameService;

    @PostMapping()
    public ResponseEntity<GroupNameResponse> createGroup(@Valid @RequestBody GroupNameCreateRequest request) {
        log.info("POST /api/groups - Create group: {}", request.getGroupName());
        GroupNameResponse response = groupNameService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupNameResponse> getGroupById(
            @PathVariable
            @Positive(message = "Id must be greater than 0")
            Long id
    ) {
        log.info("GET /api/groups/{}", id);
        GroupNameResponse response = groupNameService.getGroupById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<GroupNameResponse>> getAllGroups(
            @Parameter(description = "Số trang (zero-based, mặc định 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng items per trang (max 30)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Trường sắp xếp: id, name, createdAt")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Hướng sắp xếp: ASC hoặc DESC")
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/groups - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);
        Page<GroupNameResponse> groups = groupNameService.getAllGroups(page, size, sortBy, direction);

        return ResponseEntity.ok(groups);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GroupNameResponse> updateGroup(
            @PathVariable
            @Positive(message = "Id must be greater than 0")
            Long id,
            @Valid @RequestBody GroupNameUpdateRequest request
    ) {
        log.info("PUT /api/groups/{} - Update group", id);
        GroupNameResponse response = groupNameService.updateGroup(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable
            @Positive(message = "Id must be greater than 0")
            Long id
    ) {
        log.info("DELETE /api/groups/{} - Delete group", id);
        groupNameService.deleteGroup(id);

        return ResponseEntity.noContent().build();
    }
}
