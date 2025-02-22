/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.boot.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.MappingException;
import org.hibernate.boot.query.NamedProcedureCallDefinition;
import org.hibernate.cfg.annotations.QueryHintDefinition;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.procedure.internal.NamedCallableQueryMementoImpl;
import org.hibernate.procedure.internal.Util;
import org.hibernate.procedure.spi.NamedCallableQueryMemento;
import org.hibernate.procedure.spi.ParameterStrategy;
import org.hibernate.query.internal.ResultSetMappingResolutionContext;
import org.hibernate.query.results.ResultSetMappingImpl;

import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureParameter;

import static org.hibernate.procedure.spi.NamedCallableQueryMemento.ParameterMemento;

/**
 * Holds all the information needed from a named procedure call declaration in order to create a
 * {@link org.hibernate.procedure.internal.ProcedureCallImpl}
 *
 * @author Steve Ebersole
 *
 * @see jakarta.persistence.NamedStoredProcedureQuery
 */
public class NamedProcedureCallDefinitionImpl implements NamedProcedureCallDefinition {
	private final String registeredName;
	private final String procedureName;
	private final Class[] resultClasses;
	private final String[] resultSetMappings;
	private final ParameterDefinitions parameterDefinitions;
	private final Map<String, Object> hints;

	public NamedProcedureCallDefinitionImpl(NamedStoredProcedureQuery annotation) {
		this.registeredName = annotation.name();
		this.procedureName = annotation.procedureName();
		this.hints = new QueryHintDefinition( registeredName, annotation.hints() ).getHintsMap();
		this.resultClasses = annotation.resultClasses();
		this.resultSetMappings = annotation.resultSetMappings();

		this.parameterDefinitions = new ParameterDefinitions( annotation.parameters(), hints );

		final boolean specifiesResultClasses = resultClasses != null && resultClasses.length > 0;
		final boolean specifiesResultSetMappings = resultSetMappings != null && resultSetMappings.length > 0;

		if ( specifiesResultClasses && specifiesResultSetMappings ) {
			throw new MappingException(
					String.format(
							"NamedStoredProcedureQuery [%s] specified both resultClasses and resultSetMappings",
							registeredName
					)
			);
		}
	}

	@Override
	public String getRegistrationName() {
		return registeredName;
	}

	@Override
	public String getProcedureName() {
		return procedureName;
	}

	@Override
	public NamedCallableQueryMemento resolve(SessionFactoryImplementor sessionFactory) {
		final Set<String> collectedQuerySpaces = new HashSet<>();

		final boolean specifiesResultClasses = resultClasses != null && resultClasses.length > 0;
		final boolean specifiesResultSetMappings = resultSetMappings != null && resultSetMappings.length > 0;

		ResultSetMappingImpl resultSetMapping = new ResultSetMappingImpl( registeredName );

		if ( specifiesResultClasses ) {
			Util.resolveResultSetMappingClasses(
					resultClasses,
					resultSetMapping,
					collectedQuerySpaces::add,
					new ResultSetMappingResolutionContext() {
						@Override
						public SessionFactoryImplementor getSessionFactory() {
							return sessionFactory;
						}

//						@Override
//						public void addQueryReturns(NativeSQLQueryReturn... queryReturns) {
//							Collections.addAll( collectedQueryReturns, queryReturns );
//						}
//
//						@Override
//						public void addQuerySpaces(String... spaces) {
//							Collections.addAll( collectedQuerySpaces, spaces );
//						}
					}
			);
		}
		else if ( specifiesResultSetMappings ) {
			Util.resolveResultSetMappingNames(
					resultSetMappings,
					resultSetMapping,
					collectedQuerySpaces::add,
					new ResultSetMappingResolutionContext() {
						@Override
						public SessionFactoryImplementor getSessionFactory() {
							return sessionFactory;
						}

//						@Override
//						public NamedResultSetMappingMemento findResultSetMapping(String name) {
//							return sessionFactory.getQueryEngine().getNamedObjectRepository().getResultSetMappingMemento( name );
//						}
//
//						@Override
//						public void addQueryReturns(NativeSQLQueryReturn... queryReturns) {
//							Collections.addAll( collectedQueryReturns, queryReturns );
//						}
//
//						@Override
//						public void addQuerySpaces(String... spaces) {
//							Collections.addAll( collectedQuerySpaces, spaces );
//						}
					}
			);
		}

		return new NamedCallableQueryMementoImpl(
				getRegistrationName(),
				procedureName,
				parameterDefinitions.getParameterStrategy(),
				parameterDefinitions.toMementos( sessionFactory ),
				resultSetMappings,
				resultClasses,
				collectedQuerySpaces,
				false,
				null,
				CacheMode.IGNORE,
				FlushMode.AUTO,
				false,
				null,
				null,
				null,
				hints
		);
	}

	static class ParameterDefinitions {
		private final ParameterStrategy parameterStrategy;
		private final ParameterDefinition[] parameterDefinitions;

		ParameterDefinitions(StoredProcedureParameter[] parameters, Map<String, Object> queryHintMap) {
			if ( parameters == null || parameters.length == 0 ) {
				parameterStrategy = ParameterStrategy.POSITIONAL;
				parameterDefinitions = new ParameterDefinition[0];
			}
			else {
				parameterStrategy = StringHelper.isNotEmpty( parameters[0].name() )
						? ParameterStrategy.NAMED
						: ParameterStrategy.POSITIONAL;
				parameterDefinitions = new ParameterDefinition[ parameters.length ];

				for ( int i = 0; i < parameters.length; i++ ) {
					parameterDefinitions[i] = ParameterDefinition.from(
							parameterStrategy,
							parameters[i],
							// i+1 for the position because the apis say the numbers are 1-based, not zero
							i+1,
							queryHintMap
					);
				}
			}
		}

		public ParameterStrategy getParameterStrategy() {
			return parameterStrategy;
		}

		public List<ParameterMemento> toMementos(SessionFactoryImplementor sessionFactory) {
			final List<ParameterMemento> mementos = new ArrayList<>();
			for ( ParameterDefinition definition : parameterDefinitions ) {
				mementos.add(definition.toMemento( sessionFactory ));
			}
			return mementos;
		}
	}

	static class ParameterDefinition {
		private final Integer position;
		private final String name;
		private final ParameterMode parameterMode;
		private final Class type;

		static ParameterDefinition from(
				ParameterStrategy parameterStrategy,
				StoredProcedureParameter parameterAnnotation,
				int adjustedPosition,
				Map<String, Object> queryHintMap) {
			return new ParameterDefinition( adjustedPosition, parameterAnnotation );
		}

		private static Boolean interpretBoolean(Object value) {
			if ( value == null ) {
				return null;
			}

			if ( value instanceof Boolean ) {
				return (Boolean) value;
			}

			return Boolean.valueOf( value.toString() );
		}

		ParameterDefinition(int position, StoredProcedureParameter annotation) {
			this.position = position;
			this.name = normalize( annotation.name() );
			this.parameterMode = annotation.mode();
			this.type = annotation.type();
		}

		@SuppressWarnings("UnnecessaryUnboxing")
		public ParameterMemento toMemento(SessionFactoryImplementor sessionFactory) {
			// todo (6.0): figure out how to handle this
//			final boolean initialPassNullSetting = explicitPassNullSetting != null
//					? explicitPassNullSetting.booleanValue()
//					: sessionFactory.getSessionFactoryOptions().isProcedureParameterNullPassingEnabled();

			return new NamedCallableQueryMementoImpl.ParameterMementoImpl(
					position,
					name,
					parameterMode,
					type,
					sessionFactory.getTypeConfiguration().getBasicTypeForJavaType( type )
//					,initialPassNullSetting
			);
		}
	}

	private static String normalize(String name) {
		return StringHelper.isNotEmpty( name ) ? name : null;
	}
}
