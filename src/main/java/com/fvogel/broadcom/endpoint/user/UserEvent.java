/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

import org.springframework.context.ApplicationEvent;

/**
 * User events
 */
@SuppressWarnings({"unused"})
public class UserEvent extends ApplicationEvent {

	public static final String CREATED = "CREATED";
	public static final String UPDATED = "UPDATED";
	public static final String DELETED = "DELETED";

	private static final long serialVersionUID = 9152086626754282698L;

	private final String eventType;

	public UserEvent(String eventType, User resource) {
		super(resource);
		this.eventType = eventType;
	}

	public String getEventType() {
		return eventType;
	}

}