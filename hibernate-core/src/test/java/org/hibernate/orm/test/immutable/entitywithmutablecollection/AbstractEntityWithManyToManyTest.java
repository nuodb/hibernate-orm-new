/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.immutable.entitywithmutablecollection;

import java.util.Iterator;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metamodel.MappingMetamodel;

import org.hibernate.testing.BeforeClassOnce;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hibernate.testing.orm.junit.ExtraAssertions.assertTyping;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author Gail Badner
 */
@SessionFactory(generateStatistics = true)
public abstract class AbstractEntityWithManyToManyTest {
	private boolean isPlanContractsInverse;
	private boolean isPlanContractsBidirectional;
	private boolean isPlanVersioned;
	private boolean isContractVersioned;


	@BeforeAll
	@BeforeClass
	protected static void removeSchema() {
		Configuration config = new Configuration();
		config.configure();

		try (org.hibernate.SessionFactory sf = config.buildSessionFactory()){
			sf.inSession(session -> {
				Transaction txn = session.getTransaction();
				txn.begin();
				session.createNativeQuery("DROP SCHEMA DBO CASCADE").executeUpdate();
				txn.commit();
			});
		}
	}
	@BeforeEach
	protected void prepareTest(SessionFactoryScope scope) throws Exception {

		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();
		MappingMetamodel domainModel = sessionFactory.getRuntimeMetamodels().getMappingMetamodel();
		isPlanContractsInverse = domainModel.getCollectionDescriptor( Plan.class.getName() + ".contracts" )
				.isInverse();
		try {
			domainModel.getCollectionDescriptor( Contract.class.getName() + ".plans" );
			isPlanContractsBidirectional = true;
		}
		catch (IllegalArgumentException ex) {
			isPlanContractsBidirectional = false;
		}
        isPlanVersioned = sessionFactory.getMappingMetamodel().getEntityDescriptor(Plan.class.getName()).isVersioned();
        isContractVersioned = sessionFactory.getMappingMetamodel().getEntityDescriptor(Contract.class.getName()).isVersioned();
		sessionFactory.getStatistics().clear();
	}

	@Test
	public void testUpdateProperty(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p = new Plan( "plan" );
					p.addContract( new Contract( null, "gail", "phone" ) );
					s.persist( p );
				}
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p = getPlan( s );
					p.setDescription( "new plan" );
					assertEquals( 1, p.getContracts().size() );
					Contract c = (Contract) p.getContracts().iterator().next();
					c.setCustomerName( "yogi" );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p = getPlan( s );
					assertEquals( 1, p.getContracts().size() );
					Contract c = (Contract) p.getContracts().iterator().next();
					assertEquals( "gail", c.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertEquals( 1, c.getPlans().size() );
						assertSame( p, c.getPlans().iterator().next() );
					}
					s.delete( p );

					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testCreateWithNonEmptyManyToManyCollectionOfNew(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p = new Plan( "plan" );
					p.addContract( new Contract( null, "gail", "phone" ) );
					s.persist( p );
				}
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p = getPlan( s );
					assertEquals( 1, p.getContracts().size() );
					Contract c = (Contract) p.getContracts().iterator().next();
					assertEquals( "gail", c.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertEquals( 1, c.getPlans().size() );
						assertSame( p, c.getPlans().iterator().next() );
					}
					s.delete( p );

					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testCreateWithNonEmptyManyToManyCollectionOfExisting(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Contract c = new Contract( null, "gail", "phone" );
		scope.inTransaction(
				s -> s.persist( c )
		);

		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p = new Plan( "plan" );
					p.addContract( c );
					s.save( p );
				}
		);

		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( isContractVersioned ? 1 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 1, p1.getContracts().size() );
					Contract c1 = (Contract) p1.getContracts().iterator().next();
					assertEquals( "gail", c1.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertEquals( 1, c1.getPlans().size() );
						assertSame( p1, c1.getPlans().iterator().next() );
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testAddNewManyToManyElementToPersistentEntity(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		scope.inTransaction(
				s -> {
					s.persist( p );
				}
		);

		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = s.get( Plan.class, p.getId() );
					assertEquals( 0, p.getContracts().size() );
					p1.addContract( new Contract( null, "gail", "phone" ) );
				}
		);

		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( isContractVersioned ? 1 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 1, p1.getContracts().size() );
					Contract c = (Contract) p1.getContracts().iterator().next();
					assertEquals( "gail", c.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertEquals( 1, c.getPlans().size() );
						assertSame( p1, c.getPlans().iterator().next() );
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testAddExistingManyToManyElementToPersistentEntity(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		scope.inTransaction(
				s -> {
					s.persist( p );
					s.persist( c );
				}
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = s.get( Plan.class, p.getId() );
					assertEquals( 0, p1.getContracts().size() );
					Contract c1 = s.get( Contract.class, c.getId() );
					if ( isPlanContractsBidirectional ) {
						assertEquals( 0, c1.getPlans().size() );
					}
					p1.addContract( c1 );
				}
		);

		assertInsertCount( 0, sessionFactory );
		assertUpdateCount( isContractVersioned && isPlanVersioned ? 2 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 1, p1.getContracts().size() );
					Contract c1 = (Contract) p1.getContracts().iterator().next();
					assertEquals( "gail", c1.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertSame( p1, c1.getPlans().iterator().next() );
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testCreateWithEmptyManyToManyCollectionUpdateWithExistingElement(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		scope.inTransaction(
				s -> {
					s.persist( p );
					s.persist( c );
				}
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		p.addContract( c );

		scope.inTransaction(
				s -> s.update( p )
		);

		assertInsertCount( 0, sessionFactory );
		assertUpdateCount( isContractVersioned && isPlanVersioned ? 2 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 1, p1.getContracts().size() );
					Contract c1 = (Contract) p1.getContracts().iterator().next();
					assertEquals( "gail", c1.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertSame( p1, c1.getPlans().iterator().next() );
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testCreateWithNonEmptyManyToManyCollectionUpdateWithNewElement(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		p.addContract( c );
		scope.inTransaction(
				s -> s.persist( p )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		Contract newC = new Contract( null, "sherman", "telepathy" );
		p.addContract( newC );

		scope.inTransaction(
				s -> s.update( p )
		);

		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( isContractVersioned ? 1 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 2, p1.getContracts().size() );
					for ( Iterator it = p1.getContracts().iterator(); it.hasNext(); ) {
						Contract aContract = (Contract) it.next();
						if ( aContract.getId() == c.getId() ) {
							assertEquals( "gail", aContract.getCustomerName() );
						}
						else if ( aContract.getId() == newC.getId() ) {
							assertEquals( "sherman", aContract.getCustomerName() );
						}
						else {
							fail( "unknown contract" );
						}
						if ( isPlanContractsBidirectional ) {
							assertSame( p1, aContract.getPlans().iterator().next() );
						}
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 3, sessionFactory );
	}

	@Test
	public void testCreateWithEmptyManyToManyCollectionMergeWithExistingElement(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );

		scope.inTransaction(
				s -> {
					s.persist( p );
					s.persist( c );
				}
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		p.addContract( c );

		scope.inTransaction(
				s -> s.merge( p )
		);

		assertInsertCount( 0, sessionFactory );
		assertUpdateCount( isContractVersioned && isPlanVersioned ? 2 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 1, p1.getContracts().size() );
					Contract c1 = (Contract) p1.getContracts().iterator().next();
					assertEquals( "gail", c1.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertSame( p1, c1.getPlans().iterator().next() );
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testCreateWithNonEmptyManyToManyCollectionMergeWithNewElement(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		p.addContract( c );
		scope.inTransaction(
				s -> s.persist( p )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		Contract newC = new Contract( null, "yogi", "mail" );
		p.addContract( newC );

		scope.inTransaction(
				s -> s.merge( p )
		);

		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( isContractVersioned && isPlanVersioned ? 2 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 2, p1.getContracts().size() );
					for ( Object o : p1.getContracts() ) {
						Contract aContract = (Contract) o;
						if ( aContract.getId() == c.getId() ) {
							assertEquals( "gail", aContract.getCustomerName() );
						}
						else if ( !aContract.getCustomerName().equals( newC.getCustomerName() ) ) {
							fail( "unknown contract:" + aContract.getCustomerName() );
						}
						if ( isPlanContractsBidirectional ) {
							assertSame( p1, aContract.getPlans().iterator().next() );
						}
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 3, sessionFactory );
	}

	@Test
	public void testRemoveManyToManyElementUsingUpdate(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		p.addContract( c );

		scope.inTransaction(
				s -> s.persist( p )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		p.removeContract( c );
		assertEquals( 0, p.getContracts().size() );
		if ( isPlanContractsBidirectional ) {
			assertEquals( 0, c.getPlans().size() );
		}
		scope.inTransaction(
				s -> s.update( p )
		);

		assertUpdateCount( isContractVersioned ? 1 : 0, sessionFactory );
		assertDeleteCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					if ( isPlanContractsInverse ) {
						assertEquals( 1, p1.getContracts().size() );
						Contract c1 = (Contract) p1.getContracts().iterator().next();
						assertEquals( "gail", c1.getCustomerName() );
						assertSame( p1, c1.getPlans().iterator().next() );
					}
					else {
						assertEquals( 0, p1.getContracts().size() );
						Contract c1 = getContract( s );
						if ( isPlanContractsBidirectional ) {
							assertEquals( 0, c1.getPlans().size() );
						}
						s.delete( c1 );
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testRemoveManyToManyElementUsingUpdateBothSides(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		p.addContract( c );

		scope.inTransaction(
				s -> s.persist( p )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		p.removeContract( c );
		assertEquals( 0, p.getContracts().size() );
		if ( isPlanContractsBidirectional ) {
			assertEquals( 0, c.getPlans().size() );
		}
		scope.inTransaction(
				s -> {
					s.update( p );
					s.update( c );
				}
		);

		assertUpdateCount( isContractVersioned && isPlanVersioned ? 2 : 0, sessionFactory );
		assertDeleteCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 0, p1.getContracts().size() );
					Contract c1 = getContract( s );
					if ( isPlanContractsBidirectional ) {
						assertEquals( 0, c1.getPlans().size() );
					}
					s.delete( c1 );
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testRemoveManyToManyElementUsingMerge(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		p.addContract( c );

		scope.inTransaction(
				s -> s.persist( p )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		p.removeContract( c );
		assertEquals( 0, p.getContracts().size() );
		if ( isPlanContractsBidirectional ) {
			assertEquals( 0, c.getPlans().size() );
		}

		scope.inTransaction(
				s -> s.merge( p )
		);

		assertUpdateCount( isContractVersioned ? 1 : 0, sessionFactory );
		assertDeleteCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					if ( isPlanContractsInverse ) {
						assertEquals( 1, p1.getContracts().size() );
						Contract c1 = (Contract) p1.getContracts().iterator().next();
						assertEquals( "gail", c1.getCustomerName() );
						assertSame( p1, c1.getPlans().iterator().next() );
					}
					else {
						assertEquals( 0, p1.getContracts().size() );
						Contract c1 = (Contract) getContract( s );
						if ( isPlanContractsBidirectional ) {
							assertEquals( 0, c1.getPlans().size() );
						}
						s.delete( c1 );
					}
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testRemoveManyToManyElementUsingMergeBothSides(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		p.addContract( c );

		scope.inTransaction(
				s -> s.persist( p )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		p.removeContract( c );
		assertEquals( 0, p.getContracts().size() );
		if ( isPlanContractsBidirectional ) {
			assertEquals( 0, c.getPlans().size() );
		}

		scope.inTransaction(
				s -> {
					s.merge( p );
					s.merge( c );
				}
		);

		assertUpdateCount( isContractVersioned && isPlanVersioned ? 2 : 0, sessionFactory );
		assertDeleteCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 0, p1.getContracts().size() );
					Contract c1 = getContract( s );
					if ( isPlanContractsBidirectional ) {
						assertEquals( 0, c1.getPlans().size() );
					}
					s.delete( c1 );
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 2, sessionFactory );
	}

	@Test
	public void testDeleteManyToManyElement(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		p.addContract( c );

		scope.inTransaction(
				s -> s.persist( p )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					s.update( p );
					p.removeContract( c );
					s.delete( c );
				}
		);

		assertUpdateCount( isContractVersioned ? 1 : 0, sessionFactory );
		assertDeleteCount( 1, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 0, p1.getContracts().size() );
					Contract c1 = getContract( s );
					assertNull( c1 );
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 1, sessionFactory );
	}

	@Test
	public void testRemoveManyToManyElementByDelete(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract c = new Contract( null, "gail", "phone" );
		p.addContract( c );

		scope.inTransaction(
				s -> s.persist( p )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		p.removeContract( c );
		assertEquals( 0, p.getContracts().size() );
		if ( isPlanContractsBidirectional ) {
			assertEquals( 0, c.getPlans().size() );
		}

		scope.inTransaction(
				s -> {
					s.update( p );
					s.delete( c );
				}
		);

		assertUpdateCount( isPlanVersioned ? 1 : 0, sessionFactory );
		assertDeleteCount( 1, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 0, p1.getContracts().size() );
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 1, sessionFactory );
	}

	@Test
	public void testManyToManyCollectionOptimisticLockingWithMerge(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan pOrig = new Plan( "plan" );
		Contract cOrig = new Contract( null, "gail", "phone" );
		pOrig.addContract( cOrig );
		scope.inTransaction(
				s -> s.persist( pOrig )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p = s.get( Plan.class, pOrig.getId() );
					Contract newC = new Contract( null, "sherman", "note" );
					p.addContract( newC );
				}
		);


		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( isContractVersioned ? 1 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inSession(
				s -> {
					pOrig.removeContract( cOrig );
					try {
						s.merge( pOrig );
						assertFalse( isContractVersioned );
					}
					catch (PersistenceException ex) {
						assertTyping( StaleObjectStateException.class, ex.getCause() );
						assertTrue( isContractVersioned );
					}
					finally {
						s.getTransaction().rollback();
					}
				}
		);


		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					s.delete( p1 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 3, sessionFactory );
	}

	@Test
	public void testManyToManyCollectionOptimisticLockingWithUpdate(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan pOrig = new Plan( "plan" );
		Contract cOrig = new Contract( null, "gail", "phone" );
		pOrig.addContract( cOrig );
		scope.inTransaction(
				s -> s.persist( pOrig )
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p = s.get( Plan.class, pOrig.getId() );
					Contract newC = new Contract( null, "yogi", "pawprint" );
					p.addContract( newC );
				}
		);

		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( isContractVersioned ? 1 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inSession(
				s -> {
					s.beginTransaction();
					pOrig.removeContract( cOrig );
					s.update( pOrig );
					try {
						s.getTransaction().commit();
						assertFalse( isContractVersioned );
					}
					catch (PersistenceException ex) {
						s.getTransaction().rollback();
						assertTrue( isContractVersioned );
						if ( !scope.getSessionFactory().getSessionFactoryOptions().isJdbcBatchVersionedData() ) {
							assertTyping( StaleObjectStateException.class, ex.getCause() );
						}
						else {
							assertTyping( StaleStateException.class, ex.getCause() );
						}
					}
				}
		);

		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					s.delete( p1 );
					s.createQuery( "delete from Contract" ).executeUpdate();
					assertAllPlansAndContractsAreDeleted( s );
				}
		);
	}

	@Test
	public void testMoveManyToManyElementToNewEntityCollection(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		scope.inTransaction(
				s -> {
					p.addContract( new Contract( null, "gail", "phone" ) );
					s.persist( p );
				}
		);

		assertInsertCount( 2, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );

		Plan p2 = new Plan( "new plan" );
		scope.inTransaction(
				s -> {
					Plan p1 = getPlan( s );
					assertEquals( 1, p1.getContracts().size() );
					Contract c = (Contract) p1.getContracts().iterator().next();
					assertEquals( "gail", c.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertSame( p1, c.getPlans().iterator().next() );
					}
					p1.removeContract( c );

					p2.addContract( c );
					s.save( p2 );
				}
		);

		assertInsertCount( 1, sessionFactory );
		assertUpdateCount( isPlanVersioned && isContractVersioned ? 2 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p3 = getPlan( s, p.getId() );
					Plan p4 = getPlan( s, p2.getId() );
		/*
		if ( isPlanContractsInverse ) {
			assertEquals( 1, p1.getContracts().size() );
			c = ( Contract ) p1.getContracts().iterator().next();
			assertEquals( "gail", c.getCustomerName() );
			if ( isPlanContractsBidirectional ) {
				assertSame( p1, c.getPlans().iterator().next() );
			}
			assertEquals( 0, p2.getContracts().size() );
		}
		else {
		*/
					assertEquals( 0, p3.getContracts().size() );
					assertEquals( 1, p4.getContracts().size() );
					Contract c = (Contract) p4.getContracts().iterator().next();
					assertEquals( "gail", c.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertSame( p4, c.getPlans().iterator().next() );
					}
					//}
					s.delete( p3 );
					s.delete( p4 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 3, sessionFactory );
	}

	@Test
	public void testMoveManyToManyElementToExistingEntityCollection(SessionFactoryScope scope) {
		SessionFactoryImplementor sessionFactory = scope.getSessionFactory();

		clearCounts( sessionFactory );

		Plan p = new Plan( "plan" );
		Contract contract = new Contract( null, "gail", "phone" );
		p.addContract( contract );
		Plan p2 = new Plan( "plan2" );
		scope.inTransaction(
				s -> {
					s.persist( p );
					s.persist( p2 );
				}
		);

		assertInsertCount( 3, sessionFactory );
		assertUpdateCount( 0, sessionFactory );
		clearCounts( sessionFactory );


		scope.inTransaction(
				s -> {
					Plan p3 = getPlan( s, p.getId() );
					assertEquals( 1, p3.getContracts().size() );
					Contract c = (Contract) p3.getContracts().iterator().next();
					assertEquals( "gail", c.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertSame( p3, c.getPlans().iterator().next() );
					}
					p3.removeContract( c );
				}
		);

		assertInsertCount( 0, sessionFactory );
		assertUpdateCount( isPlanVersioned && isContractVersioned ? 2 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p3 = getPlan( s, p2.getId() );
					Contract c1 = getContract( s, contract.getId() );
					p3.addContract( c1 );
				}
		);

		assertInsertCount( 0, sessionFactory );
		assertUpdateCount( isPlanVersioned && isContractVersioned ? 2 : 0, sessionFactory );
		clearCounts( sessionFactory );

		scope.inTransaction(
				s -> {
					Plan p3 = getPlan( s, p.getId() );
					Plan p4 = getPlan( s, p2.getId() );
		/*
		if ( isPlanContractsInverse ) {
			assertEquals( 1, p3.getContracts().size() );
			c = ( Contract ) p3.getContracts().iterator().next();
			assertEquals( "gail", c.getCustomerName() );
			if ( isPlanContractsBidirectional ) {
				assertSame( p3, c.getPlans().iterator().next() );
			}
			assertEquals( 0, p4.getContracts().size() );
		}
		else {
		*/
					assertEquals( 0, p3.getContracts().size() );
					assertEquals( 1, p4.getContracts().size() );
					Contract c1 = (Contract) p4.getContracts().iterator().next();
					assertEquals( "gail", c1.getCustomerName() );
					if ( isPlanContractsBidirectional ) {
						assertSame( p4, c1.getPlans().iterator().next() );
					}
					//}
					s.delete( p3 );
					s.delete( p4 );
					assertAllPlansAndContractsAreDeleted( s );
				}
		);

		assertUpdateCount( 0, sessionFactory );
		assertDeleteCount( 3, sessionFactory );
	}

	private void assertAllPlansAndContractsAreDeleted(SessionImplementor s) {
		assertEquals( new Long( 0 ), getPlanRowCount( s ) );
		assertEquals( new Long( 0 ), getContractRowCount( s ) );
	}

	private Plan getPlan(SessionImplementor s) {
		CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
		CriteriaQuery<Plan> criteria = criteriaBuilder.createQuery( Plan.class );
		criteria.from( Plan.class );
		return s.createQuery( criteria ).uniqueResult();
	}

	private Plan getPlan(SessionImplementor s, long id) {
		CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
		CriteriaQuery<Plan> criteria = criteriaBuilder.createQuery( Plan.class );
		Root<Plan> root = criteria.from( Plan.class );
		criteria.where( criteriaBuilder.equal( root.get( "id" ), id ) );
		return s.createQuery( criteria ).uniqueResult();
	}

	private Contract getContract(SessionImplementor s) {
		CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
		CriteriaQuery<Contract> criteria = criteriaBuilder.createQuery( Contract.class );
		criteria.from( Contract.class );
		return s.createQuery( criteria ).uniqueResult();
	}

	private Contract getContract(SessionImplementor s, long id) {
		CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
		CriteriaQuery<Contract> criteria = criteriaBuilder.createQuery( Contract.class );
		Root<Contract> root = criteria.from( Contract.class );
		criteria.where( criteriaBuilder.equal( root.get( "id" ), id ) );
		return s.createQuery( criteria ).uniqueResult();
	}

	private Long getPlanRowCount(SessionImplementor s) {
		CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaPlanRowCount = criteriaBuilder.createQuery( Long.class );
		criteriaPlanRowCount.select( criteriaBuilder.count( criteriaPlanRowCount.from( Plan.class ) ) );
		return s.createQuery( criteriaPlanRowCount ).uniqueResult();
	}

	private Long getContractRowCount(SessionImplementor s) {
		CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaContractRowCount = criteriaBuilder.createQuery( Long.class );
		criteriaContractRowCount.select( criteriaBuilder.count( criteriaContractRowCount.from( Contract.class ) ) );
		return s.createQuery( criteriaContractRowCount ).uniqueResult();
	}

	protected void clearCounts(SessionFactoryImplementor sessionFactory) {
		sessionFactory.getStatistics().clear();
	}

	protected void assertInsertCount(int expected, SessionFactoryImplementor sessionFactory) {
		int inserts = (int) sessionFactory.getStatistics().getEntityInsertCount();
		assertEquals( expected, inserts, "unexpected insert count" );
	}

	protected void assertUpdateCount(int expected, SessionFactoryImplementor sessionFactory) {
		int updates = (int) sessionFactory.getStatistics().getEntityUpdateCount();
		assertEquals( expected, updates, "unexpected update counts" );
	}

	protected void assertDeleteCount(int expected, SessionFactoryImplementor sessionFactory) {
		int deletes = (int) sessionFactory.getStatistics().getEntityDeleteCount();
		assertEquals( expected, deletes, "unexpected delete counts" );
	}
}
