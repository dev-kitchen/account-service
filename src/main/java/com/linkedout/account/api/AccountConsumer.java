package com.linkedout.account.api;

import com.linkedout.account.service.AccountService;
import com.linkedout.common.constant.RabbitMQConstants;
import com.linkedout.common.dto.ApiRequestData;
import com.linkedout.common.dto.ApiResponseData;
import com.linkedout.common.dto.ServiceMessageDTO;
import com.linkedout.common.exception.BaseException;
import com.linkedout.common.exception.ErrorResponseBuilder;
import com.linkedout.common.messaging.ServiceIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountConsumer {

	private final RabbitTemplate rabbitTemplate;
	private final AccountService accountService;
	private final ErrorResponseBuilder errorResponseBuilder;
	private final ServiceIdentifier serviceIdentifier;


	@RabbitListener(queues = RabbitMQConstants.ACCOUNT_API_QUEUE)
	public void processApiRequest(ApiRequestData request, @Header(AmqpHeaders.CORRELATION_ID) String correlationId) {
		log.info("받은 요청: {}, correlationId: {}", request, correlationId);

		ApiResponseData response = new ApiResponseData();
		response.setCorrelationId(correlationId);
		response.setHeaders(new HashMap<>());

		// 요청 처리를 Mono로 래핑
		Mono.fromCallable(() -> {
				try {
					String path = request.getPath();
					String method = request.getMethod();
					String requestKey = method + " " + path;

					switch (requestKey) {
//						case "GET /api/auth/test" -> authService.test(request, response);
						default -> errorResponseBuilder.populateErrorResponse(response, 404, "요청을 처리할 수 없습니다: " + requestKey);
					}
				} catch (BaseException ex) {
					errorResponseBuilder.populateErrorResponse(response, ex.getStatusCode(), ex.getMessage());
				}
				return response;
			})
			.subscribeOn(Schedulers.boundedElastic())  // IO 작업은 boundedElastic 스케줄러에서 실행
			.subscribe(completedResponse -> {
				// 응답 전송
				rabbitTemplate.convertAndSend(
					RabbitMQConstants.API_EXCHANGE,
					RabbitMQConstants.API_GATEWAY_ROUTING_KEY,
					completedResponse
				);
				log.info("응답 전송: {}", completedResponse);
			});
	}

	/**
	 * 다른 서비스로부터의 메시지 요청 처리
	 * 이 큐는 auth 서비스가 다른 서비스로부터 메시지를 받는 큐
	 */
	@RabbitListener(queues = RabbitMQConstants.ACCOUNT_SERVICE_CONSUMER_QUEUE)
	public void processServiceRequest(ServiceMessageDTO<?> requestMessage) {
		String correlationId = requestMessage.getCorrelationId();
		String operation = requestMessage.getOperation();
		String senderService = requestMessage.getSenderService();
		String replyTo = requestMessage.getReplyTo();

		log.info("서비스 요청 수신: correlationId={}, operation={}, sender={}, replyTo={}",
			correlationId, operation, senderService, replyTo);

		// 요청 처리를 Mono로 래핑
		Mono.fromCallable(() -> {
				ServiceMessageDTO<Object> response = ServiceMessageDTO.builder()
					.correlationId(correlationId)
					.senderService(serviceIdentifier.getServiceName())
					.operation(operation + "Response")
					.build();

				try {
					// 작업 타입에 따른 처리 분기
					Object result = switch (operation) {
						case "test" -> accountService.test();
						// case "createAccount" -> handleCreateAccount(requestMessage);
						// case "validateToken" -> handleValidateToken(requestMessage);
						// case "refreshToken" -> handleRefreshToken(requestMessage);
						default -> throw new UnsupportedOperationException("지원하지 않는 작업: " + operation);
					};

					log.info("서비스로직 완료, 응답생성. 결과: {}", result);

					// 응답에 결과 설정
					response.setPayload(result);

				} catch (Exception e) {
					log.error("서비스 요청 처리 오류: operation={}, error={}", operation, e.getMessage(), e);
					// 오류 응답 설정
					response.setError(e.getMessage());
				}

				return response;
			})
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe(response -> {
				// 응답 메시지 전송
				log.info("서비스 응답 전송: correlationId={}, replyTo={}, 응답타입={}",
					correlationId, replyTo, (response.getError() != null ? "오류" : "성공"));

				rabbitTemplate.convertAndSend(
					RabbitMQConstants.SERVICE_EXCHANGE,
					replyTo,  // 요청의 replyTo 필드 사용
					response
				);

				log.info("서비스 응답 전송 완료: correlationId={}", correlationId);
			}, error -> {
				log.error("서비스 응답 생성 실패: correlationId={}, error={}",
					correlationId, error.getMessage(), error);

				// 오류 응답 생성 및 전송
				ServiceMessageDTO<Object> errorResponse = ServiceMessageDTO.builder()
					.correlationId(correlationId)
					.senderService(serviceIdentifier.getServiceName())
					.operation(operation + "Response")
					.error("내부 서버 오류: " + error.getMessage())
					.build();

				rabbitTemplate.convertAndSend(
					RabbitMQConstants.SERVICE_EXCHANGE,
					replyTo,
					errorResponse
				);
			});
	}
}