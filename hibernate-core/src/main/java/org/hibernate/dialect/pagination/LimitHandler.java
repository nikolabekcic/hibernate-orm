/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.dialect.pagination;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.query.Limit;
import org.hibernate.query.spi.QueryOptions;

/**
 * Contract defining dialect-specific limit and offset handling.
 * Most implementations extend {@link AbstractLimitHandler}.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public interface LimitHandler {
	/**
	 * Does this handler support limiting query results?
	 *
	 * @return True if this handler supports limit alone.
	 */
	boolean supportsLimit();

	/**
	 * Does this handler support offsetting query results without
	 * also specifying a limit?
	 *
	 * @return True if this handler supports offset alone.
	 */
	boolean supportsOffset();

	/**
	 * Does this handler support combinations of limit and offset?
	 *
	 * @return True if the handler supports an offset within the limit support.
	 */
	boolean supportsLimitOffset();

	String processSql(String sql, Limit limit);

	default String processSql(String sql, Limit limit, QueryOptions queryOptions) {
		return processSql( sql, limit );
	}

	int bindLimitParametersAtStartOfQuery(Limit limit, PreparedStatement statement, int index) throws SQLException;

	int bindLimitParametersAtEndOfQuery(Limit limit, PreparedStatement statement, int index) throws SQLException;

	void setMaxRows(Limit limit, PreparedStatement statement) throws SQLException;

}
