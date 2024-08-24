/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

//$Id$
package org.hibernate.orm.test.annotations.xml.hbm;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table( name = "ENTITYB" )
public class BImpl extends AImpl implements B {
	private static final long serialVersionUID = 1L;

	private Integer bId = 0;

	public BImpl() {
		super();
	}

	public Integer getBId() {
		return bId;
	}

	public void setBId(Integer bId) {
		this.bId = bId;
	}
}
