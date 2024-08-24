/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.jaxb.mapping.spi;

/**
 * @author Steve Ebersole
 */
public interface JaxbCascadableAttribute extends JaxbPersistentAttribute {
	JaxbCascadeTypeImpl getCascade();
	void setCascade(JaxbCascadeTypeImpl value);
}
