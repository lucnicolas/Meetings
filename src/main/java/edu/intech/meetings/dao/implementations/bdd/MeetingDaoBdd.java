package edu.intech.meetings.dao.implementations.bdd;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import edu.intech.meetings.dao.DaoFactory;
import edu.intech.meetings.dao.interfaces.IMeetingDao;
import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.model.Meeting;
import edu.intech.meetings.model.User;
import edu.intech.meetings.utils.DaoHelper;

public class MeetingDaoBdd implements IMeetingDao {

	private final EntityManager em;

	/**
	 * @param factory
	 */
	public MeetingDaoBdd(final EntityManager em) {
		this.em = em;
	}

	@Override
	public void createMeeting(final Meeting meeting, final boolean useTransaction)
			throws DaoException {
		try {
			DaoHelper.persistObject(meeting, this.em, useTransaction);
		} catch (final PersistenceException e) {
			throw new DaoException("Impossible de créer la réunion", e);
		}
	}

	@Override
	public Meeting readMeeting(final int meetingId) throws DaoException {
		final TypedQuery<Meeting> query = this.em.createNamedQuery("Meeting.findById", Meeting.class);
		query.setParameter("id", meetingId);
		final List<Meeting> ret = query.getResultList();
		if (ret.size() > 0) {
			return ret.get(0);
		}
		return null;
	}

	@Override
	public List<Meeting> readAllMeetings() throws DaoException {
		final TypedQuery<Meeting> query = this.em.createNamedQuery("Meeting.findAll", Meeting.class);
		return query.getResultList();
	}

	@Override
	public List<Meeting> readMeetingsByIdList(List<Integer> ids) throws DaoException {
		final TypedQuery<Meeting> query = this.em.createNamedQuery("Meeting.findByIdsList", Meeting.class);
		query.setParameter("ids", ids);
		return query.getResultList();	}

	@Override
	public List<Meeting> readAllMeetingsWithUser(final int userId) throws DaoException {
		final TypedQuery<Meeting> query = this.em.createNamedQuery("Meeting.findByUser", Meeting.class);
		query.setParameter("id", userId);
		return query.getResultList();
	}

	@Override
	public void updateMeeting(final Meeting meeting, final boolean useTransaction) throws DaoException {
		try {
			DaoHelper.mergeObject(meeting, this.em, useTransaction);
		} catch (final PersistenceException e) {
			throw new DaoException("Impossible de modifier la réunion", e);
		}
	}

	@Override
	public void deleteMeeting(final Meeting meeting) throws DaoException {
		try {
			// Pour éviter une violation de contrainte, on enlève les invités éventuels de
			// la réunion avant de l'effacer.
			meeting.setGuests(new ArrayList<User>());
			DaoFactory.getInstance().openTransaction();
			DaoHelper.mergeObject(meeting, this.em, false);
			DaoHelper.removeObject(meeting, this.em, false);
			DaoFactory.getInstance().commitTransaction();
		} catch (final PersistenceException e) {
			DaoFactory.getInstance().rollbackTransaction();
			throw new DaoException("Impossible de supprimer la réunion", e);
		}
	}

}
