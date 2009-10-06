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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import domain.DomainClass;
import domain.SimpleValueObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:context.xml" })
@Transactional
public class HibernateTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private DataSource dataSource;

	@Test
	public void shouldNotQueryFirsLevelCacheOnCriteria() {
		Session session = sessionFactory.getCurrentSession();
		session.persist(getDefaultData(1L, "NOK"));

		// Remove everything
		flushAndClearCaches(session);

		// Make sure a normal search works
		DomainClass object = searchWithCriteria(session);
		assertNotNull(object);

		// Bypass the session and delete in the database
		JdbcTemplate template = new JdbcTemplate(dataSource);
		assertNumberOfDomainClasses(1, template);

		deleteAll(template);
		assertNumberOfDomainClasses(0, template);

		// Do search with ID
		assertNotNull(session.get(DomainClass.class, 1L));
		// Do search with Query
		assertEquals(null, searchWithCriteria(session));
	}

	@Test
	public void shouldHitSecondLevelCache() {
		Session session = sessionFactory.getCurrentSession();
		session.persist(getDefaultData(2L, "SEK"));

		flushAndClearCaches(session);

		SecondLevelCacheStatistics cacheStats = sessionFactory.getStatistics()
				.getSecondLevelCacheStatistics(
						SimpleValueObject.class.getCanonicalName());

		assertEquals(0, cacheStats.getElementCountInMemory());

		DomainClass domain = (DomainClass) session.get(DomainClass.class, 2L);
		assertNotNull(domain.getValue().getContent());
		assertEquals(0, cacheStats.getHitCount());
		assertEquals(1, cacheStats.getMissCount());

		session.clear();
		domain = (DomainClass) session.get(DomainClass.class, 2L);
		assertNotNull(domain.getValue().getContent());
		assertEquals(1, cacheStats.getHitCount());
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

	private void assertNumberOfDomainClasses(int i, JdbcTemplate template) {
		Integer count = countEntries(template);
		assertEquals((Integer) i, count);
	}

	private DomainClass getDefaultData(Long id, String value) {
		return new DomainClass(id, "Name", new SimpleValueObject(value, "val"));
	}

	private DomainClass searchWithCriteria(Session session) {
		DetachedCriteria searchCriteria = DetachedCriteria
				.forClass(DomainClass.class);
		searchCriteria.add(Restrictions.eq("id", 1L));
		DomainClass object = (DomainClass) searchCriteria
				.getExecutableCriteria(session).uniqueResult();
		return object;
	}

	private Integer countEntries(JdbcTemplate template) {
		Integer count = (Integer) template.execute(new StatementCallback() {

			@Override
			public Object doInStatement(Statement stmt) throws SQLException,
					DataAccessException {
				ResultSet rs = stmt
						.executeQuery("SELECT COUNT(*) FROM DOMAIN_TABLE");
				rs.next();
				return rs.getInt(1);
			}
		});
		return count;
	}

	private void deleteAll(JdbcTemplate template) {
		template.execute("DELETE FROM DOMAIN_TABLE");
		template.execute("DELETE FROM VALUE_TABLE");
	}
}
