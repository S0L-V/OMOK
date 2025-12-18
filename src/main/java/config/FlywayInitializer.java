package config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.flywaydb.core.Flyway;

@WebListener
public class FlywayInitializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		String url = AppConfig.get("DB_URL");
		String user = AppConfig.get("DB_NAME");
		String pwd = AppConfig.get("DB_PWD");

		Flyway flyway = Flyway.configure()
			.dataSource(url, user, pwd)
			.locations("classpath:db/migration")
			.load();

		flyway.migrate();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}
}
