package config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.flywaydb.core.Flyway;

@WebListener
public class FlywayInitializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Flyway flyway = Flyway.configure()
			.dataSource(
				"DB_URL",
				"DB_NAME",
				"DB_PWD")
			.locations("classpath:db/migration")
			.load();

		flyway.migrate();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing
	}
}
