package edu.intech.meetings.services;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.intech.meetings.model.Meeting;
import edu.intech.meetings.model.Room;
import edu.intech.meetings.model.User;
import org.codehaus.jettison.json.JSONException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import static edu.intech.meetings.services.MeetingsServiceTest.TEST_MEETING_DURATION;
import static edu.intech.meetings.services.MeetingsServiceTest.TEST_MEETING_START_OK;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomsServiceTest extends AbstractTest {

    private final static String TEST_ROOM_NAME = "Réunion de travail";
    private final static String TEST_ROOM_CAPACITY = "180";
    private final static String TEST_ROOM_MEETINGS_KO = "-1,-2";

    private final static String TEST_CHANGE_NAME = "Réveillon du nouvel an.";
    private final static String TEST_CHANGE_CAPACITY = "45";
    private static Room tempRoom;

    private final RoomsService roomService = new RoomsService();
    private final MultivaluedMap<String, String> params = new MultivaluedMapImpl();

    @Test
    @Order(1)
    public void testGetAllRooms() {
        try {
            final Response ret = this.roomService.getAllRooms();
            assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être OK (200).");
        } catch (JSONException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(2)
    public void testCreateRoom() {
        Response ret;
        try {
            // premier test sans paramètre, donc sans token. On doit obtenir un statut 403
            // Forbidden.
            ret = this.roomService.addRoom(this.params);
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être FORBIDEN (403).");
            // 2ème test : la map de parametres et passée mais avec un token invalide.
            // On doit obtenir un statut 403 Forbidden.
            this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, "TokenBidon");
            ret = this.roomService.addRoom(this.params);
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être FORBIDEN (403).");
            // Fin des tests sur le token. Maintenant, on met un token correct et on teste
            // le reste.
            // Premier test sur la salle : aucune info entrée. On doit obtenir un
            // statut 412 PRECONDITION_FAILED.
            this.params.clear();
            this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
            ret = this.roomService.addRoom(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");
            // On teste maintenant que les données de l'utilisateur crée correspondent bien
            // à ce qu'on avait envoyé.
            this.params.add(RoomsService.ROOM_PARAM_NAME, TEST_ROOM_NAME);
            this.params.add(RoomsService.ROOM_PARAM_CAPACITY, TEST_ROOM_CAPACITY);
            ret = this.roomService.addRoom(this.params);
            tempRoom = TestsHelper.JsonToRoom((String) ret.getEntity());
            assertEquals(tempRoom.getName(), TEST_ROOM_NAME,
                    "Le titre de la salle créée n'est pas le bon.");
            assertEquals(Integer.toString(tempRoom.getCapacity()), TEST_ROOM_CAPACITY,
                    "La capacitée de la salle créée n'est pas la bonne.");
        } catch (JSONException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(3)
    public void testUpdateRoom() {
        Response ret;
        try {
            // Arrivé dans ce test, la salle créée dans le test précédent est enregistrée
            // en tant qu'objet dans la variable tempRoom.
            // On re-teste sans token pour vérifier qu'il est demandé.
            this.params.clear();
            ret = this.roomService.updateRoom(this.params);
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être FORBIDEN (403).");

            // Fin des tests sur le token. Maintenant, on met un token correct et on teste
            // le reste.
            // Premier test sur la salle : aucune info entrée. On doit obtenir un
            // statut 412 PRECONDITION_FAILED.
            this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
            ret = this.roomService.updateRoom(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 2eme test : juste le nom, une date et une durée (avec des valeurs
            // changées). On doit encore obtenir un statut 412 PRECONDITION_FAILED parce
            // qu'on a pas spécifié l'identifiant.
            this.params.add(RoomsService.ROOM_PARAM_NAME, TEST_CHANGE_NAME);
            this.params.add(RoomsService.ROOM_PARAM_CAPACITY, TEST_CHANGE_CAPACITY);
            ret = this.roomService.updateRoom(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 3eme test : on passe un identifiant au mauvais format.
            // On doit encore obtenir un statut 412 PRECONDITION_FAILED.
            this.params.add(RoomsService.ROOM_PARAM_ID, "ezaeaza");
            ret = this.roomService.updateRoom(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 4eme test : on passe un identifiant au bon format mais ne
            // correspondant pas à une salle réelle. On doit encore obtenir un statut 412
            // PRECONDITION_FAILED.
            this.params.remove(RoomsService.ROOM_PARAM_ID);
            this.params.add(RoomsService.ROOM_PARAM_ID, "-1");
            ret = this.roomService.updateRoom(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 5eme test : on passe l'identifiant du Room créé dans le premier
            // test. La mise à jour doit avoir lieu sur le nom et le mot de passe. On doit
            // obtenir un statut 200 OK.
            this.params.remove(RoomsService.ROOM_PARAM_ID);
            this.params.add(RoomsService.ROOM_PARAM_ID, Integer.toString(tempRoom.getId()));
            ret = this.roomService.updateRoom(this.params);
            assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être CREATED (201).");
            // On teste maintenant que les données de la salle retournée correspondent
            // bien à ce qu'on avait modifié.
            tempRoom = TestsHelper.JsonToRoom((String) ret.getEntity());
            assertEquals(tempRoom.getName(), TEST_CHANGE_NAME,
                    "Le nom de la salle n'a pas été modifié.");
            assertEquals(Integer.toString(tempRoom.getCapacity()), TEST_CHANGE_CAPACITY,
                    "La durée de la salle créée n'est pas la bonne.");

        } catch (JSONException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(4)
    public void testGetRoomById() {
        Response ret;
        try {
            // Note : pas besoin de token pour la lecture.
            this.params.clear();
            // 1er test : on passe un identifiant au mauvais format.
            // On doit obtenir un statut 412 PRECONDITION_FAILED.
            this.params.add(RoomsService.ROOM_PARAM_ID, "ezaeaza");
            ret = this.roomService.getRoomById(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 2eme test : on passe un identifiant null. On doit encore obtenir un statut
            // 412 PRECONDITION_FAILED.
            this.params.remove(RoomsService.ROOM_PARAM_ID);
            this.params.add(RoomsService.ROOM_PARAM_ID, null);
            ret = this.roomService.getRoomById(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 3eme test : on passe un identifiant au bon format mais ne correspondant pas à
            // une salle réelle. On doit encore obtenir un statut 412
            // PRECONDITION_FAILED.
            this.params.remove(RoomsService.ROOM_PARAM_ID);
            this.params.add(RoomsService.ROOM_PARAM_ID, "-1");
            ret = this.roomService.getRoomById(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 4eme test : on passe l'identifiant du Room créé dans le premier
            // test. On doit obtenir un statut 200 OK.
            this.params.remove(RoomsService.ROOM_PARAM_ID);
            this.params.add(RoomsService.ROOM_PARAM_ID, Integer.toString(tempRoom.getId()));
            ret = this.roomService.getRoomById(this.params);
            assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être OK (200).");
            // On teste maintenant que les données de la salle retournée correspondent
            // bien à ce qu'on avait modifié dans le dernier test.
            tempRoom = TestsHelper.JsonToRoom((String) ret.getEntity());
            assertEquals(tempRoom.getName(), TEST_CHANGE_NAME,
                    "Le nom de la salle n'a pas été modifié.");
            assertEquals(Integer.toString(tempRoom.getCapacity()), TEST_CHANGE_CAPACITY,
                    "La durée de la salle créée n'est pas la bonne.");
            assertEquals(createParticipantsListFromMeetingsList(tempRoom.getMeetings()), "",
                    "La liste des invités de la salle n'est pas correcte.");

        } catch (JSONException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(5)
    public void testDeleteRoom() {
        Response ret;
        try {
            this.params.clear();
            // On re-teste sans token pour vérifier qu'il est demandé.
            this.params.clear();
            ret = this.roomService.updateRoom(this.params);
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être FORBIDEN (403).");

            // Fin des tests sur le token. Maintenant, on met un token correct et on teste
            // le reste.
            this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);

            // 1er test : on passe un identifiant au mauvais format.
            // On doit obtenir un statut 412 PRECONDITION_FAILED.
            this.params.add(RoomsService.ROOM_PARAM_ID, "ezaeaza");
            ret = this.roomService.deleteRoomById(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 2eme test : on passe un identifiant null. On doit encore obtenir un statut
            // 412 PRECONDITION_FAILED.
            this.params.remove(RoomsService.ROOM_PARAM_ID);
            this.params.add(RoomsService.ROOM_PARAM_ID, null);
            ret = this.roomService.deleteRoomById(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 3eme test : on passe un identifiant au bon format mais ne correspondant pas à
            // une salle réel. On doit encore obtenir un statut 412
            // PRECONDITION_FAILED.
            this.params.remove(RoomsService.ROOM_PARAM_ID);
            this.params.add(RoomsService.ROOM_PARAM_ID, "-1");
            ret = this.roomService.deleteRoomById(this.params);
            assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être PRECONDITION_FAILED (412).");

            // 4eme test : on passe l'identifiant du Room créé dans le premier
            // test. On doit obtenir un statut 200 OK.
            this.params.remove(RoomsService.ROOM_PARAM_ID);
            this.params.add(RoomsService.ROOM_PARAM_ID, Integer.toString(tempRoom.getId()));
            ret = this.roomService.deleteRoomById(this.params);
            assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être OK (200).");
            // On teste maintenant que la salle a bien été effacé.
            ret = this.roomService.getRoomById(this.params);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ret.getStatus(),
                    "Le status de la réponse devrait être NOT_FOUND (404).");

        } catch (JSONException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(6)
    public void testGetRoomsByMeetingId() {
        Response ret;
        try {
            // ***********************************************************************
            // Création des données pour le test : on va créer un utilisateur qu'on va
            // inviter à 2 salles pour tester
            // ***********************************************************************
            // Création de l'utilisateur.
            final String meetingIdAsString = Integer.toString(createMeetingAndReturnId("01"));
            // Création de la première salle
            this.params.clear();
            this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
            this.params.add(RoomsService.ROOM_PARAM_NAME, TEST_ROOM_NAME);
            this.params.add(RoomsService.ROOM_PARAM_CAPACITY, TEST_ROOM_CAPACITY);
            this.params.add(RoomsService.ROOM_PARAM_MEETINGS, meetingIdAsString);
            ret = this.roomService.addRoom(this.params);
            tempRoom = TestsHelper.JsonToRoom((String) ret.getEntity());
            String commaSeparatedRoomsIdList = Integer.toString(tempRoom.getId());
            // Création de la deuxième salle
            this.params.clear();
            this.params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
            this.params.add(RoomsService.ROOM_PARAM_NAME, TEST_ROOM_NAME);
            this.params.add(RoomsService.ROOM_PARAM_CAPACITY, TEST_ROOM_CAPACITY);
            this.params.add(RoomsService.ROOM_PARAM_MEETINGS, meetingIdAsString);
            ret = this.roomService.addRoom(this.params);
            tempRoom = TestsHelper.JsonToRoom((String) ret.getEntity());
            commaSeparatedRoomsIdList = commaSeparatedRoomsIdList.concat(",").concat(Integer.toString(tempRoom.getId()));

            // ***********************************************************************
            // Tests finis : suppression des données de test
            // ***********************************************************************
            // Suppression des salles
            deleteJunkRooms(commaSeparatedRoomsIdList);
            // Supression de l'utilisateur
            deleteUser(meetingIdAsString);
        } catch (JSONException | IOException e) {
            fail(e.getMessage());
        }
    }

    private String createParticipantsListFromMeetingsList(final List<Meeting> meetings) {
        String ret = "";
        boolean first = true;
        for (final Meeting meeting : meetings) {
            if (first) {
                first = false;
            } else {
                ret = ret.concat(",");
            }
            ret = ret.concat(Integer.toString(meeting.getId()));
        }
        return ret;
    }

    private String createParticipantsList() throws IOException, JSONException {
        return Integer.toString(createMeetingAndReturnId("01")).concat(",").concat(Integer.toString(createMeetingAndReturnId("02")));
    }

    private int createMeetingAndReturnId(final String meetingNumber) throws IOException, JSONException {
        final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
        params.add(MeetingsService.MEETING_PARAM_TITLE, "TestMeeting_".concat(meetingNumber));
        params.add(MeetingsService.MEETING_PARAM_START, TEST_MEETING_START_OK);
        params.add(MeetingsService.MEETING_PARAM_DURATION, TEST_MEETING_DURATION);
        final Response ret = new MeetingsService().addMeeting(params);
        final Meeting tempMeeting = TestsHelper.JsonToMeeting((String) ret.getEntity());
        return tempMeeting.getId();
    }

    private void deleteJunkGuests(final String guestsList) throws IOException, JSONException {
        final StringTokenizer st = new StringTokenizer(guestsList, ",");
        while (st.hasMoreTokens()) {
            deleteUser(st.nextToken());
        }
    }

    private void deleteUser(final String userId) throws IOException, JSONException {
        final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
        params.add(UsersService.USER_PARAM_ID, userId);
        new UsersService().deleteUserById(params);
    }

    private void deleteJunkRooms(final String commaSeparatedRoomsIdList)
            throws IOException, JSONException {
        final StringTokenizer st = new StringTokenizer(commaSeparatedRoomsIdList, ",");
        while (st.hasMoreTokens()) {
            deleteRoom(st.nextToken());
        }
    }

    private void deleteRoom(final String roomId) throws IOException, JSONException {
        final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add(AuthenticationService.AUTH_PARAM_TOKEN, TestSetup.token);
        params.add(UsersService.USER_PARAM_ID, roomId);
        new RoomsService().deleteRoomById(params);
    }
}