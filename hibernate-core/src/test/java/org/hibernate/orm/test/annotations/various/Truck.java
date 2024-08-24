/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

//$Id$
package org.hibernate.orm.test.annotations.various;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;

/**
 * @author Emmanuel Bernard
 */
@Entity
@Table(indexes = {@Index(name = "weigth_idx", columnList = "weight"),
		@Index(name = "agreement_idx", columnList = "agreement_id")})
public class Truck extends Vehicule {

	private int weight;

	@ManyToOne
	@JoinColumn(name = "agreement_id")
	private ProfessionalAgreement agreement;

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public ProfessionalAgreement getAgreement() {
		return agreement;
	}

	public void setAgreement(ProfessionalAgreement agreement) {
		this.agreement = agreement;
	}

}
