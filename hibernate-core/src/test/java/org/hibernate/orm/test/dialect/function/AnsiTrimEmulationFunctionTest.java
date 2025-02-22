/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.dialect.function;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.dialect.function.TrimFunction;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.JdbcMappingContainer;
import org.hibernate.query.TrimSpec;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.spi.StandardSqlAstTranslator;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.sql.ast.tree.expression.SelfRenderingExpression;
import org.hibernate.sql.ast.tree.expression.TrimSpecification;
import org.hibernate.sql.exec.spi.JdbcOperation;
import org.hibernate.type.descriptor.java.CharacterJavaTypeDescriptor;
import org.hibernate.type.descriptor.jdbc.CharJdbcType;
import org.hibernate.type.internal.BasicTypeImpl;
import org.hibernate.type.spi.TypeConfiguration;

import org.hibernate.testing.orm.junit.FailureExpected;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.junit.Test;

import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class AnsiTrimEmulationFunctionTest  {
	private static final String trimSource = "a.column";

    @Test
	public void testBasicSqlServerProcessing() {
		Dialect dialect = new SQLServerDialect();
		TrimFunction function = new TrimFunction( dialect, new TypeConfiguration() );

		performBasicSpaceTrimmingTests( dialect, function );

		final String expectedTrimPrep = "replace(replace(a.column,' ','#%#%'),'-',' ')";
		final String expectedPostTrimPrefix = "replace(replace(";
		final String expectedPostTrimSuffix = ",' ','-'),'#%#%',' ')";

		// -> trim(LEADING '-' FROM a.column)
		String rendered = render( dialect, function, TrimSpec.LEADING, '-', trimSource );
		String expected = expectedPostTrimPrefix + "ltrim(" + expectedTrimPrep + ")" + expectedPostTrimSuffix;
		assertEquals( expected, rendered );

		// -> trim(TRAILING '-' FROM a.column)
		rendered = render( dialect, function, TrimSpec.TRAILING, '-', trimSource );
		expected = expectedPostTrimPrefix + "rtrim(" + expectedTrimPrep + ")" + expectedPostTrimSuffix;
		assertEquals( expected, rendered );

		// -> trim(BOTH '-' FROM a.column)
		rendered = render( dialect, function, TrimSpec.BOTH, '-', trimSource );
		expected = expectedPostTrimPrefix + "ltrim(rtrim(" + expectedTrimPrep + "))" + expectedPostTrimSuffix;
		assertEquals( expected, rendered );
	}

    @Test
	public void testBasicSybaseProcessing() {
		Dialect dialect = new SybaseDialect();
		TrimFunction function = new TrimFunction( dialect, new TypeConfiguration() );

		performBasicSpaceTrimmingTests( dialect, function );

		final String expectedTrimPrep = "str_replace(str_replace(a.column,' ','#%#%'),'-',' ')";
		final String expectedPostTrimPrefix = "str_replace(str_replace(";
		final String expectedPostTrimSuffix = ",' ','-'),'#%#%',' ')";

		// -> trim(LEADING '-' FROM a.column)
		String rendered = render( dialect, function, TrimSpec.LEADING, '-', trimSource );
		String expected = expectedPostTrimPrefix + "ltrim(" + expectedTrimPrep + ")" + expectedPostTrimSuffix;
		assertEquals( expected, rendered );

		// -> trim(TRAILING '-' FROM a.column)
		rendered = render( dialect, function, TrimSpec.TRAILING, '-', trimSource );
		expected = expectedPostTrimPrefix + "rtrim(" + expectedTrimPrep + ")" + expectedPostTrimSuffix;
		assertEquals( expected, rendered );

		// -> trim(BOTH '-' FROM a.column)
		rendered = render( dialect, function, TrimSpec.BOTH, '-', trimSource );
		expected = expectedPostTrimPrefix + "ltrim(rtrim(" + expectedTrimPrep + "))" + expectedPostTrimSuffix;
		assertEquals( expected, rendered );
	}

	private void performBasicSpaceTrimmingTests(Dialect dialect, TrimFunction function) {
		// -> trim(a.column)
		String rendered = render( dialect, function, TrimSpec.BOTH, ' ', trimSource );
		assertEquals( "ltrim(rtrim(a.column))", rendered );

		// -> trim(LEADING FROM a.column)
		rendered = render( dialect, function, TrimSpec.LEADING, ' ', trimSource );
		assertEquals( "ltrim(a.column)", rendered );

		// -> trim(TRAILING FROM a.column)
		rendered = render( dialect, function, TrimSpec.TRAILING, ' ', trimSource );
		assertEquals( "rtrim(a.column)", rendered );
	}

	private String render(
			Dialect dialect,
			TrimFunction function,
			TrimSpec trimSpec,
			char trimCharacter,
			String trimSource) {
		SessionFactoryImplementor factory = Mockito.mock( SessionFactoryImplementor.class );
		JdbcServices jdbcServices = Mockito.mock( JdbcServices.class );
		Mockito.doReturn( jdbcServices ).when( factory ).getJdbcServices();
		Mockito.doReturn( dialect ).when( jdbcServices ).getDialect();
		StandardSqlAstTranslator<JdbcOperation> walker = new StandardSqlAstTranslator<>(
				factory,
				null
		);
		List<SqlAstNode> sqlAstArguments = new ArrayList<>();
		sqlAstArguments.add( new TrimSpecification( trimSpec ) );
		sqlAstArguments.add( new QueryLiteral<>( trimCharacter, new BasicTypeImpl<>( CharacterJavaTypeDescriptor.INSTANCE, CharJdbcType.INSTANCE ) ) );
		sqlAstArguments.add( new SelfRenderingExpression() {
			@Override
			public void renderToSql(
					SqlAppender sqlAppender, SqlAstTranslator<?> walker, SessionFactoryImplementor sessionFactory) {
				sqlAppender.appendSql( trimSource );
			}

			@Override
			public JdbcMappingContainer getExpressionType() {
				return null;
			}
		} );
    	function.render( walker, sqlAstArguments, walker );
		return walker.getSql();
	}

}
