/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

import com.fvogel.broadcom.exception.*;
import com.fvogel.broadcom.common.ResourceIdentity;
import com.fvogel.broadcom.validation.OnCreate;
import com.fvogel.broadcom.validation.OnUpdate;
import com.fvogel.broadcom.validation.ResourceId;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.time.Duration;

@RestController
@RequestMapping("")
@Slf4j
public class UserController {

	private final UserService userService;

	/*
	 * Constructor
	 */
	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	/*
	 * Get all
	 */
	@GetMapping(value = UserRoutes.FIND_ALL_USER, produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<User> getAllUsers() {
		return userService.findAllUsers();
	}

	/*
	 * Get one by resourceId
	 *
	 */
	@GetMapping(value = UserRoutes.FIND_ONE_USER, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<User> getUserById(@PathVariable @ResourceId String id) {
		return userService.findUserByResourceId(id);
	}

	/**
	 * If api needs to push items as Streams to ensure Backpressure is applied, we
	 * need to set produces to MediaType.TEXT_EVENT_STREAM_VALUE
	 *
	 * MediaType.TEXT_EVENT_STREAM_VALUE is the official media type for Server Sent
	 * Events (SSE) MediaType.APPLICATION_STREAM_JSON_VALUE is for server to
	 * server/http client communications.
	 *
	 * https://stackoverflow.com/questions/52098863/whats-the-difference-between-text-event-stream-and-application-streamjson
	 *
	 */
	@GetMapping(value = UserRoutes.STREAM_USER, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public Flux<User> getUserStream() {
		// This is only an example implementation. Modify this line as needed.
		return userService.findAllUsers().delayElements(Duration.ofMillis(250));
	}

	/*
	 * Create
	 */
	@PostMapping(value = UserRoutes.CREATE_USER, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<ResponseEntity<ResourceIdentity>> createUser(@RequestBody @Validated(OnCreate.class) User resource) {
		Mono<String> id = userService.createUser(resource);
		return id.map(value -> ResponseEntity.status(HttpStatus.CREATED).body(new ResourceIdentity(value)));
	}

	/*
	 * Update by resourceId
	 */
	@PutMapping(value = UserRoutes.UPDATE_USER, produces = MediaType.APPLICATION_JSON_VALUE)
	public void updateUser(@PathVariable @ResourceId String id, @RequestBody @Validated(OnUpdate.class) User user) {
		if (!Objects.equals(id, user.getResourceId())) {
			log.error("Update declined: mismatch between query string identifier, {}, and resource identifier, {}", id,
					user.getResourceId());
			throw new UnprocessableEntityException("Mismatch between the identifiers in the URI and the payload");
		}
		userService.updateUser(user);
	}

	/*
	 * Delete one
	 */
	@DeleteMapping(value = UserRoutes.DELETE_USER)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable @ResourceId String id) {
		Mono<User> resource = userService.findUserByResourceId(id);
		resource.subscribe(value -> userService.deleteUserByResourceId(id));
	}
}