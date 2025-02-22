/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.event.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import org.hibernate.Session;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Gail Badner
 */
@TestForIssue( jiraKey = "HHH-9979")
public class MergeListPreAndPostPersistTest extends BaseCoreFunctionalTestCase {

	protected Class[] getAnnotatedClasses() {
		return new Class[] {
				Order.class,
				Item.class
		};
	}

	@Test
	@TestForIssue( jiraKey = "HHH-9979")
	public void testAllPropertiesCopied() {
		final Order order = new Order();
		order.id = 1L;
		order.name = "order";
		Item item = new Item();
		item.id = 1L;
		item.name = "item";
		order.items.add( item );

		addEntityListeners( order );

		Session s = openSession();
		s.getTransaction().begin();
		s.merge( order );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.getTransaction().begin();
		s.delete( order );
		s.getTransaction().commit();
		s.close();
	}

	@Entity(name = "`Order`")
	private static class Order {
		@Id
		public Long id;

		@Basic(optional = false)
		public String name;

		@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
		public List<Item> items = new ArrayList<Item>();

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			Order order = (Order) o;

			return name.equals( order.name );

		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}

	@Entity(name = "Item")
	private static class Item {
		@Id
		public Long id;

		@Basic(optional = false)
		public String name;

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			Item item = (Item) o;

			return name.equals( item.name );
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}

	private void addEntityListeners(final Order order) {

		EventListenerRegistry registry = sessionFactory().getServiceRegistry()
				.getService( EventListenerRegistry.class );
		registry.setListeners(
				EventType.PRE_INSERT,
				new PreInsertEventListener() {
					@Override
					public boolean onPreInsert(PreInsertEvent event) {
						if ( Order.class.isInstance( event.getEntity() ) ) {
							assertEquals( order, event.getEntity());
							assertEquals( order.items, ( (Order) event.getEntity() ).items );
						}
						return false;
					}
				}
		);

		registry.setListeners(
				EventType.POST_INSERT,
				new PostInsertEventListener() {
					public void onPostInsert(PostInsertEvent event) {
						if ( Order.class.isInstance( event.getEntity() ) ) {
							assertEquals( order, event.getEntity());
							assertEquals( order.items, ( (Order) event.getEntity() ).items );
						}
					}

					public boolean requiresPostCommitHanding(EntityPersister persister) {
						return false;
					}
				}
		);
	}
}
