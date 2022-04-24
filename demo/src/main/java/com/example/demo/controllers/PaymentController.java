package com.example.demo.controllers;

import com.example.demo.models.Payment;
import com.example.demo.publishers.PaymentPublisher;
import com.example.demo.repositories.InMemoryDatabase;
import com.example.demo.repositories.PaymentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;

@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
@Log4j2
public class PaymentController {

	private final PaymentRepository paymentRepository;
	private final PaymentPublisher paymentPublisher;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Payment> createPayment(@RequestBody final NewPaymentInput input) {

		String userId = input.getUserId();

		log.info("Payment to be processed {}", userId);
		return this.paymentRepository.createPayment(userId)
				.flatMap(this.paymentPublisher::onPaymentCreate)
				.flatMap(
						payment -> Flux.interval(Duration.ofSeconds(1))
								.doOnNext(it -> log.info("Next tick - {}", it))
								.flatMap(tick -> this.paymentRepository.getPayment(userId))
								.filter(it -> Payment.PaymentStatus.APPROVED == it.getStatus()).next())
				.doOnNext(next -> log.info("Payment processed {}", userId))
				.timeout(Duration.ofSeconds(20))
				.retryWhen(
						Retry.backoff(2, Duration.ofSeconds(1))
								.doAfterRetry(signal -> log.info("Execution failed... retrying {}", signal.totalRetries())));
	}
	
	@GetMapping("/users")
	public Flux<Payment> findAllById(@RequestParam String ids) {
		final var idsList = Arrays.asList(ids.split(","));
		log.info("Collecting {} payments", idsList.size());
		return Flux.fromIterable(idsList).flatMap(this.paymentRepository::getPayment);
	}


	@GetMapping("/ids")
	public Mono<String> getIds() {
		return Mono.fromCallable(() -> String.join(",", InMemoryDatabase.DATABASE.keySet()))
				.subscribeOn(Schedulers.parallel());
	}

	@Data
	public static class NewPaymentInput {

		private String userId;
	}
}
