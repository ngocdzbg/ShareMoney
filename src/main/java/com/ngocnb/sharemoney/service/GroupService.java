package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.CreateGroupRequest;
import com.ngocnb.sharemoney.dto.request.UpdateGroupRequest;
import com.ngocnb.sharemoney.dto.response.GroupResponse;
import org.springframework.data.domain.Page;

public interface GroupService {
    GroupResponse createGroup(CreateGroupRequest request);

    GroupResponse getGroupById(Long id);

    Page<GroupResponse> getAllGroups(
            int page,
            int size,
            String sortBy,
            String direction
    );

    GroupResponse updateGroup(Long id, UpdateGroupRequest req);

    void deleteGroup(Long id);
}
