/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.spi;

import java.util.Objects;

import org.hibernate.query.ResultListTransformer;
import org.hibernate.query.TupleTransformer;
import org.hibernate.query.spi.QueryInterpretationCache;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMappingProducer;

/**
 * @author Steve Ebersole
 */
public class SelectInterpretationsKey implements QueryInterpretationCache.Key {
	private final String sql;
	private final JdbcValuesMappingProducer jdbcValuesMappingProducer;
	private final TupleTransformer tupleTransformer;
	private final ResultListTransformer resultListTransformer;

	public SelectInterpretationsKey(
			String sql,
			JdbcValuesMappingProducer jdbcValuesMappingProducer,
			TupleTransformer tupleTransformer,
			ResultListTransformer resultListTransformer) {
		this.sql = sql;
		this.jdbcValuesMappingProducer = jdbcValuesMappingProducer;
		this.tupleTransformer = tupleTransformer;
		this.resultListTransformer = resultListTransformer;
	}

	@Override
	public String getQueryString() {
		return sql;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		SelectInterpretationsKey that = (SelectInterpretationsKey) o;

		return sql.equals( that.sql )
				&& Objects.equals( jdbcValuesMappingProducer, that.jdbcValuesMappingProducer )
				&& Objects.equals( tupleTransformer, that.tupleTransformer )
				&& Objects.equals( resultListTransformer, that.resultListTransformer );

	}

	@Override
	public int hashCode() {
		int result = sql.hashCode();
		result = 31 * result + jdbcValuesMappingProducer.hashCode();
		result = 31 * result + ( tupleTransformer != null ? tupleTransformer.hashCode() : 0 );
		result = 31 * result + ( resultListTransformer != null ? resultListTransformer.hashCode() : 0 );
		return result;
	}
}
