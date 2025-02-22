/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import org.hibernate.metamodel.model.domain.EntityDomainType;
import org.hibernate.query.PathException;
import org.hibernate.query.TreatedNavigablePath;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;

/**
 * @author Steve Ebersole
 */
public class SqmTreatedSimplePath<T, S extends T>
		extends SqmEntityValuedSimplePath<S>
		implements SqmSimplePath<S>, SqmTreatedPath<T,S> {

	private final EntityDomainType<S> treatTarget;
	private final SqmPath<T> wrappedPath;

	@SuppressWarnings({"unchecked", "WeakerAccess"})
	public SqmTreatedSimplePath(
			SqmPluralValuedSimplePath<T> wrappedPath,
			EntityDomainType<S> treatTarget,
			NodeBuilder nodeBuilder) {
		super(
				wrappedPath.getNavigablePath().treatAs(
						treatTarget.getHibernateEntityName()
				),
				(EntityDomainType<S>) wrappedPath.getReferencedPathSource(),
				wrappedPath.getLhs(),
				nodeBuilder
		);
		this.treatTarget = treatTarget;
		this.wrappedPath = wrappedPath;
	}

	@SuppressWarnings({"unchecked", "WeakerAccess"})
	public SqmTreatedSimplePath(
			SqmPath<T> wrappedPath,
			EntityDomainType<S> treatTarget,
			NodeBuilder nodeBuilder) {
		super(
				wrappedPath.getNavigablePath().treatAs(
						treatTarget.getHibernateEntityName()
				),
				(EntityDomainType<S>) wrappedPath.getReferencedPathSource(),
				wrappedPath.getLhs(),
				nodeBuilder
		);
		this.treatTarget = treatTarget;
		this.wrappedPath = wrappedPath;
	}

	@Override
	public void registerReusablePath(SqmPath<?> path) {
		super.registerReusablePath( path );
		wrappedPath.registerReusablePath( path );
	}

	@Override
	public EntityDomainType<S> getTreatTarget() {
		return treatTarget;
	}

	@Override
	public SqmPath<T> getWrappedPath() {
		return wrappedPath;
	}

	@Override
	public EntityDomainType<S> getNodeType() {
		return treatTarget;
	}

	@Override
	public <S1 extends S> SqmTreatedSimplePath<S,S1> treatAs(Class<S1> treatJavaType) throws PathException {
		return super.treatAs( treatJavaType );
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitTreatedPath( this );
	}

	@Override
	public void appendHqlString(StringBuilder sb) {
		sb.append( "treat(" );
		wrappedPath.appendHqlString( sb );
		sb.append( " as " );
		sb.append( treatTarget.getName() );
		sb.append( ')' );
	}
}
