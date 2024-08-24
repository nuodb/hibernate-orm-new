/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.named;

import org.hibernate.Incubating;

/**
 * Contract for Query impls that can be converted to a named query memento to be
 * stored in the {@link NamedObjectRepository}
 *
 * @author Steve Ebersole
 */
@Incubating
public interface NameableQuery {
	/**
	 * Convert the query into the memento
	 */
	NamedQueryMemento<?> toMemento(String name);
}
