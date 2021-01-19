package edu.intech.meetings.tests.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.intech.meetings.model.User;
import edu.intech.meetings.services.AuthenticationService;
import edu.intech.meetings.services.UsersService;
import edu.intech.meetings.utils.PasswordHelper;

/**
 * Classe servant à tester les services utilisateur de la classe
 * {@link UsersService}. Cette classe hérite de {@link AbstractTest} afin que
 * les tests lancés puissent profiter des initialisation et fermetures globales
 * proposées par la classe {@link TestSetup}.<br>
 * <br>
 * 
 * <b><u>Attention, les tests sont ordonnés à l'aide des annotations
 * {@link TestMethodOrder} en début de classe et {@link Order} devant chaque
 * test. Il est important de ne pas modifier cet ordre car les tests suivants se
 * servent des données crées par les précédents.</u></b>
 *
 * @author martin
 *
 */

@TestMethodOrder(OrderAnnotation.class)
public class UsersServiceTest extends AbstractTest {

	private final static String TEST_USER_NAME = "testUser";
	private final static String TEST_USER_PASS = "testPass";
	private final static String TEST_USER_FIRSTNAME = "testFirst";
	private final static String TEST_USER_MAIL_OK = "testMailOk@gmail.com";
	private final static String TEST_USER_MAIL_KO = "testMailKo";

	private final static String TEST_CHANGE_NAME = "changeUser";
	private final static String TEST_CHANGE_PASS = "changePass";

	private final UsersService userService = new UsersService();
	private final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	private static User tempUser;

	@Test
	@Order(1)
	public void testGetAllUsers() {
		try {
			final Response ret = this.userService.getAllUsers();
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");
		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(2)
	public void testCreateUser() {
		Response ret;
		try {
			// premier test sans paramètre, donc sans token. On doit obtenir un statut 403
			// Forbidden.
			ret = this.userService.addUser(this.params);
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être FORBIDEN (403).");
			// 2ème test : la map de parametres et passée mais avec un token invalide.
			// On doit obtenir un statut 403 Forbidden.
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, "TokenBidon");
			ret = this.userService.addUser(this.params);
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être FORBIDEN (403).");
			// Fin des tests sur le token. Maintenant, on met un token correct et on teste
			// le reste.
			// Premier test sur le user : aucune info entrée. On doit obtenir un
			// statut 412 PRECONDITION_FAILED.
			this.params.clear();
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
			ret = this.userService.addUser(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");
			// 2eme test sur le user : juste le nom. On doit obtenir un
			// statut 412 PRECONDITION_FAILED.
			this.params.add(UsersService.USER_PARAM_NAME, TEST_USER_NAME);
			ret = this.userService.addUser(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");
			// 3eme test sur le user : juste le nom et le mot de passe. Ca doit marcher.
			this.params.add(UsersService.USER_PARAM_PWD, TEST_USER_PASS);
			ret = this.userService.addUser(this.params);
			assertEquals(Response.Status.CREATED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être CREATED (201).");
			// On teste maintenant que les données de l'utilisateur crée correspondent bien
			// à ce qu'on avait envoyé.
			tempUser = TestsHelper.JsonToUser((String) ret.getEntity());
			assertEquals(UsersServiceTest.tempUser.getName(), TEST_USER_NAME,
					"Le nom de l'utilisateur créé n'est pas le bon.");
			assertEquals(UsersServiceTest.tempUser.getPassword(), PasswordHelper.generateSecurePassword(TEST_USER_PASS),
					"Le mot de passe de l'utilisateur créé n'est pas le bon.");
		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(3)
	public void testUpdateUser() {
		Response ret;
		try {
			// Arrivé dans ce test, la map @this.param est déjà renseignée avec le token
			// ainsi que le nom et le mot de passe de l'utilisateur. Celui-ci est enregistré
			// en tant qu'objet dans la variable tempUser.
			// On re-teste sans token pour vérifier qu'il est demandé.
			this.params.clear();
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être FORBIDEN (403).");

			// Fin des tests sur le token. Maintenant, on met un token correct et on teste
			// le reste.
			// Premier test sur le user : aucune info entrée. On doit obtenir un
			// statut 412 PRECONDITION_FAILED.
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 2eme test : juste le nom et le mot de passe (avec des valeurs
			// changées). On doit encore obtenir un statut 412 PRECONDITION_FAILED parce
			// qu'on a pas spécifié l'identifiant.
			this.params.add(UsersService.USER_PARAM_NAME, TEST_CHANGE_NAME);
			this.params.add(UsersService.USER_PARAM_PWD, TEST_CHANGE_PASS);
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 3eme test : on passe un identifiant au mauvais format.
			// On doit encore obtenir un statut 412 PRECONDITION_FAILED.
			this.params.add(UsersService.USER_PARAM_ID, "ezaeaza");
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 4eme test : on passe un identifiant au bon format mais ne
			// correspondant pas à un utilisateur réel. On doit encore obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(UsersService.USER_PARAM_ID);
			this.params.add(UsersService.USER_PARAM_ID, "-1");
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 5eme test : on passe l'identifiant du User créé dans le premier
			// test. La mise à jour doit avoir lieu sur le nom et le mot de passe. On doit
			// obtenir un statut 200 OK.
			this.params.remove(UsersService.USER_PARAM_ID);
			this.params.add(UsersService.USER_PARAM_ID, Integer.toString(tempUser.getId()));
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être CREATED (201).");
			// On teste maintenant que les données de l'utilisateur retourné correspondent
			// bien à ce qu'on avait modifié.
			UsersServiceTest.tempUser = TestsHelper.JsonToUser((String) ret.getEntity());
			assertEquals(UsersServiceTest.tempUser.getName(), TEST_CHANGE_NAME,
					"Le nom de l'utilisateur n'a pas été modifié.");
			assertEquals(UsersServiceTest.tempUser.getPassword(),
					PasswordHelper.generateSecurePassword(TEST_CHANGE_PASS),
					"Le mot de passe de l'utilisateur n'a pas été modifié.");

			// 6eme test : on tente de modifier l'adresse mail en mettant une adresse
			// invalide. On doit obtenir un statut 412 PRECONDITION_FAILED.
			this.params.add(UsersService.USER_PARAM_MAIL, TEST_USER_MAIL_KO);
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 7eme test : on met maintenant un adresse mail correcte ainsi qu'un prénom.
			// On doit obtenir un statut 200 OK.
			this.params.remove(UsersService.USER_PARAM_MAIL);
			this.params.add(UsersService.USER_PARAM_MAIL, TEST_USER_MAIL_OK);
			this.params.add(UsersService.USER_PARAM_FIRSTNAME, TEST_USER_FIRSTNAME);
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");
			// On teste maintenant que les données de l'utilisateur retourné correspondent
			// bien à ce qu'on avait modifié.
			UsersServiceTest.tempUser = TestsHelper.JsonToUser((String) ret.getEntity());
			assertEquals(UsersServiceTest.tempUser.getFirstName(), TEST_USER_FIRSTNAME,
					"Le prénom de l'utilisateur n'a pas été modifié.");
			assertEquals(UsersServiceTest.tempUser.getEMail(), TEST_USER_MAIL_OK,
					"L'e-mail de l'utilisateur n'a pas été modifié.");

		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(4)
	public void testGetUserById() {
		Response ret;
		try {
			// Note : pas besoin de token pour la lecture.
			this.params.clear();
			// 1er test : on passe un identifiant au mauvais format.
			// On doit obtenir un statut 412 PRECONDITION_FAILED.
			this.params.add(UsersService.USER_PARAM_ID, "ezaeaza");
			ret = this.userService.getUserById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 2eme test : on passe un identifiant null. On doit encore obtenir un statut
			// 412 PRECONDITION_FAILED.
			this.params.remove(UsersService.USER_PARAM_ID);
			this.params.add(UsersService.USER_PARAM_ID, null);
			ret = this.userService.getUserById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 3eme test : on passe un identifiant au bon format mais ne correspondant pas à
			// un utilisateur réel. On doit encore obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(UsersService.USER_PARAM_ID);
			this.params.add(UsersService.USER_PARAM_ID, "-1");
			ret = this.userService.getUserById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 4eme test : on passe l'identifiant du User créé dans le premier
			// test. On doit obtenir un statut 200 OK.
			this.params.remove(UsersService.USER_PARAM_ID);
			this.params.add(UsersService.USER_PARAM_ID, Integer.toString(tempUser.getId()));
			ret = this.userService.getUserById(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");
			// On teste maintenant que les données de l'utilisateur retourné correspondent
			// bien à ce qu'on avait modifié.
			UsersServiceTest.tempUser = TestsHelper.JsonToUser((String) ret.getEntity());
			assertEquals(UsersServiceTest.tempUser.getName(), TEST_CHANGE_NAME,
					"Le nom de l'utilisateur n'a pas été modifié.");
			assertEquals(UsersServiceTest.tempUser.getPassword(),
					PasswordHelper.generateSecurePassword(TEST_CHANGE_PASS),
					"Le mot de passe de l'utilisateur n'a pas été modifié.");
			assertEquals(UsersServiceTest.tempUser.getFirstName(), TEST_USER_FIRSTNAME,
					"Le prénom de l'utilisateur n'a pas été modifié.");
			assertEquals(UsersServiceTest.tempUser.getEMail(), TEST_USER_MAIL_OK,
					"L'e-mail de l'utilisateur n'a pas été modifié.");

		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(5)
	public void testDeleteUser() {
		Response ret;
		try {
			this.params.clear();
			// On re-teste sans token pour vérifier qu'il est demandé.
			this.params.clear();
			ret = this.userService.updateUser(this.params);
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être FORBIDEN (403).");

			// Fin des tests sur le token. Maintenant, on met un token correct et on teste
			// le reste.
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);

			// 1er test : on passe un identifiant au mauvais format.
			// On doit obtenir un statut 412 PRECONDITION_FAILED.
			this.params.add(UsersService.USER_PARAM_ID, "ezaeaza");
			ret = this.userService.deleteUserById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 2eme test : on passe un identifiant null. On doit encore obtenir un statut
			// 412 PRECONDITION_FAILED.
			this.params.remove(UsersService.USER_PARAM_ID);
			this.params.add(UsersService.USER_PARAM_ID, null);
			ret = this.userService.deleteUserById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 3eme test : on passe un identifiant au bon format mais ne correspondant pas à
			// un utilisateur réel. On doit encore obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(UsersService.USER_PARAM_ID);
			this.params.add(UsersService.USER_PARAM_ID, "-1");
			ret = this.userService.deleteUserById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 4eme test : on passe l'identifiant du User créé dans le premier
			// test. On doit obtenir un statut 200 OK.
			this.params.remove(UsersService.USER_PARAM_ID);
			this.params.add(UsersService.USER_PARAM_ID, Integer.toString(tempUser.getId()));
			ret = this.userService.deleteUserById(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");
			// On teste maintenant que l'utilisateur a bien été effacé.
			ret = this.userService.getUserById(this.params);
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être NOT_FOUND (404).");

		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

}
