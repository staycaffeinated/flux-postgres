/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.endpoint.user;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveSortingRepository<UserEntityBean, Long> {

	// Find by the resource ID known by external applications
	Mono<UserEntityBean> findByResourceId(String id);

	// Find by the database ID
	Mono<UserEntityBean> findById(Long id);

	/* returns the number of entities deleted */
	Mono<Long> deleteByResourceId(String id);

	Flux<UserEntityBean> findAllByText(String text);
}
