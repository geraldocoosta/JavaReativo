package com.example.demo.publishers;

import com.example.demo.models.Payment;
import com.example.demo.models.PubSubMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Component
public class PaymentPublisher {

	private final Sinks.Many<PubSubMessage> sink;
	private final ObjectMapper mapper;

	public Mono<Payment> onPaymentCreate(final Payment payment) {
		return Mono.fromCallable(() -> {
			String userId = payment.getUserId();
			String data = mapper.writeValueAsString(payment);
			return new PubSubMessage(userId, data);
		})
				.subscribeOn(Schedulers.parallel())
				.doOnNext(this.sink::tryEmitNext)
				.thenReturn(payment);
	}

}
