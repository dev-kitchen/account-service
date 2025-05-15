package com.linkedout.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedout.account.entity.Account;
import com.linkedout.common.model.dto.account.AccountDTO;
import com.linkedout.common.model.dto.auth.oauth.google.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.linkedout.account.repository.AccountRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
  private final ObjectMapper objectMapper;
  private final ModelMapper modelMapper;
  private final AccountRepository accountRepository;

  public Mono<String> test() {
    log.info("서비스로직 진입");
    return Mono.just("ok");
  }

  public Mono<AccountDTO> findAccountByEmail(String email) {
    return accountRepository
        .findByEmail(email)
        .mapNotNull(
            account -> modelMapper.map(account, AccountDTO.class))
        .switchIfEmpty(Mono.empty());
  }

  public Mono<AccountDTO> createAccount(GoogleUserInfo userInfo) {
    Account account =
        Account.builder()
            .email(userInfo.getEmail())
            .name(userInfo.getName())
            .picture(userInfo.getPicture())
            .provider("google")
            .providerId(userInfo.getSub())
            .build();

    log.info("{}", account);

    return accountRepository
        .save(account)
        .doOnNext(saved -> log.info("저장 후 계정: {}", saved))
        .doOnError(e -> log.error("저장 중 오류: {}", e.getMessage(), e))
        .map(savedAccount -> modelMapper.map(savedAccount, AccountDTO.class));
  }
}
