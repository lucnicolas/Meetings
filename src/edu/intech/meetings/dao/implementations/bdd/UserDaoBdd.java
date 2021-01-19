package edu.intech.meetings.dao.implementations.bdd;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import edu.intech.meetings.dao.interfaces.IUserDao;
import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.model.User;
import edu.intech.meetings.utils.DaoHelper;

public class UserDaoBdd implements IUserDao {

	private final EntityManager em;

	/**
	 *
	 */
	public UserDaoBdd(final EntityManager em) {
		this.em = em;
	}

	@Override
	public User createUser(final User user, final boolean useTransaction) throws DaoException {
		try {
			DaoHelper.persistObject(user, this.em, useTransaction);
			return user;
		} catch (final PersistenceException e) {
			throw new DaoException("Impossible de créer l'utilisateur", e);
		}
	}

	@Override
	public User readUser(final int id) throws DaoException {
		final TypedQuery<User> query = this.em.createNamedQuery("User.findById", User.class);
		query.setParameter("id", id);
		if (query.getResultList().size() > 0) {
			return query.getResultList().get(0);
		}
		return null;
	}

	@Override
	public User readUserByName(final String name) throws DaoException {
		final TypedQuery<User> query = this.em.createNamedQuery("User.findByName", User.class);
		query.setParameter("name", name);
		final List<User> ret = query.getResultList();
		if (ret.size() > 0) {
			return ret.get(0);
		}
		return null;
	}

	@Override
	public List<User> readUsersByIdList(final List<Integer> ids) throws DaoException {
		final TypedQuery<User> query = this.em.createNamedQuery("User.findByIdsList", User.class);
		query.setParameter("ids", ids);
		return query.getResultList();
	}

	@Override
	public List<User> readAllUsers() throws DaoException {
		final TypedQuery<User> query = this.em.createNamedQuery("User.findAll", User.class);
		return query.getResultList();
	}

	@Override
	public void updateUser(final User s, final boolean useTransaction) throws DaoException {
		try {
			DaoHelper.mergeObject(s, this.em, useTransaction);
		} catch (final PersistenceException e) {
			throw new DaoException("Impossible de modifier l'utilisateur.", e);
		}
	}

	@Override
	public void deleteUser(final User s, final boolean useTransaction) throws DaoException {
		try {
			DaoHelper.removeObject(s, this.em, useTransaction);
		} catch (final PersistenceException e) {
			throw new DaoException("Impossible de supprimer l'utilisateur.", e);
		}
	}
}
