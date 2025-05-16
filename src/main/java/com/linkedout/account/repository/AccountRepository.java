package com.linkedout.account.repository;

import com.linkedout.common.model.entity.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<
	Account, Long> {

  /**
   * 이메일로 계정 조회
   *
   * @param email 조회할 계정 이메일
   * @return 해당 이메일의 계정 (Optional)
   */
  Mono<Account> findByEmail(String email);
}
