/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.advice;

import lombok.extern.slf4j.Slf4j;
import com.fvogel.broadcom.exception.ResourceNotFoundException;
import com.fvogel.broadcom.exception.UnprocessableEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of the GlobalExceptionHandler
 */
@Slf4j
class GlobalExceptionHandlerTests {

	private GlobalExceptionHandler exceptionHandlerUnderTest;

	@BeforeEach
	public void setUp() {
		exceptionHandlerUnderTest = new GlobalExceptionHandler();
	}

	@Test
	void whenUnprocessableEntityException_expectHttpStatusIsUnprocessableEntity() {
		// when
		var publisher = exceptionHandlerUnderTest
				.handleUnprocessableEntityException(new UnprocessableEntityException("test"));

		// then
		StepVerifier.create(publisher).expectSubscription().consumeNextWith(
				p -> assertThat(p.getStatus().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value()))
				.verifyComplete();
	}

	@Test
	void whenResourceNotFoundException_expectHttpStatusIsNotFound() {
		// when
		var publisher = exceptionHandlerUnderTest.handleResourceNotFound(new ResourceNotFoundException("test"));

		// then
		StepVerifier.create(publisher).expectSubscription()
				.consumeNextWith(p -> assertThat(p.getStatus().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value()))
				.verifyComplete();
	}

	@Test
	void whenNumberFormatExceptionException_expectHttpStatusIsUnprocessableEntity() {
		// when
		var publisher = exceptionHandlerUnderTest.handleNumberFormatException(new NumberFormatException("test"));

		// then
		StepVerifier.create(publisher).expectSubscription().consumeNextWith(
				p -> assertThat(p.getStatus().getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value()))
				.verifyComplete();
	}

	@Test
	void verifyHandleMethodReturnsNull() {
		var serverWebExchange = Mockito.mock(ServerWebExchange.class);
		var publisher = exceptionHandlerUnderTest.handle(serverWebExchange, new Throwable());

		assertThat(publisher).isNull();
	}
}