/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.orm.test.dialect.function;

import static java.util.Calendar.MONTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.hibernate.dialect.SybaseASEDialect;
import org.hibernate.query.Query;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.RequiresDialect;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Richard H. Tingstad
 */
@DomainModel(
		xmlMappings = "org/hibernate/orm/test/dialect/function/Product.hbm.xml"
)
@SessionFactory
@RequiresDialect(value = SybaseASEDialect.class, majorVersion = 11)
public class SybaseASEFunctionTest {

	private Calendar calendar = Calendar.getInstance();

	@BeforeAll
	protected void prepareTest(SessionFactoryScope scope) throws Exception {
		scope.inTransaction(
				session -> {
					Product product = new Product();
					product.setPrice(new BigDecimal(0.5));
					product.setDate( calendar.getTime() );
					session.save( product );
				}
		);
	}

	@AfterAll
	protected void cleanupTest(SessionFactoryScope scope) throws Exception {
		scope.inTransaction(
				session -> session.createQuery( "delete from Product" ).executeUpdate()
		);
	}

	@Test
	public void testCharLengthFunction(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Query query = session.createQuery( "select char_length('123456') from Product" );
					assertEquals(6, ((Number) query.uniqueResult()).intValue());
				}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HHH-7070")
	public void testDateaddFunction(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Query query = session.createQuery( "select dateadd(day, 1, p.date) from Product p" );
					assertTrue(calendar.getTime().before((Date) query.uniqueResult()));
				}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HHH-7070")
	public void testDatepartFunction(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Query query = session.createQuery( "select datepart(month, p.date) from Product p" );
					assertEquals(calendar.get(MONTH) + 1, ((Number) query.uniqueResult()).intValue());
				}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HHH-7070")
	public void testDatediffFunction(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Query query = session.createQuery( "SELECT DATEDIFF( DAY, '1999/07/19 00:00', '1999/07/23 23:59' ) from Product" );
					assertEquals(4, ((Number) query.uniqueResult()).intValue());
				}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HHH-7070")
	public void testAtn2Function(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Query query = session.createQuery("select atn2(p.price, .48) from Product p");
					assertEquals(0.805803, ((Number) query.uniqueResult()).doubleValue(), 0.000001 );
				}
		);
	}
}
