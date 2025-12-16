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
                        "jdbc:oracle:thin:@localhost:1521:xe",
                        "omok",
                        "omok1234"
                )
                .load();

        flyway.migrate();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing
    }
}
