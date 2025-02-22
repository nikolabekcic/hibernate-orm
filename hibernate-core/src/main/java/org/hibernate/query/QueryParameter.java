/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query;

import org.hibernate.Incubating;
import org.hibernate.metamodel.model.domain.AllowableParameterType;

/**
 * Represents a parameter defined in the source (HQL/JPQL or criteria) query.
 *
 * @author Steve Ebersole
 */
@Incubating
public interface QueryParameter<T> extends jakarta.persistence.Parameter<T> {
	/**
	 * Does this parameter allow multi-valued (collection, array, etc) binding?
	 * <p/>
	 * This is only valid for HQL/JPQL and (I think) Criteria queries, and is
	 * determined based on the context of the parameters declaration.
	 *
	 * @return {@code true} indicates that multi-valued binding is allowed for this
	 * parameter
	 */
	boolean allowsMultiValuedBinding();

	/**
	 * Get the Hibernate Type associated with this parameter, if one.  May
	 * return {@code null}.
	 *
	 * @return The associated Hibernate Type, may be {@code null}.
	 */
	AllowableParameterType<T> getHibernateType();
}
