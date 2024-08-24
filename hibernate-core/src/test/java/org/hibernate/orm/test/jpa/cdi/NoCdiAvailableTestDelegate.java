/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.jpa.cdi;

import java.util.Collections;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.testing.orm.jpa.PersistenceUnitInfoAdapter;
import org.hibernate.testing.util.ServiceRegistryUtil;

/**
 * @author Steve Ebersole
 */
public class NoCdiAvailableTestDelegate {
	public static EntityManagerFactory passingNoBeanManager() {
		return new HibernatePersistenceProvider().createContainerEntityManagerFactory(
				new PersistenceUnitInfoAdapter(),
				ServiceRegistryUtil.createBaseSettings()
		);
	}

	public static void passingBeanManager() {
		Map<String, Object> settings = ServiceRegistryUtil.createBaseSettings();
		settings.put( AvailableSettings.JAKARTA_CDI_BEAN_MANAGER, new Object() );
		new HibernatePersistenceProvider().createContainerEntityManagerFactory(
				new PersistenceUnitInfoAdapter(),
				settings
		).close();
	}
}
