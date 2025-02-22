/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.converter;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.PersistenceException;

import org.hibernate.metamodel.model.convert.spi.JpaAttributeConverter;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import org.jboss.logging.Logger;

/**
 * Adapter for incorporating JPA {@link AttributeConverter} handling into the SqlTypeDescriptor contract.
 * <p/>
 * Essentially this is responsible for mapping to/from the intermediate database type representation.  Continuing the
 * {@code AttributeConverter<Integer,String>} example from
 * {@link org.hibernate.mapping.SimpleValue#buildAttributeConverterTypeAdapter()}, the "intermediate database type
 * representation" would be the String representation.  So on binding, we convert the incoming Integer to String;
 * on extraction we extract the value as String and convert to Integer.
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("JavadocReference")
public class AttributeConverterJdbcTypeAdapter implements JdbcType {
	private static final Logger log = Logger.getLogger( AttributeConverterJdbcTypeAdapter.class );

	private final JpaAttributeConverter converter;
	private final JdbcType delegate;
	private final JavaType intermediateJavaTypeDescriptor;

	public AttributeConverterJdbcTypeAdapter(
			JpaAttributeConverter converter,
			JdbcType delegate,
			JavaType intermediateJavaTypeDescriptor) {
		this.converter = converter;
		this.delegate = delegate;
		this.intermediateJavaTypeDescriptor = intermediateJavaTypeDescriptor;
	}

	@Override
	public int getJdbcTypeCode() {
		return delegate.getJdbcTypeCode();
	}

	@Override
	public int getDefaultSqlTypeCode() {
		return delegate.getDefaultSqlTypeCode();
	}

	@Override
	public String toString() {
		return "AttributeConverterSqlTypeDescriptorAdapter(" + converter.getClass().getName() + ")";
	}


	// Binding ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	@SuppressWarnings("unchecked")
	public <X> ValueBinder<X> getBinder(JavaType<X> javaTypeDescriptor) {
		// Get the binder for the intermediate type representation
		final ValueBinder realBinder = delegate.getBinder( intermediateJavaTypeDescriptor );

		return new ValueBinder<X>() {
			@Override
			public void bind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
				final Object convertedValue;
				try {
					convertedValue = converter.toRelationalValue( value );
				}
				catch (PersistenceException pe) {
					throw pe;
				}
				catch (RuntimeException re) {
					throw new PersistenceException( "Error attempting to apply AttributeConverter", re );
				}

				log.debugf( "Converted value on binding : %s -> %s", value, convertedValue );
				realBinder.bind( st, convertedValue, index, options );
			}

			@Override
			public void bind(CallableStatement st, X value, String name, WrapperOptions options) throws SQLException {
				final Object convertedValue;
				try {
					convertedValue = converter.toRelationalValue( value );
				}
				catch (PersistenceException pe) {
					throw pe;
				}
				catch (RuntimeException re) {
					throw new PersistenceException( "Error attempting to apply AttributeConverter", re );
				}

				log.debugf( "Converted value on binding : %s -> %s", value, convertedValue );
				realBinder.bind( st, convertedValue, name, options );
			}
		};
	}


	// Extraction ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public <X> ValueExtractor<X> getExtractor(JavaType<X> javaTypeDescriptor) {
		// Get the extractor for the intermediate type representation
		final ValueExtractor realExtractor = delegate.getExtractor( intermediateJavaTypeDescriptor );

		return new ValueExtractor<X>() {
			@Override
			public X extract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
				return doConversion( realExtractor.extract( rs, paramIndex, options ) );
			}

			@Override
			public X extract(CallableStatement statement, int paramIndex, WrapperOptions options) throws SQLException {
				return doConversion( realExtractor.extract( statement, paramIndex, options ) );
			}

			@Override
			public X extract(CallableStatement statement, String paramName, WrapperOptions options) throws SQLException {
				return doConversion( realExtractor.extract( statement, paramName, options ) );
			}

			@SuppressWarnings("unchecked")
			private X doConversion(Object extractedValue) {
				try {
					X convertedValue = (X) converter.toDomainValue( extractedValue );
					log.debugf( "Converted value on extraction: %s -> %s", extractedValue, convertedValue );
					return convertedValue;
				}
				catch (PersistenceException pe) {
					throw pe;
				}
				catch (RuntimeException re) {
					throw new PersistenceException( "Error attempting to apply AttributeConverter", re );
				}
			}
		};
	}
}
