import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import db.Tables;
import domain.Child;
import domain.DomainClass;
import domain.Currency;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:context.xml" })
@Transactional
public class HibernateTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private DataSource dataSource;

	@Test
	public void shouldAllowUnpersistedValueObjectWhenAlreadyPresentInDatabase() {
		Session session = sessionFactory.getCurrentSession();
		String currencyKey = "NOK";
		persistCurrency(session, currencyKey);
		flushAndClearCaches(session);
		
		assertNumberOfObjectsInDatabase(1, Tables.CURRENCY_TABLE);

		session.persist(getDefaultData(1L, currencyKey));
		session.flush();
		
		assertNumberOfObjectsInDatabase(1, Tables.CURRENCY_TABLE);
	}

	private void persistCurrency(Session session, String currencyKey) {
		session.persist(new Currency(currencyKey, "Description"));
	}

	@Test
	public void shouldNotQueryFirsLevelCacheOnCriteria() {
		Session session = sessionFactory.getCurrentSession();
		persistDefaultData(session, 1L, "NOK");

		// Remove everything
		flushAndClearCaches(session);

		// Make sure a normal search works
		DomainClass object = searchWithCriteria(session);
		assertNotNull(object);

		// Bypass the session and delete in the database
		assertNumberOfObjectsInDatabase(1, Tables.DOMAIN_TABLE);

		deleteAll();
		assertNumberOfObjectsInDatabase(0, Tables.DOMAIN_TABLE);

		// Do search with ID
		assertNotNull(session.get(DomainClass.class, 1L));
		// Do search with Query
		assertEquals(null, searchWithCriteria(session));
	}

	private void persistDefaultData(Session session, long domainId,
			String currencyKey) {
		persistCurrency(session, currencyKey);
		session.persist(getDefaultData(domainId, currencyKey));
	}

	@Test
	public void shouldHitSecondLevelCache() {
		Session session = sessionFactory.getCurrentSession();
		persistDefaultData(session, 2L, "SEK");

		flushAndClearCaches(session);

		SecondLevelCacheStatistics cacheStats = sessionFactory.getStatistics()
				.getSecondLevelCacheStatistics(
						Currency.class.getCanonicalName());

		assertEquals(0, cacheStats.getElementCountInMemory());

		DomainClass domain = (DomainClass) session.get(DomainClass.class, 2L);
		assertNotNull(domain.getCurrency().getContent());
		assertEquals(0, cacheStats.getHitCount());
		assertEquals(1, cacheStats.getMissCount());

		session.clear();
		domain = (DomainClass) session.get(DomainClass.class, 2L);
		assertNotNull(domain.getCurrency().getContent());
		assertEquals(1, cacheStats.getHitCount());
	}

	@Test
	public void shouldInsertParentChildRelationshipInTheCorrectOrder() {
		Session session = sessionFactory.getCurrentSession();
		persistCurrency(session, "NOK");
		DomainClass domain = getDefaultData(3L, "NOK");

		domain.addChild(new Child("1", "Child1"));

		session.persist(domain);
		flushAndClearCaches(session);
	}

	private void flushAndClearCaches(Session session) {
		session.flush();
		evictSecondLevelCache();
		session.clear();
	}

	@SuppressWarnings("unchecked")
	private void evictSecondLevelCache() {
		Map<String, CollectionMetadata> roleMap = sessionFactory
				.getAllCollectionMetadata();
		for (String roleName : roleMap.keySet()) {
			sessionFactory.evictCollection(roleName);
		}

		Map<String, ClassMetadata> entityMap = sessionFactory
				.getAllClassMetadata();
		for (String entityName : entityMap.keySet()) {
			sessionFactory.evictEntity(entityName);
		}

		sessionFactory.evictQueries();
	}

	private void printTableNames(JdbcTemplate template) {
		template.execute(new ConnectionCallback() {

			@Override
			public Object doInConnection(Connection con) throws SQLException,
					DataAccessException {
				ResultSet rs = con.getMetaData().getTables(null, null, null,
						new String[] { "TABLE" });
				while (rs.next()) {
					System.out.println(rs.getString("TABLE_NAME"));
				}

				return null;
			}
		});
	}

	private void assertNumberOfObjectsInDatabase(int i, String table) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		Integer count = countEntriesInDatabase(template, table);
		assertEquals((Integer) i, count);
	}

	private DomainClass getDefaultData(Long id, String value) {
		return new DomainClass(id, "Name", new Currency(value, "val"));
	}

	private DomainClass searchWithCriteria(Session session) {
		DetachedCriteria searchCriteria = DetachedCriteria
				.forClass(DomainClass.class);
		searchCriteria.add(Restrictions.eq("id", 1L));
		DomainClass object = (DomainClass) searchCriteria
				.getExecutableCriteria(session).uniqueResult();
		return object;
	}

	private Integer countEntriesInDatabase(JdbcTemplate template, final String tableName) {
		Integer count = (Integer) template.execute(new StatementCallback() {

			@Override
			public Object doInStatement(Statement stmt) throws SQLException,
					DataAccessException {
				ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM "
						+ tableName);
				rs.next();
				return rs.getInt(1);
			}
		});
		return count;
	}

	private void deleteAll() {
		SimpleJdbcTestUtils.deleteFromTables(new SimpleJdbcTemplate(dataSource),
				Tables.DOMAIN_TABLE, Tables.CURRENCY_TABLE, Tables.CHILD_TABLE);
	}
}
