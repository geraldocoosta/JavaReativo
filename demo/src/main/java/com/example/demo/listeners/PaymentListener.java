package com.example.demo.listeners;

import com.example.demo.models.Payment.PaymentStatus;
import com.example.demo.models.PubSubMessage;
import com.example.demo.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentListener implements InitializingBean {

	private final Sinks.Many<PubSubMessage> sink;
	private final PaymentRepository paymentRepository;

	@Override
	public void afterPropertiesSet() {

		this.sink.asFlux()
				.delayElements(Duration.ofSeconds(2))
				.subscribe(next -> {
					log.info("On next message");
					this.paymentRepository
							.processPayment(next.getKey(), PaymentStatus.APPROVED)
							.doOnNext(it -> log.info("Payment processed on listener"))
							.subscribe();
				},
				error -> log.info("On pub-sub listener observe error", error),
				() -> log.info("On pub-sub listener complete")
		);
	}
}