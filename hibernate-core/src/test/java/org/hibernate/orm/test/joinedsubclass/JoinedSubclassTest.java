/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.joinedsubclass;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.query.Query;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gavin King
 */
@DomainModel(
		xmlMappings = "org/hibernate/orm/test/joinedsubclass/Person.hbm.xml"
)
@SessionFactory
public class JoinedSubclassTest {

	@AfterEach
	public void tearDown(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					session.createQuery( "delete from Employee" ).executeUpdate();
					session.createQuery( "delete from Customer" ).executeUpdate();
					session.createQuery( "delete from Person" ).executeUpdate();
				}
		);
	}


	@Test
	public void testAccessAsIncorrectSubclass(SessionFactoryScope scope) {
		final Employee e = new Employee();
		scope.inTransaction(
				session -> {
					e.setName( "Steve" );
					e.setSex( 'M' );
					e.setTitle( "grand poobah" );
					session.save( e );
				}
		);


		Customer c = scope.fromTransaction(
				session ->
						session.get( Customer.class, new Long( e.getId() ) )

		);
		assertNull( c );

		scope.inTransaction(
				session -> {
					Employee employee = session.get( Employee.class, new Long( e.getId() ) );
					Customer customer = session.get( Customer.class, new Long( e.getId() ) );
					assertNotNull( employee );
					assertNull( customer );
				}
		);

		scope.inTransaction(
				session -> {
					session.delete( e );
				}
		);
	}

	@Test
	public void testQuerySubclassAttribute(SessionFactoryScope scope) {
		scope.inTransaction( s -> {
			Person p = new Person();
			p.setName( "Emmanuel" );
			p.setSex( 'M' );
			s.persist( p );
			Employee q = new Employee();
			q.setName( "Steve" );
			q.setSex( 'M' );
			q.setTitle( "Mr" );
			q.setSalary( new BigDecimal( 1000 ) );
			s.persist( q );

			List result = s.createQuery( "from Person where salary > 100" ).list();
			assertEquals( result.size(), 1 );
			assertSame( result.get( 0 ), q );

			result = s.createQuery( "from Person where salary > 100 or name like 'E%'" ).list();
			assertEquals( result.size(), 2 );

			CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
			CriteriaQuery<Person> criteria = criteriaBuilder.createQuery( Person.class );

			Root<Person> root = criteria.from( Person.class );

			criteria.where( criteriaBuilder.gt( root.get( "salary" ), new BigDecimal( 100 ) ) );

			result = s.createQuery( criteria ).list();
//			result = s.createCriteria( Person.class )
//					.add( Property.forName( "salary" ).gt( new BigDecimal( 100 ) ) )
//					.list();
			assertEquals( result.size(), 1 );
			assertSame( result.get( 0 ), q );

			//TODO: make this work:
		/*result = s.createQuery("select salary from Person where salary > 100").list();
		assertEquals( result.size(), 1 );
		assertEquals( result.get(0), new BigDecimal(1000) );*/

			s.delete( p );
			s.delete( q );
		} );
	}

	@Test
	public void testLockingJoinedSubclass(SessionFactoryScope scope) {
		Person p = new Person();
		Employee q = new Employee();
		scope.inTransaction(
				session -> {
					p.setName( "Emmanuel" );
					p.setSex( 'M' );
					session.persist( p );
					q.setName( "Steve" );
					q.setSex( 'M' );
					q.setTitle( "Mr" );
					q.setSalary( new BigDecimal( 1000 ) );
					session.persist( q );
				}
		);

		scope.inTransaction(
				session -> {
					session.lock( p, LockMode.UPGRADE );
					session.lock( q, LockMode.UPGRADE );
					session.delete( p );
					session.delete( q );
				}
		);
	}

}

