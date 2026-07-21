package com.ogm.hrms.repository;

import com.ogm.hrms.entity.AccountToken;
import com.ogm.hrms.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {

    Optional<AccountToken> findByTokenHashAndTokenType(String tokenHash, TokenType tokenType);
}
