/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.jpa.event.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import org.hibernate.jpa.event.spi.Callback;
import org.hibernate.jpa.event.spi.CallbackDefinition;
import org.hibernate.jpa.event.spi.CallbackType;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.resource.beans.spi.ManagedBeanRegistry;

/**
 * Represents a JPA callback on the embeddable type
 *
 * @author Vlad Mihalcea
 */
public class EmbeddableCallback extends AbstractCallback {

	public static class Definition implements CallbackDefinition {
		private final Getter embeddableGetter;
		private final Method callbackMethod;
		private final CallbackType callbackType;

		public Definition(Getter embeddableGetter, Method callbackMethod, CallbackType callbackType) {
			this.embeddableGetter = embeddableGetter;
			this.callbackMethod = callbackMethod;
			this.callbackType = callbackType;
		}

		@Override
		public Callback createCallback(ManagedBeanRegistry beanRegistry) {
			return new EmbeddableCallback( embeddableGetter, callbackMethod, callbackType );
		}
	}

	private final Getter embeddableGetter;
	private final Method callbackMethod;

	private EmbeddableCallback(Getter embeddableGetter, Method callbackMethod, CallbackType callbackType) {
		super( callbackType );
		this.embeddableGetter = embeddableGetter;
		this.callbackMethod = callbackMethod;
	}

	@Override
	public boolean performCallback(Object entity) {
		try {
			Object embeddable = embeddableGetter.get( entity );
			if ( embeddable != null ) {
				callbackMethod.invoke( embeddable );
			}
			return true;
		}
		catch (InvocationTargetException e) {
			//keep runtime exceptions as is
			if ( e.getTargetException() instanceof RuntimeException ) {
				throw (RuntimeException) e.getTargetException();
			}
			else {
				throw new RuntimeException( e.getTargetException() );
			}
		}
		catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"EmbeddableCallback([%s] %s.%s)",
				getCallbackType().name(),
				callbackMethod.getDeclaringClass().getName(),
				callbackMethod.getName()
		);
	}
}
