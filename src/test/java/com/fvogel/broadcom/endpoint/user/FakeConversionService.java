/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * Creates a ConversionService instance suitable for testing
 */
public class FakeConversionService {

	static ConversionService build() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter(new UserBeanToResourceConverter());
		service.addConverter(new UserResourceToBeanConverter());
		return service;
	}
}
