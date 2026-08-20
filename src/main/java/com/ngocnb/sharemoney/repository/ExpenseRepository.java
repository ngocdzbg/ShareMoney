package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT DISTINCT e FROM Expense e " +
            "LEFT JOIN FETCH e.paidBy " +
            "LEFT JOIN FETCH e.participants p " +
            "LEFT JOIN FETCH p.groupMember " +
            "WHERE e.group.id = :groupId")
    Page<Expense> findByGroupIdFetchDetails(@Param("groupId") Long groupId, Pageable pageable);

    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.paidBy " +
            "LEFT JOIN FETCH e.participants p " +
            "LEFT JOIN FETCH p.groupMember " +
            "WHERE e.id = :id AND e.group.id = :groupId")
    Optional<Expense> findByIdAndGroupIdFetchDetails(@Param("id") Long id, @Param("groupId") Long groupId);

    // ➕ THÊM MỚI: Lấy tất cả khoản chi + chi tiết người tham gia (Dùng cho Settlement)
    @Query("SELECT DISTINCT e FROM Expense e " +
            "LEFT JOIN FETCH e.paidBy " +
            "LEFT JOIN FETCH e.participants p " +
            "LEFT JOIN FETCH p.groupMember " +
            "WHERE e.group.id = :groupId")
    List<Expense> findAllByGroupIdFetchDetails(@Param("groupId") Long groupId);
}
