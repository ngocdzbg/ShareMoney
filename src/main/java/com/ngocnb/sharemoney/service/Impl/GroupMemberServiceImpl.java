package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.dto.request.GroupMemberBatchCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupMemberCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupMemberUpdateRequest;
import com.ngocnb.sharemoney.dto.response.GroupMemberResponse;
import com.ngocnb.sharemoney.entity.GroupMember;
import com.ngocnb.sharemoney.entity.GroupName;
import com.ngocnb.sharemoney.exception.ResourceNotFoundException;
import com.ngocnb.sharemoney.repository.GroupMemberRepository;
import com.ngocnb.sharemoney.repository.GroupNameRepository;
import com.ngocnb.sharemoney.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMemberServiceImpl implements GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupNameRepository groupNameRepository;

    @Override
    @Transactional
    public GroupMemberResponse addMemberToGroup(Long groupId, GroupMemberCreateRequest request) {
        log.info("Service: Adding member '{}' to group ID: {}", request.getName(), groupId);

        GroupName group = groupNameRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .name(request.getName())
                .build();

        GroupMember savedMember = groupMemberRepository.save(groupMember);
        return mapToResponse(savedMember);
    }

    @Override
    @Transactional
    public List<GroupMemberResponse> addMembersToGroup(Long groupId, GroupMemberBatchCreateRequest request) {
        log.info("Service: Batch adding {} members to group ID: {}", request.getMembers().size(), groupId);

        GroupName group = groupNameRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        List<GroupMember> membersToSave = request.getMembers().stream()
                .map(req -> GroupMember.builder()
                        .group(group)
                        .name(req.getName())
                        .build())
                .collect(Collectors.toList());

        List<GroupMember> savedMembers = groupMemberRepository.saveAll(membersToSave);

        return savedMembers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GroupMemberResponse getMemberById(Long groupId, Long memberId) {
        log.info("Service: Fetching member ID: {} in group ID: {}", memberId, groupId);

        GroupMember member = groupMemberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId + " in group: " + groupId));

        return mapToResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupMemberResponse> getMembersByGroupId(Long groupId, int page, int size, String sortBy, String direction) {
        log.info("Service: Fetching members for group ID: {}", groupId);

        if (!groupNameRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with id: " + groupId);
        }

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, 30), sort);
        Page<GroupMember> memberPage = groupMemberRepository.findByGroupId(groupId, pageable);

        return memberPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public GroupMemberResponse updateMember(Long groupId, Long memberId, GroupMemberUpdateRequest request) {
        log.info("Service: Updating member ID: {} in group ID: {}", memberId, groupId);

        GroupMember member = groupMemberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId + " in group: " + groupId));

        member.setName(request.getName());
        GroupMember updatedMember = groupMemberRepository.save(member);

        return mapToResponse(updatedMember);
    }

    @Override
    @Transactional
    public void deleteMember(Long groupId, Long memberId) {
        log.info("Service: Deleting member ID: {} from group ID: {}", memberId, groupId);

        GroupMember member = groupMemberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId + " in group: " + groupId));

        groupMemberRepository.delete(member);
    }

    private GroupMemberResponse mapToResponse(GroupMember member) {
        return GroupMemberResponse.builder()
                .id(member.getId())
                .groupId(member.getGroup().getId())
                .name(member.getName())
                .build();
    }
}
