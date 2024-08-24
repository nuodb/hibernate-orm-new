/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.jaxb.mapping.spi;

/**
 * JAXB binding interface for natural-id definitions
 *
 * @author Steve Ebersole
 */
public interface JaxbNaturalId extends JaxbBaseAttributesContainer {
	/**
	 * The cache config associated with this natural-id
	 */
	JaxbCachingImpl getCaching();

	/**
	 * Whether the natural-id (all attributes which are part of it) should
	 * be considered mutable or immutable.
	 */
	boolean isMutable();
}
