package edu.intech.meetings.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import edu.intech.meetings.dao.implementations.bdd.MeetingDaoBdd;
import edu.intech.meetings.dao.implementations.bdd.RoomDaoBdd;
import edu.intech.meetings.dao.implementations.bdd.UserDaoBdd;
import edu.intech.meetings.dao.interfaces.IMeetingDao;
import edu.intech.meetings.dao.interfaces.IRoomDao;
import edu.intech.meetings.dao.interfaces.IUserDao;
import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.servletListener.MeetingsContextListener;

public class DaoFactory {

	private static DaoFactory instance;
	private final EntityManager em;

	private IUserDao userDao = null;
	private IMeetingDao meetingDao = null;
	private IRoomDao roomDao = null;

	/**
	 * actory
	 *
	 * @return the _instance
	 * @throws DaoException
	 */
	public static final DaoFactory getInstance() throws DaoException {
		if (instance == null) {
			instance = new DaoFactory();
		}
		return instance;
	}

	/**
	 *
	 */
	private DaoFactory() throws DaoException {
		try {
			this.em = MeetingsContextListener.createEntityManager();
			System.out.println("EntityManager créé.");
		} catch (final Exception e) {
			throw new DaoException("Impossible de créer l'EntityManager.", e);
		}
	}

	/**
	 * Ouvre une transaction sur la BDD. Celle-ci devra être fermée plus tard par un
	 * appel à {@link #commitTransaction()} ou {@link #rollbackTransaction()}.
	 */
	public void openTransaction() {
		this.em.getTransaction().begin();
	}

	/**
	 * Valide une transaction sur la BDD. Celle-ci doit avoir auparavant été ouverte
	 * par un appel à {@link #openTransaction()}.
	 */
	public void commitTransaction() {
		final EntityTransaction trans = this.em.getTransaction();
		if (trans.isActive()) {
			trans.commit();
		}
	}

	/**
	 * Annule une transaction sur la BDD. Celle-ci doit avoir auparavant été ouverte
	 * par un appel à {@link #openTransaction()}.
	 */
	public void rollbackTransaction() {
		final EntityTransaction trans = this.em.getTransaction();
		if (trans.isActive()) {
			trans.rollback();
		}
	}

	public IUserDao getUserDao() {
		if (this.userDao == null) {
			this.userDao = new UserDaoBdd(this.em);
		}
		return this.userDao;
	}

	public IMeetingDao getMeetingDao() {
		if (this.meetingDao == null) {
			this.meetingDao = new MeetingDaoBdd(this.em);
		}
		return this.meetingDao;
	}

	public IRoomDao getRoomDao() {
		if (this.roomDao == null) {
			this.roomDao = new RoomDaoBdd(this.em);
		}
		return this.roomDao;
	}

}
