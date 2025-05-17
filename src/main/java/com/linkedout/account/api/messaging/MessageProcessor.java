package com.linkedout.account.api.messaging;

import com.linkedout.account.service.AccountService;
import com.linkedout.common.model.dto.ServiceMessageDTO;
import com.linkedout.common.model.dto.auth.AuthenticationDTO;
import com.linkedout.common.model.dto.auth.oauth.google.GoogleUserInfoDTO;
import com.linkedout.common.util.converter.PayloadConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProcessor {
	private final AccountService accountService;
	private final PayloadConverter payloadConverter;


	public Mono<?> processOperation(String operation, ServiceMessageDTO<?> requestMessage, AuthenticationDTO accountInfo) {
		return switch (operation) {
			case "getFindById" -> {
				Long id = payloadConverter.convert(requestMessage.getPayload(), Long.class);
				yield accountService.findAccountById(id);
			}
			case "getFindByEmail" -> {
				String email = payloadConverter.convert(requestMessage.getPayload(), String.class);
				yield accountService.findAccountByEmail(email);
			}
			case "postCreateAccount" -> {
				GoogleUserInfoDTO userInfo =
					payloadConverter.convert(requestMessage.getPayload(), GoogleUserInfoDTO.class);
				yield accountService.createAccount(userInfo);
			}
			default -> Mono.error(new UnsupportedOperationException("지원하지 않는 작업: " + operation));
		};
	}
}