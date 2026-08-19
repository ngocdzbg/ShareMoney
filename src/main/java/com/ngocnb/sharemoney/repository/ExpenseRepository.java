package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
