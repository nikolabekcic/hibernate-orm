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
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * Represents an expression whose type is boolean, and can therefore be used as a predicate.
 *
 * @author Steve Ebersole
 */
public class SqmBooleanExpressionPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression<Boolean> booleanExpression;

	public SqmBooleanExpressionPredicate(
			SqmExpression<Boolean> booleanExpression,
			NodeBuilder nodeBuilder) {
		this( booleanExpression, false, nodeBuilder );
	}

	public SqmBooleanExpressionPredicate(
			SqmExpression<Boolean> booleanExpression,
			boolean negated,
			NodeBuilder nodeBuilder) {
		super( negated, nodeBuilder );

		assert booleanExpression.getNodeType() != null;
		final Class<?> expressionJavaType = booleanExpression.getNodeType().getExpressableJavaTypeDescriptor().getJavaTypeClass();
		assert boolean.class.equals( expressionJavaType ) || Boolean.class.equals( expressionJavaType );

		this.booleanExpression = booleanExpression;
	}

	public SqmExpression<Boolean> getBooleanExpression() {
		return booleanExpression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBooleanExpressionPredicate( this );
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return Collections.singletonList( booleanExpression );
	}

	@Override
	public void appendHqlString(StringBuilder sb) {
		booleanExpression.appendHqlString( sb );
	}

	@Override
	protected SqmNegatablePredicate createNegatedNode() {
		return new SqmBooleanExpressionPredicate( booleanExpression, !isNegated(), nodeBuilder() );
	}
}
