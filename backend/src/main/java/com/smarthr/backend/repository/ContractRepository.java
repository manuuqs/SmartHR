package com.smarthr.backend.repository;
import com.smarthr.backend.domain.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {}
