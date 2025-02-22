/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java;

import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import jakarta.persistence.TemporalType;

import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.compare.CalendarComparator;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeDescriptorIndicators;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link Calendar} handling, but just for the time portion.
 *
 * @author Steve Ebersole
 */
public class CalendarTimeJavaTypeDescriptor extends AbstractTemporalJavaTypeDescriptor<Calendar> {
	public static final CalendarTimeJavaTypeDescriptor INSTANCE = new CalendarTimeJavaTypeDescriptor();

	protected CalendarTimeJavaTypeDescriptor() {
		super( Calendar.class, CalendarJavaTypeDescriptor.CalendarMutabilityPlan.INSTANCE, CalendarComparator.INSTANCE );
	}

	@Override
	public TemporalType getPrecision() {
		return TemporalType.TIME;
	}

	@Override
	public JdbcType getRecommendedJdbcType(JdbcTypeDescriptorIndicators context) {
		return context.getTypeConfiguration().getJdbcTypeDescriptorRegistry().getDescriptor( Types.TIME );
	}

	@Override
	protected <X> TemporalJavaTypeDescriptor<X> forTimePrecision(TypeConfiguration typeConfiguration) {
		//noinspection unchecked
		return (TemporalJavaTypeDescriptor<X>) this;
	}

	@Override
	protected <X> TemporalJavaTypeDescriptor<X> forTimestampPrecision(TypeConfiguration typeConfiguration) {
		//noinspection unchecked
		return (TemporalJavaTypeDescriptor<X>) CalendarJavaTypeDescriptor.INSTANCE;
	}

	@Override
	protected <X> TemporalJavaTypeDescriptor<X> forDatePrecision(TypeConfiguration typeConfiguration) {
		//noinspection unchecked
		return (TemporalJavaTypeDescriptor<X>) CalendarDateJavaTypeDescriptor.INSTANCE;
	}

	public String toString(Calendar value) {
		return DateJavaTypeDescriptor.INSTANCE.toString( value.getTime() );
	}

	public Calendar fromString(CharSequence string) {
		Calendar result = new GregorianCalendar();
		result.setTime( DateJavaTypeDescriptor.INSTANCE.fromString( string.toString() ) );
		return result;
	}

	@Override
	public boolean areEqual(Calendar one, Calendar another) {
		if ( one == another ) {
			return true;
		}
		if ( one == null || another == null ) {
			return false;
		}

		return one.get(Calendar.DAY_OF_MONTH) == another.get(Calendar.DAY_OF_MONTH)
			&& one.get(Calendar.MONTH) == another.get(Calendar.MONTH)
			&& one.get(Calendar.YEAR) == another.get(Calendar.YEAR);
	}

	@Override
	public int extractHashCode(Calendar value) {
		int hashCode = 1;
		hashCode = 31 * hashCode + value.get(Calendar.DAY_OF_MONTH);
		hashCode = 31 * hashCode + value.get(Calendar.MONTH);
		hashCode = 31 * hashCode + value.get(Calendar.YEAR);
		return hashCode;
	}

	@SuppressWarnings({ "unchecked" })
	public <X> X unwrap(Calendar value, Class<X> type, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( Calendar.class.isAssignableFrom( type ) ) {
			return (X) value;
		}
		if ( java.sql.Date.class.isAssignableFrom( type ) ) {
			return (X) new java.sql.Date( value.getTimeInMillis() );
		}
		if ( java.sql.Time.class.isAssignableFrom( type ) ) {
			return (X) new java.sql.Time( value.getTimeInMillis() );
		}
		if ( java.sql.Timestamp.class.isAssignableFrom( type ) ) {
			return (X) new java.sql.Timestamp( value.getTimeInMillis() );
		}
		if ( Date.class.isAssignableFrom( type ) ) {
			return (X) new  Date( value.getTimeInMillis() );
		}
		throw unknownUnwrap( type );
	}

	public <X> Calendar wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( Calendar.class.isInstance( value ) ) {
			return (Calendar) value;
		}

		if ( ! Date.class.isInstance( value ) ) {
			throw unknownWrap( value.getClass() );
		}

		Calendar cal = new GregorianCalendar();
		cal.setTime( (Date) value );
		return cal;
	}

	@Override
	public int getDefaultSqlPrecision(Dialect dialect, JdbcType jdbcType) {
		return 0; //seconds (currently ignored since Dialects don't parameterize time type by precision)
	}
}
