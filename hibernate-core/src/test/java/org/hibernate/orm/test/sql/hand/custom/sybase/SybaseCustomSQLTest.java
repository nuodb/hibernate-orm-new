/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.sql.hand.custom.sybase;

import org.hibernate.dialect.SybaseDialect;
import org.hibernate.orm.test.sql.hand.custom.CustomStoredProcTestSupport;

import org.hibernate.testing.RequiresDialect;

/**
 * Custom SQL tests for Sybase dialects
 * 
 * @author Gavin King
 */
@RequiresDialect(SybaseDialect.class)
public class SybaseCustomSQLTest extends CustomStoredProcTestSupport {
	public String[] getMappings() {
		return new String[] { "sql/hand/custom/sybase/Mappings.hbm.xml" };
	}
}

