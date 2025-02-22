/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.userguide.mapping.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import org.hibernate.annotations.CustomType;
import org.hibernate.annotations.Parameter;
import org.hibernate.orm.test.jpa.BaseEntityManagerFunctionalTestCase;
import org.hibernate.usertype.UserTypeLegacyBridge;

import org.junit.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

/**
 * @author Vlad Mihalcea
 */
public class ExplicitTypeTest extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {
			Product.class
		};
	}

	@Test
	public void test() {
		doInJPA( this::entityManagerFactory, entityManager -> {

		} );
	}

	//tag::basic-type-annotation-example[]
	@Entity(name = "Product")
	public class Product {

		@Id
		private Integer id;
		
		private String sku;

		@CustomType(
				value = UserTypeLegacyBridge.class,
				parameters = @Parameter( name = UserTypeLegacyBridge.TYPE_NAME_PARAM_KEY, value = "nstring" )
		)
		private String name;

		@CustomType(
				value = UserTypeLegacyBridge.class,
				parameters = @Parameter( name = UserTypeLegacyBridge.TYPE_NAME_PARAM_KEY, value = "materialized_nclob" )
		)
		private String description;
	}
	//end::basic-type-annotation-example[]
}
