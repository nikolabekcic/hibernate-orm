/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.id;

import java.util.Properties;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.factory.spi.StandardGenerator;
import org.hibernate.id.uuid.StandardRandomStrategy;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.UUIDJavaTypeDescriptor;

/**
 * An {@link IdentifierGenerator} which generates {@link UUID} values using a pluggable
 * {@link UUIDGenerationStrategy generation strategy}.  The values this generator can return
 * include {@link UUID}, {@link String} and byte[16]
 * <p/>
 * Supports 2 config parameters:<ul>
 * <li>{@link #UUID_GEN_STRATEGY} - names the {@link UUIDGenerationStrategy} instance to use</li>
 * <li>{@link #UUID_GEN_STRATEGY_CLASS} - names the {@link UUIDGenerationStrategy} class to use</li>
 * </ul>
 * <p/>
 * Currently there are 2 standard implementations of {@link UUIDGenerationStrategy}:<ul>
 * <li>{@link StandardRandomStrategy} (the default, if none specified)</li>
 * <li>{@link org.hibernate.id.uuid.CustomVersionOneStrategy}</li>
 * </ul>
 *
 * @deprecated (since 6.0) - use {@link org.hibernate.id.uuid.UuidGenerator} and
 * {@link org.hibernate.annotations.UuidGenerator} instead
 */
@Deprecated
public class UUIDGenerator implements StandardGenerator {
	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( UUIDGenerator.class );

	public static final String UUID_GEN_STRATEGY = "uuid_gen_strategy";
	public static final String UUID_GEN_STRATEGY_CLASS = "uuid_gen_strategy_class";

	private UUIDGenerationStrategy strategy;
	private UUIDJavaTypeDescriptor.ValueTransformer valueTransformer;

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
		// check first for an explicit strategy instance
		strategy = (UUIDGenerationStrategy) params.get( UUID_GEN_STRATEGY );

		if ( strategy == null ) {
			// next check for an explicit strategy class
			final String strategyClassName = params.getProperty( UUID_GEN_STRATEGY_CLASS );
			if ( strategyClassName != null ) {
				try {
					final ClassLoaderService cls = serviceRegistry.getService( ClassLoaderService.class );
					final Class strategyClass = cls.classForName( strategyClassName );
					try {
						strategy = (UUIDGenerationStrategy) strategyClass.newInstance();
					}
					catch ( Exception ignore ) {
						LOG.unableToInstantiateUuidGenerationStrategy(ignore);
					}
				}
				catch ( ClassLoadingException ignore ) {
					LOG.unableToLocateUuidGenerationStrategy( strategyClassName );
				}
			}
		}

		if ( strategy == null ) {
			// lastly use the standard random generator
			strategy = StandardRandomStrategy.INSTANCE;
		}

		if ( UUID.class.isAssignableFrom( type.getReturnedClass() ) ) {
			valueTransformer = UUIDJavaTypeDescriptor.PassThroughTransformer.INSTANCE;
		}
		else if ( String.class.isAssignableFrom( type.getReturnedClass() ) ) {
			// todo (6.0) : allow for org.hibernate.type.descriptor.java.UUIDJavaTypeDescriptor.NoDashesStringTransformer
			valueTransformer = UUIDJavaTypeDescriptor.ToStringTransformer.INSTANCE;
		}
		else if ( byte[].class.isAssignableFrom( type.getReturnedClass() ) ) {
			valueTransformer = UUIDJavaTypeDescriptor.ToBytesTransformer.INSTANCE;
		}
		else {
			throw new HibernateException( "Unanticipated return type [" + type.getReturnedClass().getName() + "] for UUID conversion" );
		}
	}

	public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		return valueTransformer.transform( strategy.generateUUID( session ) );
	}
}
