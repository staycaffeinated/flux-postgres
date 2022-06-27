/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

import com.fvogel.broadcom.math.SecureRandomSeries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * unit tests
 */
class UserEventTests {

	final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@Test
	void shouldReturnEventTypeOfCreated() {
		User resource = User.builder().resourceId(randomSeries.nextResourceId()).text("Hello world").build();
		UserEvent event = new UserEvent(UserEvent.CREATED, resource);

		assertThat(event.getEventType()).isEqualTo(UserEvent.CREATED);
	}

	@Test
	void shouldReturnEventTypeOfUpdated() {
		User resource = User.builder().resourceId(randomSeries.nextResourceId()).text("Hello world").build();
		UserEvent event = new UserEvent(UserEvent.UPDATED, resource);

		assertThat(event.getEventType()).isEqualTo(UserEvent.UPDATED);
	}

	@Test
	void shouldReturnEventTypeOfDeleted() {
		User resource = User.builder().resourceId(randomSeries.nextResourceId()).text("Hello world").build();
		UserEvent event = new UserEvent(UserEvent.DELETED, resource);

		assertThat(event.getEventType()).isEqualTo(UserEvent.DELETED);
	}
}