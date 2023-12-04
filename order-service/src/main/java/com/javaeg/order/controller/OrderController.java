package com.javaeg.order.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.javaeg.order.service.OrderService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class OrderController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private OrderService orderService;

	@GetMapping("/order")
	@CircuitBreaker(name = "payment-circuit-breaker", fallbackMethod = "paymentCircuitBreaker")
	public Order getOrder(@RequestParam String orderItem, @RequestParam Float amount, String customerId) {
		log.info("Order received.");
		ResponseEntity<String> response = restTemplate
				.getForEntity("http://PAYMENT-SERVICE/payment?amount=" + amount.floatValue(), String.class);
		log.info(response.getBody());
		orderService.processOrder(orderItem, amount, customerId);
		return new Order(String.format("Order for the item %s has been processed with order-id %s", orderItem,
				UUID.randomUUID()));
	}

	public Order paymentCircuitBreaker(String orderItem, Float amount, String customerId, Throwable t) {
		log.info("paymentCircuitBreaker called");
		log.info("orderItem {}", orderItem);
		log.info("amount {}", amount);
		log.info("customerId {}", customerId);
		log.info("exp {}", t.getMessage());

		return new Order("Payment failed, please try after sometime");
	}

	record Order(String orderDesc) {
	}
}
