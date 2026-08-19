package com.ngocnb.sharemoney.repository;

import com.ngocnb.sharemoney.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
