/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.jdbc;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.type.descriptor.JdbcExtractingLogging;
import org.hibernate.type.descriptor.JdbcTypeNameMapper;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;

/**
 * Convenience base implementation of {@link ValueExtractor}
 *
 * @author Steve Ebersole
 */
public abstract class BasicExtractor<J> implements ValueExtractor<J>, Serializable {
	private final JavaType<J> javaTypeDescriptor;
	private final JdbcType jdbcType;

	public BasicExtractor(JavaType<J> javaTypeDescriptor, JdbcType jdbcType) {
		this.javaTypeDescriptor = javaTypeDescriptor;
		this.jdbcType = jdbcType;
	}

	public JavaType<J> getJavaTypeDescriptor() {
		return javaTypeDescriptor;
	}

	public JdbcType getJdbcTypeDescriptor() {
		return jdbcType;
	}

	@Override
	public J extract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
		final J value = doExtract( rs, paramIndex, options );
		if ( value == null || rs.wasNull() ) {
			if ( JdbcExtractingLogging.TRACE_ENABLED ) {
				JdbcExtractingLogging.LOGGER.tracef(
						"extracted value ([%s] : [%s]) - [null]",
						paramIndex,
						JdbcTypeNameMapper.getTypeName( getJdbcTypeDescriptor().getDefaultSqlTypeCode() )
				);
			}
			return null;
		}
		else {
			if ( JdbcExtractingLogging.TRACE_ENABLED ) {
				JdbcExtractingLogging.LOGGER.tracef(
						"extracted value ([%s] : [%s]) - [%s]",
						paramIndex,
						JdbcTypeNameMapper.getTypeName( getJdbcTypeDescriptor().getDefaultSqlTypeCode() ),
						getJavaTypeDescriptor().extractLoggableRepresentation( value )
				);
			}
			return value;
		}
	}

	/**
	 * Perform the extraction.
	 * <p/>
	 * Called from {@link #extract}.  Null checking of the value (as well as consulting {@link ResultSet#wasNull}) is
	 * done there.
	 *
	 * @return The extracted value.
	 *
	 * @throws SQLException Indicates a problem access the result set
	 */
	protected abstract J doExtract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException;

	@Override
	public J extract(CallableStatement statement, int paramIndex, WrapperOptions options) throws SQLException {
		final J value = doExtract( statement, paramIndex, options );
		if ( value == null || statement.wasNull() ) {
			if ( JdbcExtractingLogging.TRACE_ENABLED ) {
				JdbcExtractingLogging.LOGGER.tracef(
						"extracted procedure output  parameter ([%s] : [%s]) - [null]",
						paramIndex,
						JdbcTypeNameMapper.getTypeName( getJdbcTypeDescriptor().getDefaultSqlTypeCode() )
				);
			}
			return null;
		}
		else {
			if ( JdbcExtractingLogging.TRACE_ENABLED ) {
				JdbcExtractingLogging.LOGGER.tracef(
						"extracted procedure output  parameter ([%s] : [%s]) - [%s]",
						paramIndex,
						JdbcTypeNameMapper.getTypeName( getJdbcTypeDescriptor().getDefaultSqlTypeCode() ),
						getJavaTypeDescriptor().extractLoggableRepresentation( value )
				);
			}
			return value;
		}
	}

	/**
	 * Perform the extraction.
	 * <p/>
	 * Called from {@link #extract}.  Null checking of the value (as well as consulting {@link ResultSet#wasNull}) is
	 * done there.
	 *
	 * @return The extracted value.
	 *
	 * @throws SQLException Indicates a problem accessing the parameter value
	 */
	protected abstract J doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException;

	@Override
	public J extract(CallableStatement statement, String paramName, WrapperOptions options) throws SQLException {
		final J value = doExtract( statement, paramName, options );
		if ( value == null || statement.wasNull() ) {
			if ( JdbcExtractingLogging.TRACE_ENABLED ) {
				JdbcExtractingLogging.LOGGER.tracef(
						"extracted named procedure output  parameter ([%s] : [%s]) - [null]",
						paramName,
						JdbcTypeNameMapper.getTypeName( getJdbcTypeDescriptor().getDefaultSqlTypeCode() )
				);
			}
			return null;
		}
		else {
			if ( JdbcExtractingLogging.TRACE_ENABLED ) {
				JdbcExtractingLogging.LOGGER.tracef(
						"extracted named procedure output  parameter ([%s] : [%s]) - [%s]",
						paramName,
						JdbcTypeNameMapper.getTypeName( getJdbcTypeDescriptor().getDefaultSqlTypeCode() ),
						getJavaTypeDescriptor().extractLoggableRepresentation( value )
				);
			}
			return value;
		}
	}

	/**
	 * Perform the extraction.
	 * <p/>
	 * Called from {@link #extract}.  Null checking of the value (as well as consulting {@link ResultSet#wasNull}) is
	 * done there.
	 *
	 * @param statement The callable statement containing the output parameter
	 * @param name The output parameter name
	 * @param options The binding options
	 *
	 * @return The extracted value.
	 *
	 * @throws SQLException Indicates a problem accessing the parameter value
	 */
	protected abstract J doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException;
}
