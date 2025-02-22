/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.jdbc.internal;

import org.hibernate.dialect.Dialect;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.spi.BasicJdbcLiteralFormatter;

/**
 * JdbcLiteralFormatter implementation for handling character data
 *
 * @author Steve Ebersole
 */
public class JdbcLiteralFormatterCharacterData extends BasicJdbcLiteralFormatter {
	public static final String NATIONALIZED_PREFIX = "n";

	private final boolean isNationalized;

	public JdbcLiteralFormatterCharacterData(JavaType<?> javaTypeDescriptor) {
		this( javaTypeDescriptor, false );
	}

	public JdbcLiteralFormatterCharacterData(JavaType<?> javaTypeDescriptor, boolean isNationalized) {
		super( javaTypeDescriptor );
		this.isNationalized = isNationalized;
	}

	@Override
	public void appendJdbcLiteral(SqlAppender appender, Object value, Dialect dialect, WrapperOptions wrapperOptions) {
		final String literalValue = unwrap( value, String.class, wrapperOptions );
		if ( isNationalized ) {
			appender.appendSql( NATIONALIZED_PREFIX );
		}
		dialect.appendLiteral( appender, literalValue );
	}
}
