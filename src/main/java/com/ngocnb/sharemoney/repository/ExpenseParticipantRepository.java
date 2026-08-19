package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.entity.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {
}
