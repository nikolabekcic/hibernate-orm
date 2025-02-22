/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.dialect.function;

import java.util.List;

import org.hibernate.metamodel.model.domain.AllowableFunctionReturnType;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.function.FunctionRenderingSupport;
import org.hibernate.query.sqm.function.SelfRenderingSqmFunction;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * A special function that renders to the T-SQL "str" function if more than a single argument is given,
 * otherwise renders a cast expression like {@link CastStrEmulation}.
 *
 * @author Christian Beikov
 */
public class TransactSQLStrFunction extends CastStrEmulation implements FunctionRenderingSupport {

	public TransactSQLStrFunction(TypeConfiguration typeConfiguration) {
		super(
				"str",
				StandardArgumentsValidators.between( 1, 3 ),
				StandardFunctionReturnTypeResolvers.invariant(
						typeConfiguration.getBasicTypeRegistry().resolve( StandardBasicTypes.STRING )
				)
		);
	}

	@Override
	protected <T> SelfRenderingSqmFunction<T> generateSqmFunctionExpression(
			List<? extends SqmTypedNode<?>> arguments,
			AllowableFunctionReturnType<T> impliedResultType,
			QueryEngine queryEngine,
			TypeConfiguration typeConfiguration) {
		if ( arguments.size() == 1 ) {
			return super.generateSqmFunctionExpression( arguments, impliedResultType, queryEngine, typeConfiguration );
		}

		return new SelfRenderingSqmFunction<>(
				this,
				this,
				arguments,
				impliedResultType,
				getReturnTypeResolver(),
				queryEngine.getCriteriaBuilder(),
				getName()
		);
	}

	@Override
	public void render(SqlAppender sqlAppender, List<SqlAstNode> arguments, SqlAstTranslator<?> walker) {
		sqlAppender.appendSql( "str(" );
		arguments.get( 0 ).accept( walker );
		for ( int i = 1; i < arguments.size(); i++ ) {
			sqlAppender.appendSql( ',' );
			arguments.get( i ).accept( walker );
		}
		sqlAppender.appendSql( ')' );
	}
}
