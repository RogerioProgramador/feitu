package com.feitu.repository;

import com.feitu.domain.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InviteCodeRepository extends JpaRepository<InviteCode, UUID> {
    Optional<InviteCode> findByCodeIgnoreCaseAndAtivoTrue(String code);
}
