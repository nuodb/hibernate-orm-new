/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.models.xml.complete;

/**
 * @author Steve Ebersole
 */
public class Sub extends Root {
	private String subName;

	protected Sub() {
		// for Hibernate use
	}

	public Sub(Integer id, String name, String subName) {
		super( id, name );
		this.subName = subName;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}
}
