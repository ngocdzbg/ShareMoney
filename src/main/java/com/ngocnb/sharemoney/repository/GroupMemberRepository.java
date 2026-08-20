package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.entity.GroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Page<GroupMember> findByGroupId(Long groupId, Pageable pageable);

    Optional<GroupMember> findByIdAndGroupId(Long id, Long groupId);

    List<GroupMember> findByGroupId(Long groupId);
}
