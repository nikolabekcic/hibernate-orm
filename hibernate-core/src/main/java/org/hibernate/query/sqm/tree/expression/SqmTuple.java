/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import java.util.Arrays;
import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.query.criteria.JpaCompoundSelection;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.SqmExpressable;
import org.hibernate.query.sqm.tree.select.SqmJpaCompoundSelection;

/**
 * Models a tuple of values, generally defined as a series of values
 * wrapped in parentheses, e.g. `(value1, value2, ..., valueN)`.
 *
 * Differs from {@link SqmJpaCompoundSelection} in that this node can be used
 * anywhere in the SQM tree, whereas SqmJpaCompoundSelection is only valid
 * in the SELECT clause per JPA
 *
 * @author Steve Ebersole
 */
public class SqmTuple<T>
		extends AbstractSqmExpression<T>
		implements JpaCompoundSelection<T> {
	private final List<SqmExpression<?>> groupedExpressions;

	public SqmTuple(NodeBuilder nodeBuilder, SqmExpression<?>... groupedExpressions) {
		this( Arrays.asList( groupedExpressions ), nodeBuilder );
	}

	public SqmTuple(NodeBuilder nodeBuilder, SqmExpressable<T> type, SqmExpression<?>... groupedExpressions) {
		this( Arrays.asList( groupedExpressions ), type, nodeBuilder );
	}

	public SqmTuple(List<SqmExpression<?>> groupedExpressions, NodeBuilder nodeBuilder) {
		this( groupedExpressions, null, nodeBuilder );
	}

	public SqmTuple(List<SqmExpression<?>> groupedExpressions, SqmExpressable<T> type, NodeBuilder nodeBuilder) {
		super( type, nodeBuilder );
		if ( groupedExpressions.isEmpty() ) {
			throw new QueryException( "tuple grouping cannot be constructed over zero expressions" );
		}
		this.groupedExpressions = groupedExpressions;
		if ( type == null ) {
			setExpressableType( nodeBuilder.getTypeConfiguration().resolveTupleType( groupedExpressions ) );
		}
	}

	public List<SqmExpression<?>> getGroupedExpressions() {
		return groupedExpressions;
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitTuple( this );
	}

	@Override
	public void appendHqlString(StringBuilder sb) {
		sb.append( '(' );
		groupedExpressions.get( 0 ).appendHqlString( sb );
		for ( int i = 1; i < groupedExpressions.size(); i++ ) {
			sb.append(", ");
			groupedExpressions.get( i ).appendHqlString( sb );
		}
		sb.append( ')' );
	}

	@Override
	public String asLoggableText() {
		return toString();
	}

	@Override
	public boolean isCompoundSelection() {
		return true;
	}

	@Override
	public List<? extends JpaSelection<?>> getSelectionItems() {
		return getGroupedExpressions();
	}

}
