/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.mapping.generated;

import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.testing.RequiresDialect;

/**
 * @author Steve Ebersole
 */
@RequiresDialect(SybaseDialect.class)
@RequiresDialect(SQLServerDialect.class)
public class TimestampGeneratedValuesWithCachingTest extends AbstractGeneratedPropertyTest {
	public final String[] getMappings() {
		return new String[] { "mapping/generated/MSSQLGeneratedPropertyEntity.hbm.xml" };
	}
}
