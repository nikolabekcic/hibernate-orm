/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.java.spi;

import java.util.Map;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaTypeDescriptor;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeDescriptorIndicators;

/**
 * @author Steve Ebersole
 */
public class MapEntryJavaDescriptor extends AbstractClassJavaTypeDescriptor<Map.Entry> {
	/**
	 * Singleton access
	 */
	public static final MapEntryJavaDescriptor INSTANCE = new MapEntryJavaDescriptor();

	public MapEntryJavaDescriptor() {
		super( Map.Entry.class );
	}

	@Override
	public JdbcType getRecommendedJdbcType(JdbcTypeDescriptorIndicators context) {
		throw new UnsupportedOperationException( "Unsupported attempt to resolve JDBC type for Map.Entry" );
	}

	@Override
	public Map.Entry fromString(CharSequence string) {
		throw new UnsupportedOperationException( "Unsupported attempt create Map.Entry from String" );
	}

	@Override
	public <X> X unwrap(Map.Entry value, Class<X> type, WrapperOptions options) {
		throw new UnsupportedOperationException( "Unsupported attempt to unwrap Map.Entry value" );
	}

	@Override
	public <X> Map.Entry wrap(X value, WrapperOptions options) {
		throw new UnsupportedOperationException( "Unsupported attempt to wrap Map.Entry value" );
	}
}
