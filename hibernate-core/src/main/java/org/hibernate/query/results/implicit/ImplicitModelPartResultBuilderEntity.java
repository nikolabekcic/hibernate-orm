/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.results.implicit;

import java.util.function.BiFunction;

import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.EntityValuedModelPart;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.results.DomainResultCreationStateImpl;
import org.hibernate.query.results.ResultBuilderEntityValued;
import org.hibernate.query.results.ResultsHelper;
import org.hibernate.query.results.dynamic.DynamicFetchBuilderLegacy;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.entity.EntityResult;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMetadata;

/**
 * @author Steve Ebersole
 */
public class ImplicitModelPartResultBuilderEntity
		implements ImplicitModelPartResultBuilder, ResultBuilderEntityValued {

	private final NavigablePath navigablePath;
	private final EntityValuedModelPart modelPart;

	public ImplicitModelPartResultBuilderEntity(
			NavigablePath navigablePath,
			EntityValuedModelPart modelPart) {
		this.navigablePath = navigablePath;
		this.modelPart = modelPart;
	}

	public ImplicitModelPartResultBuilderEntity(EntityMappingType entityMappingType) {
		this( new NavigablePath( entityMappingType.getEntityName() ), entityMappingType );
	}

	@Override
	public Class<?> getJavaType() {
		return modelPart.getJavaTypeDescriptor().getJavaTypeClass();
	}

	@Override
	public EntityResult buildResult(
			JdbcValuesMetadata jdbcResultsMetadata,
			int resultPosition,
			BiFunction<String, String, DynamicFetchBuilderLegacy> legacyFetchResolver,
			DomainResultCreationState domainResultCreationState) {
		final DomainResultCreationStateImpl creationStateImpl = ResultsHelper.impl( domainResultCreationState );
		creationStateImpl.disallowPositionalSelections();

		final TableGroup tableGroup = creationStateImpl.getFromClauseAccess().resolveTableGroup(
				navigablePath,
				np -> {
					if ( navigablePath.getParent() != null ) {
						return creationStateImpl.getFromClauseAccess().getTableGroup( navigablePath.getParent() );
					}

					return modelPart.getEntityMappingType().createRootTableGroup(
							// since this is only used for result set mappings, the canUseInnerJoins value is irrelevant.
							true,
							navigablePath,
							null,
							null,
							creationStateImpl,
							creationStateImpl.getCreationContext()
					);
				}
		);

		return (EntityResult) modelPart.createDomainResult(
				navigablePath,
				tableGroup,
				null,
				domainResultCreationState
		);
	}
}
