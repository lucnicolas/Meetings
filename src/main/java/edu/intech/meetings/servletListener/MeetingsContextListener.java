package edu.intech.meetings.servletListener;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class MeetingsContextListener implements ServletContextListener {

	private static EntityManagerFactory emf;

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		emf = Persistence.createEntityManagerFactory("EvalS5");
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		if (emf != null) {
			emf.close();
		}
	}

	public static EntityManager createEntityManager() {
		if (emf == null) {
			throw new IllegalStateException("Context is not initialized yet.");
		}
		return emf.createEntityManager();
	}

}
