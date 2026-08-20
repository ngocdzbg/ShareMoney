package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.GroupMemberBatchCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupMemberCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupMemberUpdateRequest;
import com.ngocnb.sharemoney.dto.response.GroupMemberResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface GroupMemberService {
    GroupMemberResponse addMemberToGroup(Long groupId, GroupMemberCreateRequest request);

    List<GroupMemberResponse> addMembersToGroup(Long groupId, GroupMemberBatchCreateRequest request);

    GroupMemberResponse getMemberById(Long groupId, Long memberId);

    Page<GroupMemberResponse> getMembersByGroupId(Long groupId, int page, int size, String sortBy, String direction);

    GroupMemberResponse updateMember(Long groupId, Long memberId, GroupMemberUpdateRequest request);

    void deleteMember(Long groupId, Long memberId);
}
