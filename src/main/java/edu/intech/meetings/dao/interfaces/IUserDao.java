package edu.intech.meetings.dao.interfaces;

import java.util.List;

import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.model.User;

public interface IUserDao {

	/**
	 * Stocke l'utilisateur dans la couche de persistance.
	 *
	 * @param user           L'utilisateur à stocker;
	 * @param useTransaction Mettre à <code>true</code> pour utiliser des
	 *                       transactions, <code>false</code> sinon.
	 * @return l'utilisateur stocké.
	 * @throws DaoException en cas d'erreur
	 */
	User createUser(User user, boolean useTransaction) throws DaoException;

	/**
	 * Lit l'utilisateur demandée dans la couche de persistance.
	 *
	 * @param id Identifiant de l'utilisateur à lire
	 * @return L'utilisateur lue ou <code>null</code> si non trouvée.
	 * @throws DaoException en cas d'erreur
	 */
	User readUser(int id) throws DaoException;

	/**
	 * Lit un certain nombre d'utilisateurs dans la couche de persistance.
	 *
	 * @param ids Liste des identifiants des utilisateurs à lire.
	 * @return La liste des utilisateurs lus. Peut être vide <b>mais ne peut être
	 *         <code>null</code></b>.
	 * @throws DaoException en cas d'erreur
	 */
	List<User> readUsersByIdList(final List<Integer> ids) throws DaoException;

	/**
	 * Lit l'utilisateur demandée dans la couche de persistance.
	 *
	 * @param name Nom de l'utilisateur à lire
	 * @return L'utilisateur lue ou <code>null</code> si non trouvée.
	 * @throws DaoException en cas d'erreur
	 */
	User readUserByName(String name) throws DaoException;

	/**
	 * Lit tous les utilisateurs dans la couche de persistance.
	 *
	 * @return la liste de tous les utilisateurs
	 * @throws DaoException en cas d'erreur
	 */
	List<User> readAllUsers() throws DaoException;

	/**
	 * Modifie l'utilisateur dans la couche de persistance.
	 *
	 * @param user           L'utilisateur à modifier;
	 * @param useTransaction Mettre à <code>true</code> pour utiliser des
	 *                       transactions, <code>false</code> sinon.
	 * @throws DaoException en cas d'erreur
	 */
	void updateUser(User user, boolean useTransaction) throws DaoException;

	/**
	 * Supprime l'utilisateur dans la couche de persistance.
	 *
	 * @param user           L'utilisateur à supprimer;
	 * @param useTransaction Mettre à <code>true</code> pour utiliser des
	 *                       transactions, <code>false</code> sinon.
	 * @throws DaoException en cas d'erreur
	 */
	void deleteUser(User user, boolean useTransaction) throws DaoException;

}
