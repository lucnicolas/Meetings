package edu.intech.meetings.dao.interfaces;

import java.util.List;

import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.model.Meeting;

public interface IMeetingDao {

	/**
	 * Stocke la réunion dans la couche de persistance.
	 *
	 * @param meeting        La réunion à stocker;
	 * @param useTransaction Mettre à <code>true</code> pour utiliser des
	 *                       transactions, <code>false</code> sinon.
	 * @throws DaoException en cas d'erreur
	 */
	void createMeeting(Meeting meeting, boolean useTransaction) throws DaoException;

	/**
	 * Renvoie la réunion correspondant à l'id passé.
	 *
	 * @param meetingId Identifiant de la réunion qu'on veut récupérer.
	 * @return La réunion correspondant à l'id passé.
	 * @throws DaoException en cas d'erreur
	 */
	Meeting readMeeting(int meetingId) throws DaoException;

	/**
	 * Renvoie toutes les réunions.
	 *
	 * @return la liste de toutes les réunions.
	 * @throws DaoException en cas d'erreur
	 */
	List<Meeting> readAllMeetings() throws DaoException;

	/**
	 * Renvoie toutes les réunions dans lesquelles un utilisateur est invité.
	 *
	 * @param userId Id de l'utilisateur pour lequel on veut récupérer les réunions.
	 * @return la liste de toutes les réunions dans lesquelles l'utilisateur est
	 *         invité.
	 * @throws DaoException en cas d'erreur
	 */
	List<Meeting> readAllMeetingsWithUser(int userId) throws DaoException;

	/**
	 * Modifie la réunion dans la couche de persistance.
	 *
	 * @param meeting        La réunion à modifier;
	 * @param useTransaction Mettre à <code>true</code> pour utiliser des
	 *                       transactions, <code>false</code> sinon.
	 * @throws DaoException en cas d'erreur
	 */
	void updateMeeting(Meeting meeting, boolean useTransaction) throws DaoException;

	/**
	 * Supprime la réunion dans la couche de persistance.
	 *
	 * @param meeting La réunion à supprimer;
	 * @throws DaoException en cas d'erreur
	 */
	void deleteMeeting(Meeting meeting) throws DaoException;

}
