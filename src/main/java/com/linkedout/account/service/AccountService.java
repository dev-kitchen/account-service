package com.linkedout.account.service;

import com.linkedout.common.model.dto.account.AccountDTO;
import com.linkedout.common.model.dto.auth.oauth.google.GoogleUserInfoDTO;
import reactor.core.publisher.Mono;

public interface AccountService {
	 Mono<AccountDTO> findAccountById(Long id);

	 Mono<AccountDTO> findAccountByEmail(String email);

	 Mono<AccountDTO> createAccount(GoogleUserInfoDTO userInfo);
}
