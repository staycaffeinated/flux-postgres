/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcom.endpoint.user;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import com.fvogel.broadcom.math.SecureRandomSeries;
import com.fvogel.broadcom.validation.OnCreate;
import com.fvogel.broadcom.validation.OnUpdate;
import com.fvogel.broadcom.exception.ResourceNotFoundException;
import com.fvogel.broadcom.exception.UnprocessableEntityException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserService {

	private final ApplicationEventPublisher publisher;
	private final ConversionService conversionService;
	private final UserRepository repository;
	private final SecureRandomSeries secureRandom;

	/*
	 * Constructor
	 */
	@Autowired
	public UserService(UserRepository userRepository, @Qualifier("userConverter") ConversionService conversionService,
			ApplicationEventPublisher publisher, SecureRandomSeries secureRandom) {
		this.repository = userRepository;
		this.conversionService = conversionService;
		this.publisher = publisher;
		this.secureRandom = secureRandom;
	}

	/*
	 * findAll
	 */
	public Flux<User> findAllUsers() {
		return Flux.from(repository.findAll().map(ejb -> conversionService.convert(ejb, User.class)));
	}

	/**
	 * findByResourceId
	 */
	public Mono<User> findUserByResourceId(String id) throws ResourceNotFoundException {
		Mono<UserEntityBean> monoItem = findByResourceId(id);
		return monoItem.flatMap(it -> Mono.just(conversionService.convert(it, User.class)));
	}

	/*
	 * findAllByText
	 */
	public Flux<User> findAllByText(@NonNull String text) {
		return Flux.from(repository.findAllByText(text).map(ejb -> conversionService.convert(ejb, User.class)));
	}

	/**
	 * Create
	 */
	public Mono<String> createUser(@NonNull @Validated(OnCreate.class) User resource) {
		UserEntityBean entity = conversionService.convert(resource, UserEntityBean.class);
		if (entity == null) {
			log.error("This POJO yielded a null value when converted to an entity bean: {}", resource);
			throw new UnprocessableEntityException();
		}
		entity.setResourceId(secureRandom.nextResourceId());
		return repository.save(entity).doOnSuccess(item -> publishEvent(UserEvent.CREATED, item))
				.flatMap(item -> Mono.just(item.getResourceId()));
	}

	/**
	 * Update
	 */
	public void updateUser(@NonNull @Validated(OnUpdate.class) User resource) {
		Mono<UserEntityBean> entityBean = findByResourceId(resource.getResourceId());
		entityBean.subscribe(value -> {
			// As fields are added to the entity, this block has to be updated
			value.setText(resource.getText());

			repository.save(value).doOnSuccess(item -> publishEvent(UserEvent.UPDATED, item)).subscribe();
		});
	}

	/**
	 * Delete
	 */
	public void deleteUserByResourceId(@NonNull String id) {
		repository.deleteByResourceId(id).doOnSuccess(item -> publishDeleteEvent(UserEvent.DELETED, id)).subscribe();
	}

	/**
	 * Find the EJB having the given resourceId
	 */
	Mono<UserEntityBean> findByResourceId(String id) throws ResourceNotFoundException {
		return repository.findByResourceId(id).switchIfEmpty(Mono.defer(() -> Mono.error(
				new ResourceNotFoundException(String.format("Entity not found with the given resourceId: %s", id)))));
	}

	/**
	 * Publish events
	 */
	private void publishEvent(String event, UserEntityBean entity) {
		log.debug("publishEvent: {}, resourceId: {}", event, entity.getResourceId());
		this.publisher.publishEvent(new UserEvent(event, conversionService.convert(entity, User.class)));
	}

	private void publishDeleteEvent(String event, String resourceId) {
		this.publisher.publishEvent(new UserEvent(event, User.builder().resourceId(resourceId).build()));
	}
}