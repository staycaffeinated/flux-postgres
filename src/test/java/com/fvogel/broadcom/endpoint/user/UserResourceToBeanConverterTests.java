/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcom.endpoint.user;

import com.fvogel.broadcom.math.SecureRandomSeries;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserResourceToBeanConverterTests {

	UserResourceToBeanConverter converterUnderTest = new UserResourceToBeanConverter();

	final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@Test
	void whenDataToConvertIsWellFormed_expectSuccessfulConversion() {
		final String expectedPublicId = randomSeries.nextResourceId();
		final String expectedText = "hello world";

		User pojo = User.builder().resourceId(expectedPublicId).text(expectedText).build();
		UserEntityBean ejb = converterUnderTest.convert(pojo);

		assertThat(ejb).isNotNull();
		assertThat(ejb.getResourceId()).isEqualTo(expectedPublicId);
		assertThat(ejb.getText()).isEqualTo(expectedText);
	}

	@Test
	void whenDataListIsWellFormed_expectSuccessfulConversion() {
		// Given a list of 3 items
		final String itemOne_expectedPublicId = randomSeries.nextResourceId();
		final String itemOne_expectedText = "hello goodbye";

		final String itemTwo_expectedPublicId = randomSeries.nextResourceId();
		final String itemTwo_expectedText = "strawberry fields";

		final String itemThree_expectedPublicId = randomSeries.nextResourceId();
		final String itemThree_expectedText = "sgt pepper";

		User itemOne = User.builder().resourceId(itemOne_expectedPublicId).text(itemOne_expectedText).build();
		User itemTwo = User.builder().resourceId(itemTwo_expectedPublicId).text(itemTwo_expectedText).build();
		User itemThree = User.builder().resourceId(itemThree_expectedPublicId).text(itemThree_expectedText).build();

		ArrayList<User> list = new ArrayList<>();
		list.add(itemOne);
		list.add(itemTwo);
		list.add(itemThree);

		// When
		List<UserEntityBean> results = converterUnderTest.convert(list);

		// Then expect the fields of the converted items to match the original items
		assertThat(results).hasSameSizeAs(list);
		assertThat(fieldsMatch(itemOne, results.get(0))).isTrue();
		assertThat(fieldsMatch(itemTwo, results.get(1))).isTrue();
		assertThat(fieldsMatch(itemThree, results.get(2))).isTrue();
	}

	@Test
	void whenConvertingNullObject_expectNullPointerException() {
		assertThrows(NullPointerException.class, () -> converterUnderTest.convert((User) null));
	}

	@Test
	void whenConvertingNullList_expectNullPointerException() {
		assertThrows(NullPointerException.class, () -> converterUnderTest.convert((List<User>) null));
	}

	@Test
	void shouldPopulateAllFields() {
		User resource = User.builder().resourceId(randomSeries.nextResourceId()).text("hello world").build();

		UserEntityBean bean = converterUnderTest.convert(resource);
		assertThat(bean.getResourceId()).isEqualTo(resource.getResourceId());
		assertThat(bean.getText()).isEqualTo(resource.getText());
	}

	// ------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------
	private boolean fieldsMatch(User expected, UserEntityBean actual) {
		if (!Objects.equals(expected.getResourceId(), actual.getResourceId()))
			return false;
		if (!Objects.equals(expected.getText(), actual.getText()))
			return false;
		return true;
	}
}
