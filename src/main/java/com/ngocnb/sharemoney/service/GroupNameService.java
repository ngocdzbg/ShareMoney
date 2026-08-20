package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.GroupNameCreateRequest;
import com.ngocnb.sharemoney.dto.request.GroupNameUpdateRequest;
import com.ngocnb.sharemoney.dto.response.GroupNameResponse;
import org.springframework.data.domain.Page;

public interface GroupNameService {
    GroupNameResponse createGroup(GroupNameCreateRequest request);

    GroupNameResponse getGroupById(Long id);

    Page<GroupNameResponse> getAllGroups(
            int page,
            int size,
            String sortBy,
            String direction
    );

    GroupNameResponse updateGroup(Long id, GroupNameUpdateRequest req);

    void deleteGroup(Long id);
}
