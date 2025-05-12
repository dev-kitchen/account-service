package com.linkedout.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedout.account.repository.AccountRepository;
import com.linkedout.common.dto.account.AccountDTO;
import com.linkedout.common.dto.auth.oauth.google.GoogleUserInfo;
import com.linkedout.common.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
	private final ObjectMapper objectMapper;
	private final ModelMapper modelMapper;
	private final AccountRepository accountRepository;

	public String test(){
		log.info("서비스로직 진입");
		return "ok";
	}

	public AccountDTO findAccountByEmail(String email){
		return accountRepository.findByEmail(email).map(account -> modelMapper.map(account, AccountDTO.class)).orElse(null);
	}

	public Account createAccount(GoogleUserInfo userInfo) {
		Account account = Account.builder()
			.email(userInfo.getEmail())
			.name(userInfo.getName())
			.picture(userInfo.getPicture())
			.provider("google")
			.providerId(userInfo.getSub())
			.build();

		return accountRepository.save(account);
	}
}
