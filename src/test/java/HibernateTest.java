import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import domain.DomainClass;

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
		session.persist(new DomainClass(1L, "Name"));

		// Make sure a normal search works
		DomainClass object = searchWithCriteria(session);
		assertNotNull(object);

		// Bypass the session and delete in the database
		JdbcTemplate template = new JdbcTemplate(dataSource);
		deleteAll(template);
		Integer count = countEntries(template);
		assertEquals((Integer) 0, count);

		// Do search with ID
		assertNotNull(session.get(DomainClass.class, 1L));
		// Do search with Query
		assertEquals(null, searchWithCriteria(session));
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
						.executeQuery("SELECT COUNT(*) FROM DOMAIN_CLASS");
				rs.next();
				return rs.getInt(1);
			}
		});
		return count;
	}

	private void deleteAll(JdbcTemplate template) {
		template.execute("DELETE FROM DOMAIN_CLASS");
	}
}
