package game.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import game.single.service.SingleGameServiceManager;

@WebListener
public class GameContextListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        SingleGameServiceManager.getInstance().destroy();
//      MultiGameServiceManager.getInstance().destroy();
    }
}