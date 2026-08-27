package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.entity.Expense;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ➕ Lấy tất cả khoản chi + chi tiết người tham gia (Dùng cho Settlement)
    @Query("""
                SELECT DISTINCT e FROM Expense e
                LEFT JOIN FETCH e.paidBy
                LEFT JOIN FETCH e.participants p
                LEFT JOIN FETCH p.groupMember
                WHERE e.group.id = :groupId
            """)
    List<Expense> findAllByGroupIdFetchDetails(@Param("groupId") Long groupId);

    @Query("""
                SELECT DISTINCT e
                FROM Expense e
                LEFT JOIN FETCH e.participants p
                LEFT JOIN FETCH p.groupMember
                WHERE e.id = :expenseId
                AND e.group.id = :groupId
            """)
    Optional<Expense> findByIdAndGroupIdFetchDetails(
            @Param("expenseId") Long expenseId,
            @Param("groupId") Long groupId
    );

    @Query("""
                SELECT DISTINCT e
                FROM Expense e
                LEFT JOIN FETCH e.participants p
                LEFT JOIN FETCH p.groupMember
                WHERE e.group.id = :groupId
            """)
    Page<Expense> findByGroupIdFetchDetails(
            @Param("groupId") Long groupId,
            Pageable pageable
    );

    // Tách từ findByGroupIdFetchDetails để không bị N + 1 query
    Page<Expense> findByGroupId(
            Long groupId,
            Pageable pageable
    );

    @Query("""
                SELECT DISTINCT e
                FROM Expense e
                LEFT JOIN FETCH e.group
                LEFT JOIN FETCH e.paidBy
                LEFT JOIN FETCH e.participants p
                LEFT JOIN FETCH p.groupMember
                WHERE e.id IN :expenseIds
            """)
    List<Expense> findAllByIdInFetchDetails(
            @Param("expenseIds") List<Long> expenseIds
    );

    //------------------------------------------------------------
}
