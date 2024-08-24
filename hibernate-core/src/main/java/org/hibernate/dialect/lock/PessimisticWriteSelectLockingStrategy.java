/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.dialect.lock;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.persister.entity.EntityPersister;

/**
 * A pessimistic locking strategy where {@link LockMode#PESSIMISTIC_WRITE}
 * lock is obtained via a select statement.
 * <p>
 * Differs from {@link SelectLockingStrategy} in throwing
 * {@link PessimisticEntityLockException}.
 *
 * @see org.hibernate.dialect.Dialect#getForUpdateString(LockMode)
 * @see org.hibernate.dialect.Dialect#appendLockHint(LockOptions, String)
 *
 * @author Steve Ebersole
 * @author Scott Marlow
 * @since 3.5
 */
public class PessimisticWriteSelectLockingStrategy extends AbstractSelectLockingStrategy {
	/**
	 * Construct a locking strategy based on SQL SELECT statements.
	 *
	 * @param lockable The metadata for the entity to be locked.
	 * @param lockMode Indicates the type of lock to be acquired.
	 */
	public PessimisticWriteSelectLockingStrategy(EntityPersister lockable, LockMode lockMode) {
		super( lockable, lockMode );
	}

	@Override
	protected HibernateException convertException(Object entity, JDBCException ex) {
		return new PessimisticEntityLockException( entity, "could not obtain pessimistic lock", ex );
	}
}
