package com.example.demo.listeners;

import com.example.demo.models.Payment;
import com.example.demo.models.Payment.PaymentStatus;
import com.example.demo.models.PubSubMessage;
import com.example.demo.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import reactor.core.publisher.Sinks;

@RequiredArgsConstructor
@Log4j2
public class PaymentListener implements InitializingBean {
	private final Sinks.Many<PubSubMessage> sink;
	private final PaymentRepository paymentRepository;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.sink.asFlux()
				.subscribe(
						next -> {
							log.info("On next message");
							this.paymentRepository.processPayment(next.getKey(), PaymentStatus.APPROVED);
						},
						error -> log.info("On pub-sub listener observe error", error),
						() -> log.info("On pub-sub listener complete")

				);
	}
}
