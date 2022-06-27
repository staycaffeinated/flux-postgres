/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

/**
 * Routes to User resources
 */
@SuppressWarnings({"java:S1075"})
public final class UserRoutes {

	private UserRoutes() {
	}

	public static final String BASE_PATH_USER = "/users";
	public static final String USER_ID = "/{id}";

	public static final String FIND_ONE_USER = BASE_PATH_USER + USER_ID;
	public static final String FIND_ALL_USER = BASE_PATH_USER + "/findAll";
	public static final String STREAM_USER = BASE_PATH_USER + "/stream";

	public static final String CREATE_USER = BASE_PATH_USER;
	public static final String UPDATE_USER = BASE_PATH_USER + USER_ID;
	public static final String DELETE_USER = BASE_PATH_USER + USER_ID;
	public static final String SEARCH_USER = BASE_PATH_USER + "/search";

	// Get websocket events
	public static final String EVENTS_USER = BASE_PATH_USER + "/ws/events";
}