/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcom.endpoint.user;

class UserGenerator {
	static User generateUser() {
		return User.builder().text("sample text").build();
	}
}