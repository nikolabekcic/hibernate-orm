/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.engine.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;

/**
 * Used to uniquely key an entity instance in relation to a particular session
 * by some unique property reference, as opposed to identifier.
 * <p/>
 * Uniqueing information consists of the entity-name, the referenced
 * property name, and the referenced property value.
 *
 * @author Gavin King
 * @see EntityKey
 */
public class EntityUniqueKey implements Serializable {
	private final String uniqueKeyName;
	private final String entityName;
	private final Object key;
	private final Type keyType;
	private final int hashCode;

	public EntityUniqueKey(
			final String entityName,
			final String uniqueKeyName,
			final Object key,
			final Type keyType,
			final SessionFactoryImplementor factory) {
		this.uniqueKeyName = uniqueKeyName;
		this.entityName = entityName;
		this.key = key;
		this.keyType = keyType;
		this.hashCode = generateHashCode( factory );
	}

	public String getEntityName() {
		return entityName;
	}

	public Object getKey() {
		return key;
	}

	public String getUniqueKeyName() {
		return uniqueKeyName;
	}

	public int generateHashCode(SessionFactoryImplementor factory) {
		int result = 17;
		result = 37 * result + entityName.hashCode();
		result = 37 * result + uniqueKeyName.hashCode();
		result = 37 * result + keyType.getHashCode( key, factory );
		return result;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object other) {
		EntityUniqueKey that = (EntityUniqueKey) other;
		return that != null && that.entityName.equals( entityName )
				&& that.uniqueKeyName.equals( uniqueKeyName )
				&& keyType.isEqual( that.key, key );
	}

	@Override
	public String toString() {
		return "EntityUniqueKey" + MessageHelper.infoString( entityName, uniqueKeyName, key );
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		checkAbilityToSerialize();
		oos.defaultWriteObject();
	}

	private void checkAbilityToSerialize() {
		// The unique property value represented here may or may not be
		// serializable, so we do an explicit check here in order to generate
		// a better error message
		if ( key != null && !Serializable.class.isAssignableFrom( key.getClass() ) ) {
			throw new IllegalStateException(
					"Cannot serialize an EntityUniqueKey which represents a non " +
							"serializable property value [" + entityName + "." + uniqueKeyName + "]"
			);
		}
	}

	/**
	 * Custom serialization routine used during serialization of a
	 * Session/PersistenceContext for increased performance.
	 *
	 * @param oos The stream to which we should write the serial data.
	 *
	 * @throws IOException
	 */
	public void serialize(ObjectOutputStream oos) throws IOException {
		checkAbilityToSerialize();
		oos.writeObject( uniqueKeyName );
		oos.writeObject( entityName );
		oos.writeObject( key );
		oos.writeObject( keyType );
	}

	/**
	 * Custom deserialization routine used during deserialization of a
	 * Session/PersistenceContext for increased performance.
	 *
	 * @param ois The stream from which to read the entry.
	 * @param session The session being deserialized.
	 *
	 * @return The deserialized EntityEntry
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static EntityUniqueKey deserialize(
			ObjectInputStream ois,
			SessionImplementor session) throws IOException, ClassNotFoundException {
		return new EntityUniqueKey(
				(String) ois.readObject(),
				(String) ois.readObject(),
				ois.readObject(),
				(Type) ois.readObject(),
				session.getFactory()
		);
	}
}
