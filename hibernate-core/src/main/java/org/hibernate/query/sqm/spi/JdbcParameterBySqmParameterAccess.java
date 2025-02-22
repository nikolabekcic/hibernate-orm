/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.spi;

import java.util.List;
import java.util.Map;

import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;

/**
 * Access to the mapping between an SqmParameter and all of its JDBC parameters
 *
 * @author Steve Ebersole
 */
@FunctionalInterface
public interface JdbcParameterBySqmParameterAccess {
	/**
	 * The mapping between an SqmParameter and all of its JDBC parameters
	 */
	Map<SqmParameter, List<List<JdbcParameter>>> getJdbcParamsBySqmParam();
}
