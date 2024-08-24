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

import org.hibernate.AssertionFailure;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Uniquely identifies a collection instance in a particular session.
 *
 * @author Gavin King
 */
public final class CollectionKey implements Serializable {
	private final String role;
	private final Object key;
	private final @Nullable Type keyType;
	private final SessionFactoryImplementor factory;
	private final int hashCode;

	public CollectionKey(CollectionPersister persister, Object key) {
		this(
				persister.getRole(),
				key,
				persister.getKeyType().getTypeForEqualsHashCode(),
				persister.getFactory()
		);
	}

	private CollectionKey(
			String role,
			@Nullable Object key,
			@Nullable Type keyType,
			SessionFactoryImplementor factory) {
		this.role = role;
		if ( key == null ) {
			throw new AssertionFailure( "null identifier for collection of role (" + role + ")" );
		}
		this.key = key;
		this.keyType = keyType;
		this.factory = factory;
		//cache the hash-code
		this.hashCode = generateHashCode();
	}

	private int generateHashCode() {
		int result = 17;
		result = 37 * result + role.hashCode();
		result = 37 * result + ( keyType == null ? key.hashCode() : keyType.getHashCode( key, factory ) );
		return result;
	}

	public String getRole() {
		return role;
	}

	public Object getKey() {
		return key;
	}

	@Override
	public String toString() {
		final CollectionPersister collectionDescriptor = factory.getRuntimeMetamodels()
				.getMappingMetamodel()
				.getCollectionDescriptor( role );
		return "CollectionKey" + MessageHelper.collectionInfoString( collectionDescriptor, key, factory );
	}

	@Override
	public boolean equals(final @Nullable Object other) {
		if ( this == other ) {
			return true;
		}
		if ( other == null || CollectionKey.class != other.getClass() ) {
			return false;
		}

		final CollectionKey that = (CollectionKey) other;
		return that.role.equals( role )
				&& ( this.key == that.key ||
					keyType == null ? this.key.equals( that.key ) : keyType.isEqual( this.key, that.key, factory ) );
	}

	@Override
	public int hashCode() {
		return hashCode;
	}


	/**
	 * Custom serialization routine used during serialization of a
	 * Session/PersistenceContext for increased performance.
	 *
	 * @param oos The stream to which we should write the serial data.
	 *
	 */
	public void serialize(ObjectOutputStream oos) throws IOException {
		oos.writeObject( role );
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
	 * @return The deserialized CollectionKey
	 *
	 */
	public static CollectionKey deserialize(
			ObjectInputStream ois,
			SessionImplementor session) throws IOException, ClassNotFoundException {
		return new CollectionKey(
				(String) ois.readObject(),
				ois.readObject(),
				(Type) ois.readObject(),
				// Should never be able to be null
				session.getFactory()
		);
	}
}
