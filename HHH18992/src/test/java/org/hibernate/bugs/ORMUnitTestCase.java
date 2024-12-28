/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.Test;

import reproducer.Customer;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using its built-in unit test framework.
 * Although ORMStandaloneTestCase is perfectly acceptable as a reproducer, usage of this class is much preferred.
 * Since we nearly always include a regression test with bug fixes, providing your reproducer using this method
 * simplifies the process.
 * <p>
 * What's even better?  Fork hibernate-orm itself, add your test case directly to a module's unit tests, then
 * submit it as a PR!
 */
@DomainModel(
		annotatedClasses = {
				// Add your entities here.
				Customer.class,
				// Bar.class
		},
		// If you use *.hbm.xml mappings, instead of annotations, add the mappings here.
		xmlMappings = {
				// "org/hibernate/test/Foo.hbm.xml",
				// "org/hibernate/test/Bar.hbm.xml"
		}
)
@ServiceRegistry(
		// Add in any settings that are specific to your test.  See resources/hibernate.properties for the defaults.
		settings = {
				// For your own convenience to see generated queries:
				@Setting(name = AvailableSettings.SHOW_SQL, value = "true"),
				@Setting(name = AvailableSettings.FORMAT_SQL, value = "true"),
				// @Setting( name = AvailableSettings.GENERATE_STATISTICS, value = "true" ),

				// Add your own settings that are a part of your quarkus configuration:
				// @Setting( name = AvailableSettings.SOME_CONFIGURATION_PROPERTY, value = "SOME_VALUE" ),
		}
)
@SessionFactory
class ORMUnitTestCase {

	// Add your tests, using standard JUnit 5.
	@Test
	void hhh18992Test(SessionFactoryScope scope) throws Exception {
		
		List<Customer> customerList = List.of(
			new Customer(1L, "Customer A"),
			new Customer(2L, "Customer B"),
			new Customer(3L, "Customer C"),
			new Customer(4L, "Customer D"),
			new Customer(5L, "Customer E")
		);

		List<Long> customerIds = customerList.stream()
			.map(Customer::getId)
			.collect(Collectors.toList());
		
		scope.inTransaction( session -> {

			customerList.forEach( session::persist );

		} );

		scope.inTransaction( session -> {

			List<Customer> customersLoaded = session.byMultipleIds(Customer.class)
				.with(new LockOptions(LockMode.PESSIMISTIC_READ))
				.multiLoad(customerIds);

			assertNotNull(customerList);
			assertEquals(customerList.size(), customersLoaded.size());
			customersLoaded.forEach(customer -> {
				assertEquals(LockMode.PESSIMISTIC_READ, session.getCurrentLockMode(customer));
			});

		} );

	}
}
