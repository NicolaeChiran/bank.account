package com.demo.bank.account.repository;

import com.demo.bank.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // No custom methods needed
}

