package com.linkedout.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedout.account.repository.AccountRepository;
import com.linkedout.common.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
	private final ObjectMapper objectMapper;
	private final AccountRepository accountRepository;

	public String test(){
		log.info("서비스로직 진입");
		return "ok";
	}

	public Optional<Account> findAccountByEmail(String email){
		return accountRepository.findByEmail(email);
	};
}
