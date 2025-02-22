/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import java.util.Collections;
import java.util.List;
import jakarta.persistence.criteria.Expression;

import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;

/**
 * @author Steve Ebersole
 */
public class SqmGroupedPredicate extends AbstractSqmPredicate {
	private final SqmPredicate subPredicate;

	public SqmGroupedPredicate(SqmPredicate subPredicate, NodeBuilder nodeBuilder) {
		super( subPredicate.getExpressable(), nodeBuilder );
		this.subPredicate = subPredicate;
	}

	public SqmPredicate getSubPredicate() {
		return subPredicate;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitGroupedPredicate( this );
	}

	@Override
	public boolean isNegated() {
		return false;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return Collections.singletonList( subPredicate );
	}

	@Override
	public SqmPredicate not() {
		return new SqmNegatedPredicate( this, nodeBuilder() );
	}
	@Override
	public void appendHqlString(StringBuilder sb) {
		sb.append( '(' );
		subPredicate.appendHqlString( sb );
		sb.append( ')' );
	}
}
