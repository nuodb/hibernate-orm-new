/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

//$Id: DocumentInterceptor.java 7860 2005-08-11 21:58:23Z oneovthafew $
package org.hibernate.orm.test.interfaceproxy;

import java.util.Calendar;

import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

/**
 * @author Gavin King
 */
public class DocumentInterceptor implements Interceptor {

	public boolean onFlushDirty(
			Object entity,
			Object id,
			Object[] currentState,
			Object[] previousState,
			String[] propertyNames,
			Type[] types) throws CallbackException {
		if ( entity instanceof Document ) {
			currentState[2] = Calendar.getInstance();
			return true;
		}
		else {
			return false;
		}
	}

	public boolean onSave(
			Object entity,
			Object id,
			Object[] state,
			String[] propertyNames,
			Type[] types) throws CallbackException {
		if ( entity instanceof Document ) {
			state[3] = state[2] = Calendar.getInstance();
			return true;
		}
		else {
			return false;
		}
	}

	public void onDelete(
			Object entity,
			Object id,
			Object[] state,
			String[] propertyNames,
			Type[] types) throws CallbackException {

	}
}
