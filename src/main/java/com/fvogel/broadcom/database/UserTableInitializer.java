/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcom.database;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

/**
 * This is an example of how to create and initialize database tables. There are
 * one of these classes per entity type, for the sake of keeping the code
 * generation simple.
 */
@Configuration
@EnableR2dbcRepositories
public class UserTableInitializer {

	@Bean
	public ConnectionFactoryInitializer userInitializer(
			@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
		var initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);

		var populator = new CompositeDatabasePopulator();
		populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("database/user-schema.sql")));
		initializer.setDatabasePopulator(populator);
		return initializer;
	}
}