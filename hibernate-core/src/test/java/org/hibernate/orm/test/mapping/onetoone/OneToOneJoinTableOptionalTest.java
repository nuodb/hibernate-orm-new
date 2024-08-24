/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.mapping.onetoone;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HHH-11596")
@DomainModel(
		annotatedClasses = {
				OneToOneJoinTableOptionalTest.Show.class,
				OneToOneJoinTableOptionalTest.ShowDescription.class
		}
)
@SessionFactory
public class OneToOneJoinTableOptionalTest {

	@Test
	public void testSavingEntitiesWithANullOneToOneAssociationValue(SessionFactoryScope scope) {
		scope.inTransaction(
				(session) -> {
					Show show = new Show();
					session.persist( show );
				}
		);

		scope.inTransaction(
				(session) -> {
					ShowDescription showDescription = new ShowDescription();
					session.persist( showDescription );
				}
		);
	}

	@Entity(name = "Show")
	@Table(name = "T_SHOW")
	public static class Show {

		@Id
		@GeneratedValue
		private Integer id;

		@OneToOne
		@JoinTable(name = "TSHOW_SHOWDESCRIPTION",
				joinColumns = @JoinColumn(name = "SHOW_ID"),
				inverseJoinColumns = @JoinColumn(name = "DESCRIPTION_ID"), foreignKey = @ForeignKey(name = "FK_DESC"))
		private ShowDescription description;


		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public ShowDescription getDescription() {
			return description;
		}

		public void setDescription(ShowDescription description) {
			this.description = description;
			description.setShow( this );
		}
	}

	@Entity(name = "ShowDescription")
	@Table(name = "SHOW_DESCRIPTION")
	public static class ShowDescription {

		@Id
		@Column(name = "ID")
		@GeneratedValue
		private Integer id;

		@OneToOne(mappedBy = "description", cascade = CascadeType.ALL)
		private Show show;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Show getShow() {
			return show;
		}

		public void setShow(Show show) {
			this.show = show;
		}
	}
}
