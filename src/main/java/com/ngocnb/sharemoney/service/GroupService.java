package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.GroupCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupUpdateRequest;
import com.ngocnb.sharemoney.dto.response.GroupResponse;
import org.springframework.data.domain.Page;

public interface GroupService {
    GroupResponse createGroup(GroupCreateRequest request);

    GroupResponse getGroupById(Long id);

    Page<GroupResponse> getAllGroups(
            int page,
            int size,
            String sortBy,
            String direction
    );

    GroupResponse updateGroup(Long id, GroupUpdateRequest req);

    void deleteGroup(Long id);
}
