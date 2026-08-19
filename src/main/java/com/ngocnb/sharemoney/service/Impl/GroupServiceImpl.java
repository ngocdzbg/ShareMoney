package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.dto.request.CreateGroupRequest;
import com.ngocnb.sharemoney.dto.request.UpdateGroupRequest;
import com.ngocnb.sharemoney.dto.response.GroupResponse;
import com.ngocnb.sharemoney.entity.Group;
import com.ngocnb.sharemoney.repository.GroupRepository;
import com.ngocnb.sharemoney.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;


    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        log.debug("Creating new group: {}", request.getGroupName());
        Group group = Group.builder()
                .name(request.getGroupName())
                .build();

        Group savedGroup = groupRepository.save(group);
        log.info("Group created successfully with id: {} - name: {}", savedGroup.getId(), savedGroup.getName());

        return toResponse(savedGroup);
    }

    @Override
    public GroupResponse getGroupById(Long id) {
        log.debug("Fetching group detail with id: {}", id);
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        log.info("Group found: {}", group.getName());
        return toResponse(group);
    }


    /**
     * Lấy danh sách tất cả nhóm với phân trang và sắp xếp
     *
     * @param page      Số trang (zero-based, mặc định 0)
     * @param size      Số lượng trái cây mỗi trang (mặc định 10)
     * @param sortBy    Trường để sắp xếp: id, name, price, createdAt (mặc định: id)
     * @param direction Hướng sắp xếp: ASC hoặc DESC (mặc định: ASC)
     * @return Page<GroupResponse> chứa danh sách trái cây
     * @throws IllegalArgumentException nếu sortBy không hợp lệ
     */
    @Override
    @Transactional(readOnly = true)
    public Page<GroupResponse> getAllGroups(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        log.debug(
                "Fetching group list - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction
        );

        // Validate sortBy parameter
        String[] validSortFields = {"id", "groupName", "createdAt"};

        boolean isValidField = Arrays.asList(validSortFields).contains(sortBy);

        if (!isValidField) {
            log.warn("Invalid sortBy field: {}", sortBy);

            throw new IllegalArgumentException(
                    String.format(
                            "Invalid sortBy field: %s. Valid fields: %s",
                            sortBy,
                            String.join(", ", validSortFields)
                    )
            );
        }

        // Create Sort object
        Sort.Direction sortDirection =
                "DESC".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Sort sort = Sort.by(sortDirection, sortBy);

        // Create Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch data
        Page<Group> groupsPage =
                groupRepository.findAll(pageable);

        log.info(
                "Found {} groups (total: {})",
                groupsPage.getNumberOfElements(),
                groupsPage.getTotalElements()
        );

        return groupsPage.map(this::toResponse);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(Long id, UpdateGroupRequest req) {
        log.debug("Updating group with id: {}", id);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Group not found with id: {} for update", id);
                    return new RuntimeException("Group not found");
                });

        if (req.getGroupName() != null) {
            group.setName(req.getGroupName());
        }

        Group updatedGroup = groupRepository.save(group);

        log.info("Group updated successfully with id: {}", updatedGroup.getId());
        return toResponse(group);
    }

    @Override
    public void deleteGroup(Long id) {
        log.debug("Deleting group with id: {}", id);
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Group not group with id: {} for deletion", id);
                    return new RuntimeException("Group not found");
                });

        groupRepository.delete(group);
        log.info("Group deleted successfully with id: {}", id);
    }

    private GroupResponse toResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getName())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
