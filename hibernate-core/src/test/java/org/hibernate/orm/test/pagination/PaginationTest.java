/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.pagination;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.Session;

import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Gavin King
 */
public class PaginationTest extends BaseNonConfigCoreFunctionalTestCase {
	public static final int NUMBER_OF_TEST_ROWS = 100;

	@Override
	protected String getBaseForMappings() {
		return "org/hibernate/orm/test/";
	}

	@Override
	public String[] getMappings() {
		return new String[] { "pagination/DataPoint.hbm.xml" };
	}

	@Override
	public String getCacheConcurrencyStrategy() {
		return null;
	}

	@Test
	public void testLimit() {
		inTransaction(
				session -> {
					int count;

					count = generateBaseHQLQuery( session )
							.setMaxResults( 5 )
							.list()
							.size();
					assertEquals( 5, count );

					count = generateBaseQuery( session )
							.setMaxResults( 18 )
							.list()
							.size();
					assertEquals( 18, count );

					count = generateBaseSQLQuery( session )
							.setMaxResults( 13 )
							.list()
							.size();
					assertEquals( 13, count );
				}
		);
	}

	@Test
	public void testOffset() {
		inTransaction(
				session -> {
					List result;

					result = generateBaseHQLQuery( session )
							.setFirstResult( 3 )
							.list();
					DataPoint firstDataPointHQL = (DataPoint) result.get( 0 );

					result = generateBaseQuery( session )
							.setFirstResult( 3 )
							.list();
					DataPoint firstDataPointCriteria = (DataPoint) result.get( 0 );

					assertEquals(
							"The first entry should be the same in HQL and Criteria",
							firstDataPointHQL,
							firstDataPointHQL
					);
					assertEquals( "Wrong first result", 3, firstDataPointCriteria.getSequence() );
				}
		);
	}

	@Test
	public void testLimitOffset() {
		inTransaction(
				session -> {
					List result;

					log.info("Query1: from 0 for 20");
					result = generateBaseHQLQuery( session )
							.setFirstResult( 0 )
							.setMaxResults( 20 )
							.list();
					assertEquals( 20, result.size() );
					assertEquals( 0, ( (DataPoint) result.get( 0 ) ).getSequence() );
					assertEquals( 1, ( (DataPoint) result.get( 1 ) ).getSequence() );

					log.info("Query2: from 1 for 20");
					result = generateBaseQuery( session )
							.setFirstResult( 1 )
							.setMaxResults( 20 )
							.list();
					assertEquals( 20, result.size() );
					assertEquals( 1, ( (DataPoint) result.get( 0 ) ).getSequence() );
					assertEquals( 2, ( (DataPoint) result.get( 1 ) ).getSequence() );

					log.info("Query3: from 99 for " + (Integer.MAX_VALUE - 200));
					result = generateBaseQuery( session )
							.setFirstResult( 99 )
							.setMaxResults( Integer.MAX_VALUE - 200 )
							.list();
					assertEquals( 1, result.size() );
					assertEquals( 99, ( (DataPoint) result.get( 0 ) ).getSequence() );

					log.info("Query4: from 2 for 3");
					result = session.createQuery( "select distinct description from DataPoint order by description" )
							.setFirstResult( 2 )
							.setMaxResults( 3 )
							.list();
					assertEquals( 3, result.size() );
					assertEquals( "Description: 2", result.get( 0 ) );
					assertEquals( "Description: 3", result.get( 1 ) );
					assertEquals( "Description: 4", result.get( 2 ) );

					log.info("Query5: from 2 for 5");
					result = session.createNativeQuery(
							"select description, xval, yval from DataPoint order by xval, yval" )
							.setFirstResult( 2 )
							.setMaxResults( 5 )
							.list();
					assertEquals( 5, result.size() );
					Object[] row = (Object[]) result.get( 0 );
					assertTrue( row[0] instanceof String );

					log.info("Query6: from 2 for 5");
					result = session.createNativeQuery( "select * from DataPoint order by xval, yval" )
							.setFirstResult( 2 )
							.setMaxResults( 5 )
							.list();
					assertEquals( 5, result.size() );

				}
		);
	}

	private Query generateBaseHQLQuery(Session session) {
		return session.createQuery( "select dp from DataPoint dp order by dp.sequence" );
	}

	private Query generateBaseQuery(Session session) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<DataPoint> criteria = criteriaBuilder.createQuery( DataPoint.class );
		Root<DataPoint> root = criteria.from( DataPoint.class );
		return session.createQuery( criteria.orderBy( criteriaBuilder.asc( root ) ) );
//		return session.createCriteria( DataPoint.class )
//				.addOrder( Order.asc( "sequence" ) );
	}

	private NativeQuery generateBaseSQLQuery(Session session) {
		return session.createNativeQuery( "select id, seqval, xval, yval, description from DataPoint order by seqval" )
				.addEntity( DataPoint.class );
	}

	@Before
	public void prepareTestData() {
		inTransaction(
				session -> {
					for ( int i = 0; i < NUMBER_OF_TEST_ROWS; i++ ) {
						DataPoint dataPoint = new DataPoint();
						dataPoint.setSequence( i );
						dataPoint.setDescription( "data point #" + i );
						BigDecimal x = new BigDecimal( i * 0.1d ).setScale( 19, BigDecimal.ROUND_DOWN );
						dataPoint.setX( x );
						dataPoint.setY( new BigDecimal( Math.cos( x.doubleValue() ) ).setScale(
								19,
								BigDecimal.ROUND_DOWN
						) );
						dataPoint.setDescription( "Description: " + i % 5 );
						session.save( dataPoint );
					}
				}
		);
	}

	@After
	public void cleanupTestData() {
		Session session = openSession();
		session.beginTransaction();
		session.createQuery( "delete DataPoint" ).executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
}
