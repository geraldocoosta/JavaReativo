package com.example.demo.publishers;

import com.example.demo.models.Payment;
import com.example.demo.models.PubSubMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Component
public record PaymentPublisher(Sinks.Many<PubSubMessage> sink, ObjectMapper mapper) {

	public Mono<Payment> onPaymentCreate(final Payment payment) {

		return Mono.fromCallable(() -> {
			final String userId = payment.getUserId();
			final String data = mapper.writeValueAsString(payment);
			return new PubSubMessage(userId, data);
		}).subscribeOn(Schedulers.parallel()).doOnNext(this.sink::tryEmitNext).thenReturn(payment);
	}

}
