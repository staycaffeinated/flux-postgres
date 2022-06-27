/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcom.endpoint.user;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts Drink entity beans into DrinkResource objects
 */
@Component
public class UserResourceToBeanConverter implements Converter<User, UserEntityBean> {
	/**
	 * Convert the source object of type {@code User to target type {@code
	 * UserEntityBean}.
	 *
	 * @param resource the source object to convert, which must be an instance of
	 * {@code User} (never {@code null})
	 * 
	 * @return the converted object, which must be an instance of
	 *         {@code UserEntityBean} (potentially {@code null})
	 * @throws IllegalArgumentException
	 *             if the source cannot be converted to the desired target type
	 */
	@Override
	public UserEntityBean convert(@NonNull User resource) {
		var target = new UserEntityBean();
		target.setResourceId(resource.getResourceId());
		target.setText(resource.getText());
		return target;
	}

	/**
	 * Convert a list of RestfulResource objects into EJBs
	 */
	public List<UserEntityBean> convert(@NonNull List<User> sourceList) {
		return sourceList.stream().map(this::convert).toList();
	}
}
