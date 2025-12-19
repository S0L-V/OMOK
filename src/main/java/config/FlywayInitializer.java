package config;
<<<<<<< HEAD
 import javax.naming.InitialContext;
=======

import javax.naming.InitialContext;
>>>>>>> branch 'dev' of https://github.com/S0L-V/OMOK
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
<<<<<<< HEAD
=======

>>>>>>> branch 'dev' of https://github.com/S0L-V/OMOK
import org.flywaydb.core.Flyway;
@WebListener
public class FlywayInitializer implements ServletContextListener {
<<<<<<< HEAD
	private static final String JNDI_NAME = "java:comp/env/jdbc/oracle";
=======

	private static final String JNDI_NAME = "java:comp/env/jdbc/oracle";

>>>>>>> branch 'dev' of https://github.com/S0L-V/OMOK
	@Override
<<<<<<< HEAD
	public void contextInitialized(ServletContextEvent sce) { 
		try {
			DataSource ds = lookupDataSource();
			Flyway flyway = Flyway.configure()
				.dataSource(ds)
				.locations("classpath:db/migration")
				.load();
			flyway.migrate();
=======
	public void contextInitialized(ServletContextEvent sce) {
		try {
			DataSource ds = lookupDataSource();

			Flyway flyway = Flyway.configure()
				.dataSource(ds)
				.locations("classpath:db/migration")
				.load();

			flyway.migrate();

>>>>>>> branch 'dev' of https://github.com/S0L-V/OMOK
		} catch (Exception e) {
			throw new RuntimeException("Flyway initialization failed (JNDI=" + JNDI_NAME + ")", e);
		}
	}
	@Override
	public void contextDestroyed(ServletContextEvent sce) {}
<<<<<<< HEAD
=======

>>>>>>> branch 'dev' of https://github.com/S0L-V/OMOK
	private DataSource lookupDataSource() throws NamingException {
		InitialContext ctx = new InitialContext();
		return (DataSource)ctx.lookup(JNDI_NAME);
	}
}
