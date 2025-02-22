/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.mapping.formula;

import java.sql.Types;

import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.SybaseASEDialect;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.DomainModelScope;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.SkipForDialect;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * @author Steve Ebersole
 */
@DomainModel( xmlMappings = "org/hibernate/orm/test/mapping/formula/EntityOfFormulas.hbm.xml")
@SessionFactory
public class FormulaFromHbmTests {
	@Test
	public void mappingAssertions(DomainModelScope scope) {
		scope.withHierarchy(
				EntityOfFormulas.class,
				(rootClass) -> {
					final Property stringFormula = rootClass.getProperty( "stringFormula" );
					{
						final int[] sqlTypes = stringFormula.getType().getSqlTypeCodes( scope.getDomainModel() );
						assertThat( sqlTypes.length, is( 1 ) );
						assertThat( sqlTypes[ 0 ], is( Types.VARCHAR ) );

						final Selectable selectable = ( (BasicValue) stringFormula.getValue() ).getColumn();
						assertThat( selectable, instanceOf( Formula.class ) );
					}

					final Property integerFormula = rootClass.getProperty( "integerFormula" );
					{
						final int[] sqlTypes = integerFormula.getType().getSqlTypeCodes( scope.getDomainModel() );
						assertThat( sqlTypes.length, is( 1 ) );
						assertThat( sqlTypes[ 0 ], is( Types.INTEGER ) );

						final Selectable selectable = ( (BasicValue) integerFormula.getValue() ).getColumn();
						assertThat( selectable, instanceOf( Formula.class ) );
					}
				}
		);
	}

	@Test
	@SkipForDialect(dialectClass = SybaseASEDialect.class, reason = "Sybase has no trim function which is used in the mapping", matchSubTypes = true)
	@SkipForDialect(dialectClass = MySQLDialect.class, reason = "The MySQL JDBC driver doesn't support the JDBC escape for the concat function which is used in the mapping", matchSubTypes = true)
	public void testBasicHqlUse(SessionFactoryScope scope) {
		scope.inTransaction(
				(session) -> session.createQuery( "from EntityOfFormulas" ).list()
		);
	}
}
