/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.annotations.comment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Iterator;
import java.util.stream.StreamSupport;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import org.hibernate.annotations.Comment;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.BaseUnitTest;
import org.junit.jupiter.api.Test;

/**
 * @author Yanming Zhou
 */
@BaseUnitTest
public class CommentTest {

	private static final String TABLE_NAME = "TestEntity";
	private static final String TABLE_COMMENT = "I am table";

	@Test
	@TestForIssue(jiraKey = "HHH-4369")
	public void testComments() {
		StandardServiceRegistry ssr = new StandardServiceRegistryBuilder().build();
		Metadata metadata = new MetadataSources(ssr).addAnnotatedClass(TestEntity.class).buildMetadata();
		Table table = StreamSupport.stream(metadata.getDatabase().getNamespaces().spliterator(), false)
				.flatMap(namespace -> namespace.getTables().stream()).filter(t -> t.getName().equals(TABLE_NAME))
				.findFirst().orElse(null);
		assertThat(table.getComment(), is(TABLE_COMMENT));
		Iterator<Column> it = table.getColumnIterator();
		while (it.hasNext()) {
			Column col = it.next();
			assertThat(col.getComment(), is("I am " + col.getName()));
		}
	}

	@Entity(name = "Person")
	@jakarta.persistence.Table(name = TABLE_NAME)
	@org.hibernate.annotations.Table(comment = TABLE_COMMENT, appliesTo = TABLE_NAME)
	public static class TestEntity {

		@Id
		@GeneratedValue
		@Comment("I am id")
		private Long id;

		@Comment("I am name")
		@jakarta.persistence.Column(length = 50)
		private String name;

		@ManyToOne
		@JoinColumn(name = "other")
		@Comment("I am other")
		private TestEntity other;

	}
}
