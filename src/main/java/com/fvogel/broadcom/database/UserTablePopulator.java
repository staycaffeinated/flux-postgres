/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.database;

import com.fvogel.broadcom.endpoint.user.UserEntityBean;
import com.fvogel.broadcom.endpoint.user.UserRepository;
import com.fvogel.broadcom.math.SecureRandomSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * This component populates the User database table with sample data. This is
 * suitable for testing and demonstration, but probably not what you want in
 * production.
 */
@Component
@Slf4j
public class UserTablePopulator implements ApplicationListener<ApplicationReadyEvent> {

	private final UserRepository repository;
	private final SecureRandomSeries randomSeries;

	/**
	 * Constructor
	 */
	public UserTablePopulator(UserRepository repository, SecureRandomSeries secureRandom) {
		this.repository = repository;
		this.randomSeries = secureRandom;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		repository.deleteAll()
				.thenMany(Flux.just("One", "Two", "Three", "Four", "Five").map(this::buildSampleRecord)
						.flatMap(repository::save))
				.thenMany(repository.findAll()).subscribe(pet -> log.info("Saving " + pet.toString()));
	}

	/**
	 * Creates a sample database record
	 */
	private UserEntityBean buildSampleRecord(String text) {
		return UserEntityBean.builder().resourceId(randomSeries.nextResourceId()).text(text).build();
	}
}