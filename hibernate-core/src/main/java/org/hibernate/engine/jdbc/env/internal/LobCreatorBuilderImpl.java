/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.engine.jdbc.env.internal;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.ContextualLobCreator;
import org.hibernate.engine.jdbc.LobCreationContext;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.engine.jdbc.NonContextualLobCreator;
import org.hibernate.engine.jdbc.env.spi.LobCreatorBuilder;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;

import org.jboss.logging.Logger;

/**
 * Builds {@link LobCreator} instances based on the capabilities of the environment.
 *
 * @author Steve Ebersole
 */
public class LobCreatorBuilderImpl implements LobCreatorBuilder {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger(
			CoreMessageLogger.class,
			LobCreatorBuilderImpl.class.getName()
	);

	private final boolean useContextualLobCreation;

	private LobCreatorBuilderImpl(boolean useContextualLobCreation) {
		this.useContextualLobCreation = useContextualLobCreation;
	}

	// factory methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * The public factory method for obtaining the appropriate LOB creator (according to given
	 * JDBC {@link Connection}).
	 *
	 * @param dialect The {@link Dialect} in use
	 * @param configValues The map of settings
	 * @param jdbcConnection A JDBC {@link Connection} which can be used to gauge the drivers level of support,
	 * specifically for creating LOB references.
	 */
	public static LobCreatorBuilderImpl makeLobCreatorBuilder(Dialect dialect, Map configValues, Connection jdbcConnection) {
		return new LobCreatorBuilderImpl( useContextualLobCreation( dialect, configValues, jdbcConnection ) );
	}

	/**
	 * For used when JDBC Connection is not available.
	 *
	 * @return Appropriate LobCreatorBuilder
	 */
	public static LobCreatorBuilderImpl makeLobCreatorBuilder() {
		LOG.disablingContextualLOBCreationSinceConnectionNull();
		return new LobCreatorBuilderImpl( false );
	}

	private static final Class[] NO_ARG_SIG = ArrayHelper.EMPTY_CLASS_ARRAY;
	private static final Object[] NO_ARGS = ArrayHelper.EMPTY_OBJECT_ARRAY;

	/**
	 * Basically here we are simply checking whether we can call the {@link Connection} methods for
	 * LOB creation added in JDBC 4.  We not only check whether the {@link Connection} declares these methods,
	 * but also whether the actual {@link Connection} instance implements them (i.e. can be called without simply
	 * throwing an exception).
	 *
	 * @param dialect The {@link Dialect} in use
	 * @param configValues The map of settings
	 * @param jdbcConnection The connection which can be used in level-of-support testing.
	 *
	 * @return True if the connection can be used to create LOBs; false otherwise.
	 */
	@SuppressWarnings("unchecked")
	private static boolean useContextualLobCreation(Dialect dialect, Map configValues, Connection jdbcConnection) {
		final boolean isNonContextualLobCreationRequired =
				ConfigurationHelper.getBoolean( Environment.NON_CONTEXTUAL_LOB_CREATION, configValues );
		if ( isNonContextualLobCreationRequired ) {
			LOG.disablingContextualLOBCreation( Environment.NON_CONTEXTUAL_LOB_CREATION );
			return false;
		}
		if ( jdbcConnection == null ) {
			LOG.disablingContextualLOBCreationSinceConnectionNull();
			return false;
		}

		try {
			try {
				final DatabaseMetaData meta = jdbcConnection.getMetaData();
				// if the jdbc driver version is less than 4, it shouldn't have createClob
				if ( meta.getJDBCMajorVersion() < 4 ) {
					LOG.disablingContextualLOBCreationSinceOldJdbcVersion( meta.getJDBCMajorVersion() );
					return false;
				}

				if ( !dialect.supportsJdbcConnectionLobCreation( meta ) ) {
					return false;
				}
			}
			catch ( SQLException ignore ) {
				// ignore exception and continue
			}

			final Class connectionClass = Connection.class;
			final Method createClobMethod = connectionClass.getMethod( "createClob", NO_ARG_SIG );
			if ( createClobMethod.getDeclaringClass().equals( Connection.class ) ) {
				// If we get here we are running in a jdk 1.6 (jdbc 4) environment...
				// Further check to make sure the driver actually implements the LOB creation methods.  We
				// check against createClob() as indicative of all; should we check against all 3 explicitly?
				try {
					final Object clob = createClobMethod.invoke( jdbcConnection, NO_ARGS );
					try {
						final Method freeMethod = clob.getClass().getMethod( "free", NO_ARG_SIG );
						freeMethod.invoke( clob, NO_ARGS );
					}
					catch ( Throwable ignore ) {
						LOG.tracef( "Unable to free CLOB created to test createClob() implementation : %s", ignore );
					}
					return true;
				}
				catch ( Throwable t ) {
					LOG.disablingContextualLOBCreationSinceCreateClobFailed( t );
				}
			}
		}
		catch ( NoSuchMethodException ignore ) {
		}

		return false;
	}

	/**
	 * Build a LobCreator using the given context
	 *
	 * @param lobCreationContext The LOB creation context
	 *
	 * @return The LobCreator
	 */
	public LobCreator buildLobCreator(LobCreationContext lobCreationContext) {
		return useContextualLobCreation
				? new ContextualLobCreator( lobCreationContext )
				: NonContextualLobCreator.INSTANCE;
	}
}
