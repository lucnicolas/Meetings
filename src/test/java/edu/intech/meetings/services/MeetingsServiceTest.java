package edu.intech.meetings.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.intech.meetings.model.Meeting;
import edu.intech.meetings.model.User;
import edu.intech.meetings.services.AuthenticationService;
import edu.intech.meetings.services.MeetingsService;
import edu.intech.meetings.services.UsersService;

/**
 * Classe servant à tester les services de gestion des réunions de la classe
 * {@link MeetingsService}. Cette classe hérite de {@link AbstractTest} afin que
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
public class MeetingsServiceTest extends AbstractTest {

	private final static String TEST_MEETING_TITLE = "Révéillon de noël";
	private final static String TEST_MEETING_START_OK = "24/12/2020 20:00";
	private final static String TEST_MEETING_START_KO = "33/22/2020 40:73";
	private final static String TEST_MEETING_DURATION = "180";
	private final static String TEST_MEETING_USERS_KO = "-1,-2";

	private final static String TEST_CHANGE_TITLE = "Réveillon du nouvel an.";
	private final static String TEST_CHANGE_START = "31/12/2020 20:00";
	private final static String TEST_CHANGE_DURATION = "600";
	private static Meeting tempMeeting;

	private final MeetingsService meetingService = new MeetingsService();
	private final MultivaluedMap<String, String> params = new MultivaluedMapImpl();

	@Test
	@Order(1)
	public void testGetAllMeetings() {
		try {
			final Response ret = this.meetingService.getAllMeetings();
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");
		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(2)
	public void testCreateMeeting() {
		Response ret;
		try {
			// premier test sans paramètre, donc sans token. On doit obtenir un statut 403
			// Forbidden.
			ret = this.meetingService.addMeeting(this.params);
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être FORBIDEN (403).");
			// 2ème test : la map de parametres et passée mais avec un token invalide.
			// On doit obtenir un statut 403 Forbidden.
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, "TokenBidon");
			ret = this.meetingService.addMeeting(this.params);
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être FORBIDEN (403).");
			// Fin des tests sur le token. Maintenant, on met un token correct et on teste
			// le reste.
			// Premier test sur la réunion : aucune info entrée. On doit obtenir un
			// statut 412 PRECONDITION_FAILED.
			this.params.clear();
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
			ret = this.meetingService.addMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");
			// 2eme test sur la réunion : juste le nom. On doit obtenir un
			// statut 412 PRECONDITION_FAILED.
			this.params.add(MeetingsService.MEETING_PARAM_TITLE, TEST_MEETING_TITLE);
			ret = this.meetingService.addMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");
			// 3eme test sur la réunion : le nom, une date de début dans un mauvais format
			// et une durée au bon format. On doit obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.add(MeetingsService.MEETING_PARAM_START, "C'est pas une date !");
			this.params.add(MeetingsService.MEETING_PARAM_DURATION, TEST_MEETING_DURATION);
			ret = this.meetingService.addMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");
			// 4eme test sur la réunion : le nom, une date de début dans un bon format mais
			// avec des valeurs incohérentes et une durée au bon format. On doit obtenir un
			// statut 412 PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_START);
			this.params.add(MeetingsService.MEETING_PARAM_START, TEST_MEETING_START_KO);
			ret = this.meetingService.addMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");
			// 5eme test sur la réunion : on corrige la date de début mais c'est la durée
			// qu'on rend maintenant invalide. On doit obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_START);
			this.params.add(MeetingsService.MEETING_PARAM_START, TEST_MEETING_START_OK);
			this.params.remove(MeetingsService.MEETING_PARAM_DURATION);
			this.params.add(MeetingsService.MEETING_PARAM_DURATION, "C'est pas un entier !");
			ret = this.meetingService.addMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");
			// 6eme test sur la réunion : juste le nom, une date de début au bon format et
			// une durée au bon format aussi. Ca doit marcher.
			this.params.remove(MeetingsService.MEETING_PARAM_DURATION);
			this.params.add(MeetingsService.MEETING_PARAM_DURATION, TEST_MEETING_DURATION);
			ret = this.meetingService.addMeeting(this.params);
			assertEquals(Response.Status.CREATED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être CREATED (201).");
			// On teste maintenant que les données de l'utilisateur crée correspondent bien
			// à ce qu'on avait envoyé.
			tempMeeting = TestsHelper.JsonToMeeting((String) ret.getEntity());
			assertEquals(tempMeeting.getTitle(), TEST_MEETING_TITLE,
					"Le titre de la réunion créée n'est pas le bon.");
			assertEquals(MeetingsService.DATE_FORMATTER.format(tempMeeting.getStart()), TEST_MEETING_START_OK,
					"La date de début de la réunion créée n'est pas la bonne.");
			assertEquals(Integer.toString(tempMeeting.getDuration()), TEST_MEETING_DURATION,
					"La durée de la réunion créée n'est pas la bonne.");
		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(3)
	public void testUpdateMeeting() {
		Response ret;
		try {
			// Arrivé dans ce test, la réunion créée dans le test précédent est enregistrée
			// en tant qu'objet dans la variable tempMeeting.
			// On re-teste sans token pour vérifier qu'il est demandé.
			this.params.clear();
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être FORBIDEN (403).");

			// Fin des tests sur le token. Maintenant, on met un token correct et on teste
			// le reste.
			// Premier test sur la réunion : aucune info entrée. On doit obtenir un
			// statut 412 PRECONDITION_FAILED.
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 2eme test : juste le nom, une date et une durée (avec des valeurs
			// changées). On doit encore obtenir un statut 412 PRECONDITION_FAILED parce
			// qu'on a pas spécifié l'identifiant.
			this.params.add(MeetingsService.MEETING_PARAM_TITLE, TEST_CHANGE_TITLE);
			this.params.add(MeetingsService.MEETING_PARAM_START, TEST_CHANGE_START);
			this.params.add(MeetingsService.MEETING_PARAM_DURATION, TEST_CHANGE_DURATION);
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 3eme test : on passe un identifiant au mauvais format.
			// On doit encore obtenir un statut 412 PRECONDITION_FAILED.
			this.params.add(MeetingsService.MEETING_PARAM_ID, "ezaeaza");
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 4eme test : on passe un identifiant au bon format mais ne
			// correspondant pas à une réunion réelle. On doit encore obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, "-1");
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 5eme test : on passe l'identifiant du Meeting créé dans le premier
			// test. La mise à jour doit avoir lieu sur le nom et le mot de passe. On doit
			// obtenir un statut 200 OK.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, Integer.toString(tempMeeting.getId()));
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être CREATED (201).");
			// On teste maintenant que les données de la réunion retournée correspondent
			// bien à ce qu'on avait modifié.
			tempMeeting = TestsHelper.JsonToMeeting((String) ret.getEntity());
			assertEquals(tempMeeting.getTitle(), TEST_CHANGE_TITLE,
					"Le nom de la réunion n'a pas été modifié.");
			assertEquals(MeetingsService.DATE_FORMATTER.format(tempMeeting.getStart()), TEST_CHANGE_START,
					"La date de début de la réunion créée n'est pas la bonne.");
			assertEquals(Integer.toString(tempMeeting.getDuration()), TEST_CHANGE_DURATION,
					"La durée de la réunion créée n'est pas la bonne.");

			// 6eme test : on tente d'ajouter des utilisateurs mais en mettant n'importe
			// quoi dans le champs. On doit obtenir un statut 412 PRECONDITION_FAILED.
			this.params.add(MeetingsService.MEETING_PARAM_GUESTS, "C'est pas une liste d'id user séparés par des virgules !");
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 7eme test : on met maintenant une liste d'identifiants mais ne correspondant
			// pas à des vrais utilisateurs.On doit obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_GUESTS);
			this.params.add(MeetingsService.MEETING_PARAM_GUESTS, TEST_MEETING_USERS_KO);
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 8eme test : on crée 2 utilisateurs et on les met comme invités.
			// On doit obtenir un statut 200 OK.
			this.params.remove(MeetingsService.MEETING_PARAM_GUESTS);
			final String guestsList = createGuestsList();
			this.params.add(MeetingsService.MEETING_PARAM_GUESTS, guestsList);
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");
			// On teste maintenant que les données de la réunion retourné correspondent
			// bien à ce qu'on avait modifié.
			tempMeeting = TestsHelper.JsonToMeeting((String) ret.getEntity());
			assertEquals(createGuestsListFromUsersList(tempMeeting.getGuests()), guestsList,
					"La liste des invités de la réunion n'est pas correcte.");

			// 9eme test : on désinvite les 2 utilisateurs.
			// On doit obtenir un statut 200 OK.
			this.params.remove(MeetingsService.MEETING_PARAM_GUESTS);
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");

			// il faut maintenant effacer les users "bidon" créés en tant qu'invités
			deleteJunkGuests(guestsList);

		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(4)
	public void testGetMeetingById() {
		Response ret;
		try {
			// Note : pas besoin de token pour la lecture.
			this.params.clear();
			// 1er test : on passe un identifiant au mauvais format.
			// On doit obtenir un statut 412 PRECONDITION_FAILED.
			this.params.add(MeetingsService.MEETING_PARAM_ID, "ezaeaza");
			ret = this.meetingService.getMeetingById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 2eme test : on passe un identifiant null. On doit encore obtenir un statut
			// 412 PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, null);
			ret = this.meetingService.getMeetingById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 3eme test : on passe un identifiant au bon format mais ne correspondant pas à
			// une réunion réelle. On doit encore obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, "-1");
			ret = this.meetingService.getMeetingById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 4eme test : on passe l'identifiant du Meeting créé dans le premier
			// test. On doit obtenir un statut 200 OK.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, Integer.toString(tempMeeting.getId()));
			ret = this.meetingService.getMeetingById(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");
			// On teste maintenant que les données de la réunion retournée correspondent
			// bien à ce qu'on avait modifié dans le dernier test.
			tempMeeting = TestsHelper.JsonToMeeting((String) ret.getEntity());
			assertEquals(tempMeeting.getTitle(), TEST_CHANGE_TITLE,
					"Le nom de la réunion n'a pas été modifié.");
			assertEquals(MeetingsService.DATE_FORMATTER.format(tempMeeting.getStart()), TEST_CHANGE_START,
					"La date de début de la réunion créée n'est pas la bonne.");
			assertEquals(Integer.toString(tempMeeting.getDuration()), TEST_CHANGE_DURATION,
					"La durée de la réunion créée n'est pas la bonne.");
			assertEquals(createGuestsListFromUsersList(tempMeeting.getGuests()), "",
					"La liste des invités de la réunion n'est pas correcte.");

		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(5)
	public void testDeleteMeeting() {
		Response ret;
		try {
			this.params.clear();
			// On re-teste sans token pour vérifier qu'il est demandé.
			this.params.clear();
			ret = this.meetingService.updateMeeting(this.params);
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être FORBIDEN (403).");

			// Fin des tests sur le token. Maintenant, on met un token correct et on teste
			// le reste.
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);

			// 1er test : on passe un identifiant au mauvais format.
			// On doit obtenir un statut 412 PRECONDITION_FAILED.
			this.params.add(MeetingsService.MEETING_PARAM_ID, "ezaeaza");
			ret = this.meetingService.deleteMeetingById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 2eme test : on passe un identifiant null. On doit encore obtenir un statut
			// 412 PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, null);
			ret = this.meetingService.deleteMeetingById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 3eme test : on passe un identifiant au bon format mais ne correspondant pas à
			// une réunion réel. On doit encore obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, "-1");
			ret = this.meetingService.deleteMeetingById(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 4eme test : on passe l'identifiant du Meeting créé dans le premier
			// test. On doit obtenir un statut 200 OK.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, Integer.toString(tempMeeting.getId()));
			ret = this.meetingService.deleteMeetingById(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être OK (200).");
			// On teste maintenant que la réunion a bien été effacé.
			ret = this.meetingService.getMeetingById(this.params);
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être NOT_FOUND (404).");

		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(6)
	public void testGetMeetingsByUserId() {
		Response ret;
		try {
			// ***********************************************************************
			// Création des données pour le test : on va créer un utilisateur qu'on va
			// inviter à 2 réunions pour tester
			// ***********************************************************************
			// Création de l'utilisateur.
			final String userIdAsString = Integer.toString(createUserAndReturnId("01"));
			// Création de la première réunion
			this.params.clear();
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
			this.params.add(MeetingsService.MEETING_PARAM_TITLE, TEST_MEETING_TITLE);
			this.params.add(MeetingsService.MEETING_PARAM_START, TEST_MEETING_START_OK);
			this.params.add(MeetingsService.MEETING_PARAM_DURATION, TEST_MEETING_DURATION);
			this.params.add(MeetingsService.MEETING_PARAM_GUESTS, userIdAsString);
			ret = this.meetingService.addMeeting(this.params);
			tempMeeting = TestsHelper.JsonToMeeting((String) ret.getEntity());
			String commaSeparatedMeetingsIdList = Integer.toString(tempMeeting.getId());
			// Création de la deuxième réunion
			this.params.clear();
			this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
			this.params.add(MeetingsService.MEETING_PARAM_TITLE, TEST_CHANGE_TITLE);
			this.params.add(MeetingsService.MEETING_PARAM_START, TEST_CHANGE_START);
			this.params.add(MeetingsService.MEETING_PARAM_DURATION, TEST_CHANGE_DURATION);
			this.params.add(MeetingsService.MEETING_PARAM_GUESTS, userIdAsString);
			ret = this.meetingService.addMeeting(this.params);
			tempMeeting = TestsHelper.JsonToMeeting((String) ret.getEntity());
			commaSeparatedMeetingsIdList = commaSeparatedMeetingsIdList.concat(",").concat(Integer.toString(tempMeeting.getId()));

			// ***********************************************************************
			// Maintenant, on peut tester le service qui renvoie les réunions dans
			// lesquelles un utilisateur est invité.
			// ***********************************************************************
			// Premier test : pas d'id fourni. On doit obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.clear();
			ret = this.meetingService.getMeetingsByUserId(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 2eme test : identifiant null. On doit encore obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.add(MeetingsService.MEETING_PARAM_ID, null);
			ret = this.meetingService.getMeetingsByUserId(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 3eme test : on passe un identifiant au mauvais format.
			// On doit encore obtenir un statut 412 PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, "ezaeaza");
			ret = this.meetingService.getMeetingsByUserId(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 4eme test : on passe un identifiant au bon format mais ne
			// correspondant pas à un utilisateur réel. On doit encore obtenir un statut 412
			// PRECONDITION_FAILED.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, "-1");
			ret = this.meetingService.getMeetingsByUserId(this.params);
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être PRECONDITION_FAILED (412).");

			// 5eme test : on passe l'identifiant de l'utilisateur créé poir ce test. La
			// mise à jour doit avoir lieu sur le nom et le mot de passe. On doit obtenir un
			// statut 200 OK.
			this.params.remove(MeetingsService.MEETING_PARAM_ID);
			this.params.add(MeetingsService.MEETING_PARAM_ID, userIdAsString);
			ret = this.meetingService.getMeetingsByUserId(this.params);
			assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
					"Le status de la réponse devrait être CREATED (201).");
			// On teste maintenant que les réunions retournées correspondent
			// bien à celles qu'on avait créées.
			final List<Meeting> meetings = TestsHelper.JsonToMeetingsList((String) ret.getEntity());
			for (final Meeting tmp : meetings) {
				assertTrue(commaSeparatedMeetingsIdList.contains(Integer.toString(tmp.getId())),
						"L'une des réunions ramenée ne concerne pas l'utilisateur.");
				tmp.setTitle("Trouvée");
			}
			for (final Meeting tmp : meetings) {
				assertEquals(tmp.getTitle(), "Trouvée", "L'une des réunions n'a pas été trouvée");
			}

			// ***********************************************************************
			// Tests finis : suppression des données de test
			// ***********************************************************************
			// Suppression des réunions
			deleteJunkMeetings(commaSeparatedMeetingsIdList);
			// Supression de l'utilisateur
			deleteUser(userIdAsString);
		} catch (JSONException | IOException e) {
			fail(e.getMessage());
		}
	}

	private String createGuestsListFromUsersList(final List<User> users) {
		String ret = "";
		boolean first = true;
		for (final User user : users) {
			if (first) {
				first = false;
			} else {
				ret = ret.concat(",");
			}
			ret = ret.concat(Integer.toString(user.getId()));
		}
		return ret;
	}

	private String createGuestsList() throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		return Integer.toString(createUserAndReturnId("01")).concat(",").concat(Integer.toString(createUserAndReturnId("02")));
	}

	private int createUserAndReturnId(final String userNumber) throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
		params.add(UsersService.USER_PARAM_NAME, "TestUser_".concat(userNumber));
		params.add(UsersService.USER_PARAM_PWD, "TestUser_".concat(userNumber));
		final Response ret = new UsersService().addUser(params);
		final User tempUser = TestsHelper.JsonToUser((String) ret.getEntity());
		return tempUser.getId();
	}

	private void deleteJunkGuests(final String guestsList) throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		final StringTokenizer st = new StringTokenizer(guestsList, ",");
		while (st.hasMoreTokens()) {
			deleteUser(st.nextToken());
		}
	}

	private void deleteUser(final String userId) throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
		params.add(UsersService.USER_PARAM_ID, userId);
		new UsersService().deleteUserById(params);
	}

	private void deleteJunkMeetings(final String commaSeparatedMeetingsIdList)
			throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		final StringTokenizer st = new StringTokenizer(commaSeparatedMeetingsIdList, ",");
		while (st.hasMoreTokens()) {
			deleteMeeting(st.nextToken());
		}
	}

	private void deleteMeeting(final String meetingId) throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
		params.add(UsersService.USER_PARAM_ID, meetingId);
		new MeetingsService().deleteMeetingById(params);
	}

}
