package com.smarthr.backend.repository;
import com.smarthr.backend.domain.Compensation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationRepository extends JpaRepository<Compensation, Long> {}

