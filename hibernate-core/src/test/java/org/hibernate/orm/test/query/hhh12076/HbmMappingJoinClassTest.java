/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.query.hhh12076;

import java.util.List;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@TestForIssue(jiraKey = "HHH-12076")
@DomainModel(
		xmlMappings = {
				"org/hibernate/query/hhh12076/Claim.hbm.xml",
				"org/hibernate/query/hhh12076/EwtAssessmentExtension.hbm.xml",
				"org/hibernate/query/hhh12076/Extension.hbm.xml",
				"org/hibernate/query/hhh12076/GapAssessmentExtension.hbm.xml",
				"org/hibernate/query/hhh12076/Settlement.hbm.xml",
				"org/hibernate/query/hhh12076/SettlementExtension.hbm.xml",
				"org/hibernate/query/hhh12076/SettlementTask.hbm.xml",
				"org/hibernate/query/hhh12076/Task.hbm.xml",
				"org/hibernate/query/hhh12076/TaskStatus.hbm.xml",
		}
)
@SessionFactory
public class HbmMappingJoinClassTest {

	@BeforeEach
	protected void prepareTest(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			TaskStatus taskStatus = new TaskStatus();
			taskStatus.setName( "Enabled" );
			taskStatus.setDisplayName( "Enabled" );
			session.persist( taskStatus );

			for ( long i = 0; i < 10; i++ ) {
				SettlementTask settlementTask = new SettlementTask();
				Settlement settlement = new Settlement();
				settlementTask.setLinked( settlement );
				settlementTask.setStatus( taskStatus );

				Claim claim = new Claim();
				settlement.setClaim( claim );

				for ( int j = 0; j < 2; j++ ) {
					GapAssessmentExtension gapAssessmentExtension = new GapAssessmentExtension();
					gapAssessmentExtension.setSettlement( settlement );
					EwtAssessmentExtension ewtAssessmentExtension = new EwtAssessmentExtension();
					ewtAssessmentExtension.setSettlement( settlement );

					settlement.getExtensions().add( gapAssessmentExtension );
					settlement.getExtensions().add( ewtAssessmentExtension );
				}
				session.persist( claim );
				session.persist( settlement );
				session.persist( settlementTask );
			}
		} );
	}

	@Test
	public void testClassExpressionInOnClause(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			List<SettlementTask> results = session.createQuery(
							"select " +
									"	rootAlias.id, " +
									"	linked.id, " +
									"	extensions.id " +
									"from SettlementTask as rootAlias " +
									"join rootAlias.linked as linked " +
									"left join linked.extensions as extensions " +
									"	on extensions.class = org.hibernate.orm.test.query.hhh12076.EwtAssessmentExtension " +
									"where linked.id = :claimId" )
					.setParameter( "claimId", 1L )
					.getResultList();

			assertNotNull( results );
		} );
	}
}
