/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.engine.spi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.metamodel.mapping.EntityValuedModelPart;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.select.QuerySpec;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;

/**
 * Encapsulates details related to entities which contain sub-select-fetchable
 * collections and which were loaded in a Session so that those collections may
 * be sub-select fetched later during initialization
 */
public class SubselectFetch {
	private final EntityValuedModelPart entityModelPart;
	private final QuerySpec loadingSqlAst;
	private final TableGroup ownerTableGroup;
	private final List<JdbcParameter> loadingJdbcParameters;
	private final JdbcParameterBindings loadingJdbcParameterBindings;
	private final Set<EntityKey> resultingEntityKeys;

	public SubselectFetch(
			EntityValuedModelPart entityModelPart,
			QuerySpec loadingSqlAst,
			TableGroup ownerTableGroup,
			List<JdbcParameter> loadingJdbcParameters,
			JdbcParameterBindings loadingJdbcParameterBindings,
			Set<EntityKey> resultingEntityKeys) {
		this.entityModelPart = entityModelPart;
		this.loadingSqlAst = loadingSqlAst;
		this.ownerTableGroup = ownerTableGroup;
		this.loadingJdbcParameters = loadingJdbcParameters;
		this.loadingJdbcParameterBindings = loadingJdbcParameterBindings;
		this.resultingEntityKeys = resultingEntityKeys;
	}

	public EntityValuedModelPart getEntityModelPart() {
		return entityModelPart;
	}

	public List<JdbcParameter> getLoadingJdbcParameters() {
		// todo (6.0) : do not believe this is needed
		// 		- see org.hibernate.loader.ast.internal.LoaderSelectBuilder.generateSelect(org.hibernate.engine.spi.SubselectFetch)
		return loadingJdbcParameters;
	}

	/**
	 * The SQL AST select from which the owner was loaded
	 */
	public QuerySpec getLoadingSqlAst() {
		return loadingSqlAst;
	}

	/**
	 * The TableGroup for the owner within the {@link #getLoadingSqlAst()}
	 */
	public TableGroup getOwnerTableGroup() {
		return ownerTableGroup;
	}

	/**
	 * The JDBC parameter bindings related to {@link #getLoadingSqlAst()} for
	 * the specific execution that loaded the owners
	 */
	public JdbcParameterBindings getLoadingJdbcParameterBindings() {
		return loadingJdbcParameterBindings;
	}

	/**
	 *The entity-keys of all owners loaded from a particular execution
	 *
	 * Used for "empty collection" handling mostly
	 */
	public Set<EntityKey> getResultingEntityKeys() {
		return resultingEntityKeys;
	}

	@Override
	public String toString() {
		return "SubselectFetch(" + entityModelPart.getEntityMappingType().getEntityName() + ")";
	}

	public static RegistrationHandler createRegistrationHandler(
			BatchFetchQueue batchFetchQueue,
			SelectStatement sqlAst,
			TableGroup tableGroup,
			List<JdbcParameter> jdbcParameters,
			JdbcParameterBindings jdbcParameterBindings) {
		final SubselectFetch subselectFetch = new SubselectFetch(
				null,
				sqlAst.getQuerySpec(),
				tableGroup,
				jdbcParameters,
				jdbcParameterBindings,
				new HashSet<>()
		);

		return new StandardRegistrationHandler( batchFetchQueue, subselectFetch );
	}

	public static RegistrationHandler createRegistrationHandler(
			BatchFetchQueue batchFetchQueue,
			SelectStatement sqlAst,
			List<JdbcParameter> jdbcParameters,
			JdbcParameterBindings jdbcParameterBindings) {
		final List<TableGroup> roots = sqlAst.getQuerySpec().getFromClause().getRoots();
		if ( roots.isEmpty() ) {
			// we allow this now
			return NO_OP_REG_HANDLER;
		}

		return createRegistrationHandler( batchFetchQueue, sqlAst, roots.get( 0 ), jdbcParameters, jdbcParameterBindings );
	}

	public interface RegistrationHandler {
		void addKey(EntityKey key);
	}

	private static final RegistrationHandler NO_OP_REG_HANDLER = new RegistrationHandler() {
		@Override
		public void addKey(EntityKey key) {
		}
	} ;

	public static class StandardRegistrationHandler implements RegistrationHandler {
		private final BatchFetchQueue batchFetchQueue;
		private final SubselectFetch subselectFetch;

		private StandardRegistrationHandler(BatchFetchQueue batchFetchQueue, SubselectFetch subselectFetch) {
			this.batchFetchQueue = batchFetchQueue;
			this.subselectFetch = subselectFetch;
		}

		public void addKey(EntityKey key) {
			subselectFetch.resultingEntityKeys.add( key );
			batchFetchQueue.addSubselect( key, subselectFetch );
		}
	}
}
