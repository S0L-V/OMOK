package config;
 import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
@WebListener
public class FlywayInitializer implements ServletContextListener {
	private static final String JNDI_NAME = "java:comp/env/jdbc/oracle";
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			DataSource ds = lookupDataSource();
			Flyway flyway = Flyway.configure()
				.dataSource(ds)
				.locations("classpath:db/migration")
				.load();
			flyway.migrate();
		} catch (Exception e) {
			throw new RuntimeException("Flyway initialization failed (JNDI=" + JNDI_NAME + ")", e);
		}
	}
	@Override
	public void contextDestroyed(ServletContextEvent sce) {}
	private DataSource lookupDataSource() throws NamingException {
		InitialContext ctx = new InitialContext();
		return (DataSource)ctx.lookup(JNDI_NAME);
	}
}