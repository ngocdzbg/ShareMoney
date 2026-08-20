package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);

    Optional<GroupMember> findByIdAndGroupId(
            Long id,
            Long groupId
    );
}
