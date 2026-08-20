package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.GroupMemberRequest;
import com.ngocnb.sharemoney.dto.response.GroupMemberResponse;

import java.util.List;

public interface GroupMemberService {
    GroupMemberResponse createMember(Long groupId, GroupMemberRequest request);

    List<GroupMemberResponse> getMembersByGroupId(Long groupId);

    GroupMemberResponse getMemberById(Long groupId, Long memberId);

    GroupMemberResponse updateMember(Long groupId, Long memberId, GroupMemberRequest request);

    void deleteMember(Long groupId, Long memberId);
}
