/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.userguide.mapping.basic;

import java.sql.Types;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.internal.BasicAttributeMapping;
import org.hibernate.persister.entity.EntityPersister;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;

/**
 * Tests for mapping `double` values
 *
 * @author Steve Ebersole
 */
@DomainModel( annotatedClasses = FloatMappingTests.EntityOfFloats.class )
@SessionFactory
public class FloatMappingTests {

	@Test
	public void testMappings(SessionFactoryScope scope) {
		// first, verify the type selections...
		final MappingMetamodel domainModel = scope.getSessionFactory().getDomainModel();
		final EntityPersister entityDescriptor = domainModel.findEntityDescriptor( EntityOfFloats.class );

		{
			final BasicAttributeMapping attribute = (BasicAttributeMapping) entityDescriptor.findAttributeMapping( "wrapper" );
			assertThat( attribute.getJavaTypeDescriptor().getJavaTypeClass(), equalTo( Float.class ) );

			final JdbcMapping jdbcMapping = attribute.getJdbcMapping();
			assertThat( jdbcMapping.getJavaTypeDescriptor().getJavaTypeClass(), equalTo( Float.class ) );
			assertThat(
					jdbcMapping.getJdbcTypeDescriptor().getJdbcTypeCode(),
					isOneOf( Types.FLOAT, Types.REAL, Types.NUMERIC )
			);
		}

		{
			final BasicAttributeMapping attribute = (BasicAttributeMapping) entityDescriptor.findAttributeMapping( "primitive" );
			assertThat( attribute.getJavaTypeDescriptor().getJavaTypeClass(), equalTo( Float.class ) );

			final JdbcMapping jdbcMapping = attribute.getJdbcMapping();
			assertThat( jdbcMapping.getJavaTypeDescriptor().getJavaTypeClass(), equalTo( Float.class ) );
			assertThat(
					jdbcMapping.getJdbcTypeDescriptor().getJdbcTypeCode(),
					isOneOf( Types.FLOAT, Types.REAL, Types.NUMERIC )
			);
		}


		// and try to use the mapping
		scope.inTransaction(
				(session) -> session.persist( new EntityOfFloats( 1, 3.0F, 5.0F ) )
		);
		scope.inTransaction(
				(session) -> session.get( EntityOfFloats.class, 1 )
		);
	}

	@AfterEach
	public void dropData(SessionFactoryScope scope) {
		scope.inTransaction(
				(session) -> session.createQuery( "delete EntityOfFloats" ).executeUpdate()
		);
	}

	@Entity( name = "EntityOfFloats" )
	@Table( name = "EntityOfFloats" )
	public static class EntityOfFloats {
		@Id
		Integer id;

		//tag::basic-float-example-implicit[]
		// these will be mapped using FLOAT, REAL or NUMERIC
		// depending on the capabilities of the database
		Float wrapper;
		float primitive;
		//end::basic-float-example-implicit[]

		public EntityOfFloats() {
		}

		public EntityOfFloats(Integer id, Float wrapper, float primitive) {
			this.id = id;
			this.wrapper = wrapper;
			this.primitive = primitive;
		}
	}
}
