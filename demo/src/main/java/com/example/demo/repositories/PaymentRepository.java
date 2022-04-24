package com.example.demo.repositories;

import com.example.demo.models.Payment;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
@Log4j2
public record PaymentRepository(Database database) {

	private static final ThreadFactory THREAD_FACTORY = new CustomizableThreadFactory("database-");
	private static final Scheduler DB_SHEDULER = Schedulers.fromExecutor(Executors.newFixedThreadPool(16, THREAD_FACTORY));

	public Mono<Payment> createPayment(final String userId) {

		final Payment payment = Payment.builder().id(UUID.randomUUID().toString()).userId(userId).status(Payment.PaymentStatus.PENDING)
				.build();

		return Mono.fromCallable(() -> {
			log.info("Saving payment transaction for user {}", userId);
			return this.database.save(userId, payment);
		})
				.delayElement(Duration.ofMillis(20))
				.subscribeOn(DB_SHEDULER)
				.doOnNext(next -> log.info("Payment received {}", next.getUserId()));

	}

	public Mono<Payment> getPayment(final String userId) {

		return Mono.defer(() -> {
			log.info("Getting payment from database - {}", userId);
			Optional<Payment> payment = this.database.get(userId, Payment.class);
			return Mono.justOrEmpty(payment);
		}).subscribeOn(DB_SHEDULER).doOnNext(it -> log.info("Payment received - {}", userId));
	}

	public Mono<Payment> processPayment(final String key, final Payment.PaymentStatus status) {

		log.info("On payment {} received to status {}", key, status);
		return getPayment(key).flatMap(payment -> Mono.fromCallable(() ->
				this.database.save(key, payment.withStatus(status)))
				.delayElement(Duration.ofMillis(20))
				.subscribeOn(DB_SHEDULER));
	}
}
