/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

import com.fvogel.broadcom.math.SecureRandomSeries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Unit test the UserController
 */
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserController.class)
class UserControllerTests {

	@MockBean
	private UserService mockUserService;

	@Autowired
	private WebTestClient webClient;
	@Value("${spring.webflux.base-path}")
	String applicationBasePath;

	final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@Autowired
	public void setApplicationContext(ApplicationContext context) {
		webClient = WebTestClient.bindToApplicationContext(context).configureClient().build();
	}

	@Test
	void shouldGetOneUser() {
		final String expectedResourceID = randomSeries.nextResourceId();
		User pojo = User.builder().text("testGetOne").resourceId(expectedResourceID).build();
		UserEntityBean ejb = UserEntityBean.builder().resourceId(expectedResourceID).text("testGetOne").build();

		when(mockUserService.findByResourceId(expectedResourceID)).thenReturn(Mono.just(ejb));
		when(mockUserService.findUserByResourceId(expectedResourceID)).thenReturn(Mono.just(pojo));

		webClient.get().uri(UserRoutes.FIND_ONE_USER, expectedResourceID).accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isOk().expectBody().jsonPath("$.resourceId").isNotEmpty().jsonPath("$.text")
				.isNotEmpty();

		Mockito.verify(mockUserService, times(1)).findUserByResourceId(expectedResourceID);
	}

	@Test
	void shouldGetAllUsers() {
		List<User> list = createUserList();
		Flux<User> flux = Flux.fromIterable(list);

		when(mockUserService.findAllUsers()).thenReturn(flux);

		webClient.get().uri(UserRoutes.FIND_ALL_USER).accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isOk().expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.[0].text")
				.isNotEmpty().jsonPath("$.[0].resourceId").isNotEmpty();
	}

	@Test
	void shouldCreateUser() {
		User pojo = createUser();
		pojo.setResourceId(null);
		String expectedId = randomSeries.nextResourceId();

		when(mockUserService.createUser(any(User.class))).thenReturn(Mono.just(expectedId));

		webClient.post().uri(UserRoutes.CREATE_USER).contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(pojo), User.class).exchange().expectStatus().isCreated().expectHeader()
				.contentType(MediaType.APPLICATION_JSON);
	}

	@Test
	void shouldUpdateUser() {
		User pojo = createUser();
		webClient.put().uri(UserRoutes.UPDATE_USER, pojo.getResourceId()).contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(pojo), User.class).exchange().expectStatus().isOk();
	}

	@Test
	void whenMismatchOfResourceIds_expectUnprocessableEntityException() {
		// Given
		User pojo = createUser();
		String idInBody = randomSeries.nextResourceId();
		String idInParameter = randomSeries.nextResourceId();
		pojo.setResourceId(idInBody);

		// when the ID in the URL is a mismatch to the ID in the POJO, the request
		// should fail
		webClient.put().uri(UserRoutes.UPDATE_USER, idInParameter).contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(pojo), User.class).exchange().expectStatus().is4xxClientError();
	}

	@Test
	void shouldDeleteUser() {
		User pojo = createUser();
		when(mockUserService.findUserByResourceId(pojo.getResourceId())).thenReturn(Mono.just(pojo));

		webClient.delete().uri(UserRoutes.DELETE_USER, pojo.getResourceId()).exchange().expectStatus().isNoContent();
	}

	@Test
	void shouldGetUsersAsStream() throws Exception {
		// Given
		List<User> resourceList = createUserList();
		given(mockUserService.findAllUsers()).willReturn(Flux.fromIterable(resourceList));

		// When
		FluxExchangeResult<User> result = webClient.get().uri(UserRoutes.STREAM_USER)
				.accept(MediaType.TEXT_EVENT_STREAM).exchange().expectStatus().isOk().returnResult(User.class);

		// Then
		Flux<User> events = result.getResponseBody();
		StepVerifier.create(events).expectSubscription().consumeNextWith(p -> {
			assertThat(p.getResourceId()).isNotNull();
			assertThat(p.getText()).isNotEmpty();
		}).consumeNextWith(p -> {
			assertThat(p.getResourceId()).isNotNull();
			assertThat(p.getText()).isNotEmpty();
		}).thenCancel().verify();
	}

	/**
	 * Generates a list of sample test data
	 */
	private List<User> createUserList() {
		User w1 = User.builder().resourceId(randomSeries.nextResourceId()).text("Lorim ipsum dolor imit").build();
		User w2 = User.builder().resourceId(randomSeries.nextResourceId()).text("Hodor Hodor Hodor Hodor").build();
		User w3 = User.builder().resourceId(randomSeries.nextResourceId()).text("Now is the time to fly").build();

		ArrayList<User> list = new ArrayList<>();
		list.add(w1);
		list.add(w2);
		list.add(w3);

		return list;
	}

	/**
	 * Generates a single test item
	 */
	private User createUser() {
		return User.builder().resourceId(randomSeries.nextResourceId()).text("Duis aute irure dolor in reprehenderit.")
				.build();
	}
}
