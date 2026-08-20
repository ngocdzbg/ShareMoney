package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    void deleteByGroupId(Long groupId); // Dùng nếu muốn xóa sổ cũ để chốt lại
}