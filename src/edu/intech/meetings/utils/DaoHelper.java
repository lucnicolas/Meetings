package edu.intech.meetings.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

public class DaoHelper {

	public static void persistObject(final Object obj, final EntityManager em, final boolean useTransaction)
			throws PersistenceException {
		final EntityTransaction trans = em.getTransaction();
		try {
			if (useTransaction) {
				trans.begin();
			}
			em.persist(obj);
			if (useTransaction) {
				trans.commit();
			}
		} catch (final PersistenceException e) {
			if (useTransaction && trans.isActive()) {
				trans.rollback();
			}
			throw e;
		}
	}

	public static void mergeObject(final Object obj, final EntityManager em, final boolean useTransaction)
			throws PersistenceException {
		final EntityTransaction trans = em.getTransaction();
		try {
			if (useTransaction) {
				trans.begin();
			}
			em.merge(obj);
			if (useTransaction) {
				trans.commit();
			}
		} catch (final PersistenceException e) {
			if (useTransaction && trans.isActive()) {
				trans.rollback();
			}
			throw e;
		}
	}

	public static void removeObject(final Object obj, final EntityManager em, final boolean useTransaction)
			throws PersistenceException {
		final EntityTransaction trans = em.getTransaction();
		try {
			if (useTransaction) {
				trans.begin();
			}
			em.remove(obj);
			if (useTransaction) {
				trans.commit();
			}
		} catch (final PersistenceException e) {
			if (useTransaction && trans.isActive()) {
				trans.rollback();
			}
			throw e;
		}
	}

}
