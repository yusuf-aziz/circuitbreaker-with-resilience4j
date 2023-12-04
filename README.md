# Circuitbreaker with resilience4j
**Resilience4j's** Circuit Breaker - a powerful library that protects our application from potential failures!

## What's It All About?

- **Circuit Breaker Magic:** Imagine a Circuit Breaker like the one in our home - it stops the flow of electricity during an overload. Resilience4j's Circuit Breaker does the same for our code, preventing system failures during service outages.

- **How It Works:** This tool monitors external services and stops sending requests when they're acting up. This helps prevent the entire system from crashing, giving it time to recover.

## Why Use Resilience4j's Circuit Breaker?

- **Boost Reliability:** Prevents system-wide failures when services misbehave, ensuring our app remains stable.

- **Easy Integration:** Simple to integrate into our code, making our applications more resilient with just a few configurations.

- **Smooth Recovery:** Automatically retries requests after a cooldown period, allowing the system to recover gracefully.

## Let's try the demo
In this example, we are working with two microservices: the Order-Service manages customer orders, while the Payment-Service handles payment processing. 

Imagine if our payment service encounters an issue. How should our system respond, and how does the Circuit-Breaker pattern step in to stop the entire system from collapsing, allowing it the space to recover.

1. Clone this repository:

```
git clone https://github.com/yusuf-aziz/observability-with-micrometer-and-zipkin.git
```
2. Navigate to the cloned repository:

```
cd observability-with-micrometer-and-zipkin
```
3. Run mvn clean install:

```
mvn clean install
```
4. Run the Eureka server:

```
java -jar eureka-server-0.0.1-SNAPSHOT.jar
```

5. Run the Order service:

```
java -jar order-service-0.0.1-SNAPSHOT.jar
```
6. Run the Payment service:

```
java -jar payment-service-0.0.1-SNAPSHOT.jar
```

![alt text](https://github.com/yusuf-aziz/circuitbreaker-with-resilience4j/blob/main/eureka-server-img.png?raw=true)

7. Open a web browser and try:

```
http://localhost:8090/order?orderItem=pizza&amount=2.4&customerId=test

```
**Response:**
{"orderDesc":"Order for the item pizza has been processed with order-id f672e1e1-780b-4e43-aaa0-9c3e33a4c160"}


## Let's close the Payement-Service and try the same endpoint in browser

```
http://localhost:8090/order?orderItem=pizza&amount=2.4&customerId=test

```
**Response:**
{"orderDesc":"Payment failed, please try after sometime"}

Congratulations! The circuit breaker is working. Instead of getting an internal server error, we received a message from the fallback method we set up.

```
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
		return new Order("Payment failed, please try after sometime");
	}
```

**Please note, the fallback method must return the same type as the original method and have same method signature. Otherwise, the FallbackExecutor won't locate a compatible fallback method and will throw a NoSuchMethodException.**



