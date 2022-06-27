/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

import com.fvogel.broadcom.exception.ResourceNotFoundException;
import com.fvogel.broadcom.exception.UnprocessableEntityException;
import com.fvogel.broadcom.math.SecureRandomSeries;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.fvogel.broadcom.math.SecureRandomSeries;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests of the User service
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unused"})
class UserServiceTests {
	@Mock
	private UserRepository mockRepository;

	@Mock
	private ApplicationEventPublisher publisher;

	@Mock
	private SecureRandomSeries mockSecureRandom;

	@InjectMocks
	private UserService serviceUnderTest;

	@Spy
	private final ConversionService conversionService = FakeConversionService.build();

	final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@Test
	void shouldFindAllUsers() {
		Flux<UserEntityBean> ejbList = convertToFlux(createUserList());
		given(mockRepository.findAll()).willReturn(ejbList);

		Flux<User> stream = serviceUnderTest.findAllUsers();

		StepVerifier.create(stream).expectSubscription().expectNextCount(3).verifyComplete();
	}

	@Test
	void shouldFindUserByResourceId() {
		// Given
		UserEntityBean expectedEJB = createUser();
		String expectedId = randomSeries.nextResourceId();
		expectedEJB.setResourceId(expectedId);
		Mono<UserEntityBean> rs = Mono.just(expectedEJB);
		given(mockRepository.findByResourceId(any(String.class))).willReturn(rs);

		// When
		Mono<User> publisher = serviceUnderTest.findUserByResourceId(expectedId);

		// Then
		StepVerifier.create(publisher).expectSubscription()
				.consumeNextWith(item -> assertThat(Objects.equals(item.getResourceId(), expectedId))).verifyComplete();
	}

	@Test
	void shouldFindAllByText() {
		// Given
		final String expectedText = "Lorim ipsum";
		List<UserEntityBean> expectedRows = createUserListHavingSameTextValue(expectedText);
		given(mockRepository.findAllByText(expectedText)).willReturn(Flux.fromIterable(expectedRows));

		// When
		Flux<User> publisher = serviceUnderTest.findAllByText(expectedText);

		// Then
		StepVerifier.create(publisher).expectSubscription()
				.consumeNextWith(item -> assertThat(Objects.equals(item.getText(), expectedText)))
				.consumeNextWith(item -> assertThat(Objects.equals(item.getText(), expectedText)))
				.consumeNextWith(item -> assertThat(Objects.equals(item.getText(), expectedText))).verifyComplete();
	}

	@Test
	void shouldCreateUser() {
		// Given
		String expectedResourceId = randomSeries.nextResourceId();
		// what the client submits to the service
		User expectedPOJO = User.builder().text("Lorim ipsum dolor amount").build();
		// what the persisted version looks like
		UserEntityBean persistedObj = conversionService.convert(expectedPOJO, UserEntityBean.class);
		persistedObj.setResourceId(expectedResourceId);
		persistedObj.setId(1L);
		given(mockRepository.save(any(UserEntityBean.class))).willReturn(Mono.just(persistedObj));

		// When
		Mono<String> publisher = serviceUnderTest.createUser(expectedPOJO);

		// Then
		StepVerifier.create(publisher.log("testCreate : ")).expectSubscription()
				.consumeNextWith(item -> assertThat(Objects.equals(item, expectedResourceId))).verifyComplete();

	}

	@Test
	void shouldUpdateUser() {
		// Given
		// what the client submits
		User submittedPOJO = User.builder().text("Updated value").resourceId(randomSeries.nextResourceId()).build();
		// what the new persisted value looks like
		UserEntityBean persistedObj = conversionService.convert(submittedPOJO, UserEntityBean.class);
		Mono<UserEntityBean> dataStream = Mono.just(persistedObj);
		given(mockRepository.findByResourceId(any(String.class))).willReturn(dataStream);
		given(mockRepository.save(persistedObj)).willReturn(dataStream);

		// When
		serviceUnderTest.updateUser(submittedPOJO);

		// Then
		// verify publishEvent was invoked
		verify(publisher, times(1)).publishEvent(any());
	}

	@Test
	void shouldDeleteUser() {
		String deletedId = randomSeries.nextResourceId();
		// The repository returns 1, to indicate 1 row was deleted
		given(mockRepository.deleteByResourceId(deletedId)).willReturn(Mono.just(1L));

		serviceUnderTest.deleteUserByResourceId(deletedId);

		verify(publisher, times(1)).publishEvent(any());
	}

	@Test
	void whenDeleteNullUser_expectNullPointerException() {
		assertThrows(NullPointerException.class, () -> serviceUnderTest.deleteUserByResourceId((String) null));
	}

	@Test
	void whenFindNonExistingEntity_expectResourceNotFoundException() {
		given(mockRepository.findByResourceId(any())).willReturn(Mono.empty());

		Mono<UserEntityBean> publisher = serviceUnderTest.findByResourceId(randomSeries.nextResourceId());

		StepVerifier.create(publisher).expectSubscription().expectError(ResourceNotFoundException.class).verify();
	}

	@Test
	void whenUpdateOfNullUser_expectNullPointerException() {
		assertThrows(NullPointerException.class, () -> serviceUnderTest.updateUser(null));
	}

	@Test
	void whenFindAllByNullText_expectNullPointerException() {
		assertThrows(NullPointerException.class, () -> serviceUnderTest.findAllByText(null));
	}

	@Test
	void whenCreateNullUser_expectNullPointerException() {
		assertThrows(NullPointerException.class, () -> serviceUnderTest.createUser(null));
	}

	/**
	 * Per its API, a ConversionService::convert method _could_ return null. The
	 * scope of this test case is to verify our own code's behavior should a null be
	 * returned. In this case, an UnprocessableEntityException is thrown.
	 */
	@Test
	void whenConversionToEjbFails_expectUnprocessableEntityException() {
		// given
		ConversionService mockConversionService = Mockito.mock(ConversionService.class);
		UserService localService = new UserService(mockRepository, mockConversionService, publisher,
				new SecureRandomSeries());
		given(mockConversionService.convert(any(User.class), eq(UserEntityBean.class)))
				.willReturn((UserEntityBean) null);

		User sample = User.builder().text("sample").build();

		// when/then
		assertThrows(UnprocessableEntityException.class, () -> localService.createUser(sample));
	}

	// -----------------------------------------------------------
	// Helper methods
	// -----------------------------------------------------------

	private Flux<UserEntityBean> convertToFlux(List<UserEntityBean> list) {
		return Flux.fromIterable(createUserList());
	}

	private List<User> convertToPojo(List<UserEntityBean> list) {
		return list.stream().map(item -> conversionService.convert(item, User.class)).collect(Collectors.toList());
	}

	private List<UserEntityBean> createUserList() {
		UserEntityBean w1 = UserEntityBean.builder().resourceId(randomSeries.nextResourceId())
				.text("Lorim ipsum dolor imit").build();
		UserEntityBean w2 = UserEntityBean.builder().resourceId(randomSeries.nextResourceId())
				.text("Duis aute irure dolor in reprehenderit").build();
		UserEntityBean w3 = UserEntityBean.builder().resourceId(randomSeries.nextResourceId())
				.text("Excepteur sint occaecat cupidatat non proident").build();

		ArrayList<UserEntityBean> dataList = new ArrayList<>();
		dataList.add(w1);
		dataList.add(w2);
		dataList.add(w3);

		return dataList;
	}

	private List<UserEntityBean> createUserListHavingSameTextValue(final String value) {
		UserEntityBean w1 = UserEntityBean.builder().resourceId(randomSeries.nextResourceId()).text(value).build();
		UserEntityBean w2 = UserEntityBean.builder().resourceId(randomSeries.nextResourceId()).text(value).build();
		UserEntityBean w3 = UserEntityBean.builder().resourceId(randomSeries.nextResourceId()).text(value).build();

		ArrayList<UserEntityBean> dataList = new ArrayList<>();
		dataList.add(w1);
		dataList.add(w2);
		dataList.add(w3);

		return dataList;
	}

	private UserEntityBean createUser() {
		return UserEntityBean.builder().resourceId(randomSeries.nextResourceId()).text("Lorim ipsum dolor imit")
				.build();
	}
}