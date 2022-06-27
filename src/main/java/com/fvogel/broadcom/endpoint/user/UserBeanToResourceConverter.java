/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserBeanToResourceConverter implements Converter<UserEntityBean, User> {

	/**
	 * Convert the source object of type {@code User} to target type
	 * {@code UserResource}.
	 *
	 * @param source
	 *            the source object to convert, which must be an instance of
	 *            {@code User} (never {@code null})
	 * @return the converted object, which must be an instance of
	 *         {@code UserResource} (potentially {@code null})
	 * @throws IllegalArgumentException
	 *             if the source cannot be converted to the desired target type
	 */
	@Override
	public User convert(@NonNull UserEntityBean source) {
		return User.builder().resourceId(source.getResourceId()).text(source.getText()).build();
	}

	/**
	 * Convert a list of EJBs into RestfulResource objects
	 */
	public List<User> convert(@NonNull List<UserEntityBean> sourceList) {
		return sourceList.stream().map(this::convert).toList();
	}
}