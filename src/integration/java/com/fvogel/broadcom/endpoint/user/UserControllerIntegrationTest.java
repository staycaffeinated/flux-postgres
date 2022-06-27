/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcom.endpoint.user;

import com.fvogel.broadcom.common.ResourceIdentity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests of UserController
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

	@LocalServerPort
	int port;
	@Value("${spring.webflux.base-path}")
	String applicationBasePath;
	private WebTestClient client;
	private User user;

	@Autowired
	private UserService userService;

	@Autowired
	public void setApplicationContext(ApplicationContext context) {
		this.client = WebTestClient.bindToApplicationContext(context).configureClient().build();
	}

	@Test
	void testGetAllUsers() {
		this.client.get().uri(UserRoutes.FIND_ALL_USER).accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isOk().expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.[0].text")
				.isNotEmpty().jsonPath("$.[0].resourceId").isNotEmpty();
	}

	@Test
	void testGetSingleUser() {
		createUser();

		this.client.get().uri(replaceId(UserRoutes.FIND_ONE_USER)).accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$.resourceId").isNotEmpty().jsonPath("$.text").isNotEmpty();
	}

	@Test
	void testGetCatalogItemsStream() throws Exception {
		FluxExchangeResult<User> result = this.client.get().uri(UserRoutes.STREAM_USER)
				.accept(MediaType.TEXT_EVENT_STREAM).exchange().expectStatus().isOk().returnResult(User.class);

		Flux<User> events = result.getResponseBody();

		StepVerifier.create(events).expectSubscription().expectNextMatches(p -> p.getResourceId() != null)
				.expectNextMatches(p -> p.getResourceId() != null).expectNextMatches(p -> p.getResourceId() != null)
				.thenCancel().verify();
	}

	@Test
	void testCreateUser() {
		User user = UserGenerator.generateUser();
		user.setResourceId(null);

		this.client.post().uri(UserRoutes.CREATE_USER).contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(user), User.class).exchange().expectStatus().isCreated().expectHeader()
				.contentType(MediaType.APPLICATION_JSON);
	}

	@Test
	void testUpdateUser() {
		createUser();

		user.setText("my new text");

		this.client.put().uri(replaceId(UserRoutes.UPDATE_USER)).contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(user), User.class).exchange().expectStatus().isOk();
	}

	@Test
	void testDeleteUser() {
		createUser();

		this.client.delete().uri(replaceId(UserRoutes.DELETE_USER)).exchange().expectStatus().isNoContent();
	}

	@Test
	void testResourceNotFoundException() throws Exception {
		this.client.get().uri(UserRoutes.FIND_ONE_USER.replaceAll("\\{id\\}", "12345"))
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound().expectHeader()
				.contentType(MediaType.APPLICATION_JSON);
	}

	/**
	 * Creates a new User then updates the resourceId of the instance variable,
	 * user, with the resourceId of the added User.
	 */
	void createUser() {
		user = UserGenerator.generateUser();
		user.setResourceId(null);

		EntityExchangeResult<ResourceIdentity> result = this.client.post().uri(UserRoutes.CREATE_USER)
				.contentType(MediaType.APPLICATION_JSON).body(Mono.just(user), User.class).exchange().expectStatus()
				.isCreated().expectBody(ResourceIdentity.class).returnResult();

		// After the user is created, the endpoint returns the resourceId of the
		// created book. Here, the resourceId of the instance variable, user, is updated
		// to enable the current test to acquire the new User's resourceId.
		String resourceId = result.getResponseBody().getResourceId();
		user.setResourceId(resourceId);
	}

	/**
	 * Use this to replace the 'id' parameter in the query string with the
	 * resourceId from the instance variable, user
	 */
	String replaceId(String path) {
		return path.replaceAll("\\{id\\}", user.getResourceId().toString());
	}
}