/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.userguide.mapping.basic;

import java.util.Calendar;
import java.util.GregorianCalendar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.orm.test.jpa.BaseEntityManagerFunctionalTestCase;

import org.junit.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

/**
 * @author Vlad Mihalcea
 */
public class CalendarWithTemporalTimestampTest extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {
				DateEvent.class
		};
	}

	@Test
	public void testLifecycle() {
		final Calendar calendar = new GregorianCalendar();
		doInJPA( this::entityManagerFactory, entityManager -> {
			entityManager.persist( new DateEvent( calendar ) );
		} );
		doInJPA( this::entityManagerFactory, entityManager -> {
			DateEvent dateEvent = entityManager.createQuery( "from DateEvent", DateEvent.class ).getSingleResult();
			//Assert.assertEquals( calendar, dateEvent.getTimestamp() );
		} );
	}

	@Entity(name = "DateEvent")
	public static class DateEvent {

		@Id
		@GeneratedValue
		private Long id;

		@Temporal(TemporalType.TIMESTAMP)
		@Column(name = "`timestamp`")
		private Calendar timestamp;

		public DateEvent() {
		}

		public DateEvent(Calendar timestamp) {
			this.timestamp = timestamp;
		}

		public Long getId() {
			return id;
		}

		public Calendar getTimestamp() {
			return timestamp;
		}
	}
}
