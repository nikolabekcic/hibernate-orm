/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.select;

import java.util.Collections;
import java.util.List;

import org.hibernate.query.FetchClauseType;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaOrder;
import org.hibernate.query.criteria.JpaQueryPart;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.query.sqm.tree.SqmVisitableNode;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.type.StandardBasicTypes;

/**
 * Defines the ordering and fetch/offset part of a query which is shared with query groups.
 *
 * @author Christian Beikov
 */
public abstract class SqmQueryPart<T> implements SqmVisitableNode, JpaQueryPart<T> {
	private final NodeBuilder nodeBuilder;

	private SqmOrderByClause orderByClause;

	private SqmExpression<?> offsetExpression;
	private SqmExpression<?> fetchExpression;
	private FetchClauseType fetchClauseType = FetchClauseType.ROWS_ONLY;

	public SqmQueryPart(NodeBuilder nodeBuilder) {
		this.nodeBuilder = nodeBuilder;
	}

	public abstract SqmQuerySpec<T> getFirstQuerySpec();

	public abstract SqmQuerySpec<T> getLastQuerySpec();

	public abstract boolean isSimpleQueryPart();

	@Override
	public NodeBuilder nodeBuilder() {
		return nodeBuilder;
	}

	public SqmOrderByClause getOrderByClause() {
		return orderByClause;
	}

	public void setOrderByClause(SqmOrderByClause orderByClause) {
		this.orderByClause = orderByClause;
	}

	public SqmExpression<?> getFetchExpression() {
		return fetchExpression;
	}

	public SqmExpression<?> getOffsetExpression() {
		return offsetExpression;
	}

	public void setOffsetExpression(SqmExpression<?> offsetExpression) {
		if ( offsetExpression != null ) {
			offsetExpression.applyInferableType( nodeBuilder.getIntegerType() );
		}
		this.offsetExpression = offsetExpression;
	}

	public void setFetchExpression(SqmExpression<?> fetchExpression) {
		setFetchExpression( fetchExpression, FetchClauseType.ROWS_ONLY );
	}

	public void setFetchExpression(SqmExpression<?> fetchExpression, FetchClauseType fetchClauseType) {
		if ( fetchExpression == null ) {
			this.fetchExpression = null;
			this.fetchClauseType = null;
		}
		else {
			if ( fetchClauseType == null ) {
				throw new IllegalArgumentException( "Fetch clause may not be null!" );
			}
			fetchExpression.applyInferableType( nodeBuilder.getIntegerType() );
			this.fetchExpression = fetchExpression;
			this.fetchClauseType = fetchClauseType;
		}
	}

	@Override
	public FetchClauseType getFetchClauseType() {
		return fetchClauseType;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// JPA

	@Override
	public List<SqmSortSpecification> getSortSpecifications() {
		if ( getOrderByClause() == null ) {
			return Collections.emptyList();
		}

		return getOrderByClause().getSortSpecifications();
	}

	@Override
	public SqmQueryPart<T> setSortSpecifications(List<? extends JpaOrder> sortSpecifications) {
		if ( getOrderByClause() == null ) {
			setOrderByClause( new SqmOrderByClause() );
		}

		//noinspection unchecked
		getOrderByClause().setSortSpecifications( (List) sortSpecifications );

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SqmExpression<?> getOffset() {
		return getOffsetExpression();
	}

	@Override
	public SqmQueryPart<T> setOffset(JpaExpression<?> offset) {
		setOffsetExpression( (SqmExpression<?>) offset );
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SqmExpression<?> getFetch() {
		return getFetchExpression();
	}

	@Override
	public SqmQueryPart<T> setFetch(JpaExpression<?> fetch) {
		setFetchExpression( (SqmExpression<?>) fetch );
		return this;
	}

	@Override
	public JpaQueryPart<T> setFetch(JpaExpression<?> fetch, FetchClauseType fetchClauseType) {
		setFetchExpression( (SqmExpression<?>) fetch, fetchClauseType );
		return this;
	}

	public abstract void validateQueryStructureAndFetchOwners();

	public void appendHqlString(StringBuilder sb) {
		if ( orderByClause == null ) {
			return;
		}
		sb.append( " order by " );
		final List<SqmSortSpecification> sortSpecifications = orderByClause.getSortSpecifications();
		sortSpecifications.get( 0 ).appendHqlString( sb );
		for ( int i = 1; i < sortSpecifications.size(); i++ ) {
			sb.append( ", " );
			sortSpecifications.get( i ).appendHqlString( sb );
		}

		if ( offsetExpression != null ) {
			sb.append( " offset " );
			offsetExpression.appendHqlString( sb );
			sb.append( " rows " );
		}
		if ( fetchExpression != null ) {
			sb.append( " fetch first " );
			fetchExpression.appendHqlString( sb );
			switch ( fetchClauseType ) {
				case ROWS_ONLY:
					sb.append( " rows only" );
					break;
				case ROWS_WITH_TIES:
					sb.append( " rows with ties" );
					break;
				case PERCENT_ONLY:
					sb.append( " percent only" );
					break;
				case PERCENT_WITH_TIES:
					sb.append( " percent with ties" );
					break;
			}
		}
	}
}
