package com.ngocnb.sharemoney.service.Impl;

import com.ngocnb.sharemoney.dto.request.GroupMemberRequest;
import com.ngocnb.sharemoney.dto.response.GroupMemberResponse;
import com.ngocnb.sharemoney.entity.GroupMember;
import com.ngocnb.sharemoney.entity.GroupName;
import com.ngocnb.sharemoney.exception.ResourceNotFoundException;
import com.ngocnb.sharemoney.repository.GroupMemberRepository;
import com.ngocnb.sharemoney.repository.GroupNameRepository;
import com.ngocnb.sharemoney.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMemberServiceImpl
        implements GroupMemberService {

    private final GroupNameRepository groupNameRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Override
    @Transactional
    public GroupMemberResponse createMember(Long groupId, GroupMemberRequest request) {
        log.debug("Creating member for group: {}", groupId);
        GroupName group = getGroup(groupId);
        GroupMember member = GroupMember.builder().group(group).name(request.getName().trim()).build();
        GroupMember savedMember = groupMemberRepository.save(member);
        log.info("Created group member - id: {}, groupId: {}", savedMember.getId(), groupId);
        return convertToResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getMembersByGroupId(Long groupId) {
        log.debug("Getting members for group: {}", groupId); /* * Kiểm tra GroupName tồn tại. */
        getGroup(groupId);
        return groupMemberRepository.findByGroupId(groupId).stream().map(this::convertToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupMemberResponse getMemberById(Long groupId, Long memberId) {
        log.debug("Getting member {} from group {}", memberId, groupId);
        GroupMember member = getMember(groupId, memberId);
        return convertToResponse(member);
    }

    @Override
    @Transactional
    public GroupMemberResponse updateMember(Long groupId, Long memberId, GroupMemberRequest request) {
        log.debug("Updating member {} in group {}", memberId, groupId);
        GroupMember member = getMember(groupId, memberId);
        member.setName(request.getName().trim());
        GroupMember updatedMember = groupMemberRepository.save(member);
        log.info("Updated group member - id: {}, groupId: {}", memberId, groupId);
        return convertToResponse(updatedMember);
    }

    @Override
    @Transactional
    public void deleteMember(Long groupId, Long memberId) {
        log.debug("Deleting member {} from group {}", memberId, groupId);
        GroupMember member = getMember(groupId, memberId);
        groupMemberRepository.delete(member);
        log.info("Deleted group member - id: {}, groupId: {}", memberId, groupId);
    } /* * Lấy GroupName. */

    private GroupName getGroup(Long groupId) {
        return groupNameRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group với ID " + groupId + " không tồn tại"));
    } /* * Lấy GroupMember và đảm bảo member * thuộc đúng GroupName. */

    private GroupMember getMember(Long groupId, Long memberId) {
        return groupMemberRepository.findByIdAndGroupId(memberId, groupId).orElseThrow(() -> new ResourceNotFoundException("GroupMember với ID " + memberId + " không tồn tại trong Group " + groupId));
    } /* * Convert Entity -> Response DTO. */

    private GroupMemberResponse convertToResponse(GroupMember member) {
        return GroupMemberResponse.builder().id(member.getId()).groupId(member.getGroup().getId()).name(member.getName()).build();
    }
}
