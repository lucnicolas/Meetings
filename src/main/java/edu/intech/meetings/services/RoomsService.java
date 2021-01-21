package edu.intech.meetings.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.intech.meetings.dao.DaoFactory;
import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.exceptions.ServiceException;
import edu.intech.meetings.model.Meeting;
import edu.intech.meetings.model.Room;
import edu.intech.meetings.utils.ServicesHelper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Path("/room")
public class RoomsService {

    public final static String ROOM_PARAM_ID = "id";
    public final static String ROOM_PARAM_NAME = "name";
    public final static String ROOM_PARAM_CAPACITY = "capacity";
    public final static String ROOM_PARAM_MEETINGS = "meetings";


    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() throws JSONException, IOException {

        // No token needed to read all rooms.
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return Response.ok()
                    .entity(mapper.writeValueAsString(DaoFactory.getInstance().getRoomDao().readAllRooms()))
                    .build();
        } catch (final DaoException e) {
            final JSONObject json = new JSONObject();
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
        }
    }


    @GET
    @Path("/getById")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(final MultivaluedMap<String, String> formParams)
            throws JSONException, IOException {
        // No token needed to read all rooms.

        final JSONObject json = new JSONObject();
        // verifying given id.
        final String idAsString = ServicesHelper.extractParam(formParams, ROOM_PARAM_ID);
        if (idAsString == null) {
            json.put("status", "error");
            json.put("message", "Vous devez fournir l'identifiant.");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
        }
        int roomId = -1;
        try {
            roomId = Integer.parseUnsignedInt(idAsString);
        } catch (final NumberFormatException e) {
            json.put("status", "error");
            json.put("message", "L'identifiant fourni n'est pas un entier.");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
        }
        Room room = null;
        try {
            room = DaoFactory.getInstance().getRoomDao().readRoom(roomId);
        } catch (final DaoException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
        }
        if (room == null) {
            json.put("status", "error");
            json.put("message", "Room non trouvé");
            return Response.status(Response.Status.NOT_FOUND).entity(json.toString()).build();
        } else {
            final ObjectMapper mapper = new ObjectMapper();
            return Response.ok().entity(mapper.writeValueAsString(room)).build();
        }
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoom(final MultivaluedMap<String, String> formParams)
            throws IOException, JSONException {
        final JSONObject json = new JSONObject();
        // Checking JWT...
        try {
            AuthenticationService.checkToken(formParams);
        } catch (final ServiceException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(json.toString()).build();
        }
        Room room;
        // Verifying given parameters
        try {
            room = checkGivenRoom(formParams);
        } catch (final ServiceException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
        }
        // Trying to create...
        try {
            DaoFactory.getInstance().getRoomDao().createRoom(room, true);
        } catch (final DaoException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
        }
        // Success !
        final ObjectMapper mapper = new ObjectMapper();
        final URI serieId = UriBuilder.fromResource(this.getClass()).build(this);
        return Response.created(serieId).entity(mapper.writeValueAsString(room)).build();
    }

    @PUT
    @Consumes("application/x-www-form-urlencoded")
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRoom(final MultivaluedMap<String, String> formParams)
            throws IOException, JSONException {
        final JSONObject json = new JSONObject();
        // Checking JWT...
        try {
            AuthenticationService.checkToken(formParams);
        } catch (final ServiceException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(json.toString()).build();
        }
        Room room;
        // Verifying given parameters
        try {
            room = checkGivenRoom(formParams);
        } catch (final ServiceException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
        }
        // verifying given id.
        final String idAsString = ServicesHelper.extractParam(formParams, ROOM_PARAM_ID);
        if (idAsString == null) {
            json.put("status", "error");
            json.put("message", "Vous devez fournir l'identifiant de la réunion à modifier.");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();

        }
        int roomId = -1;
        try {
            roomId = Integer.parseUnsignedInt(idAsString);
        } catch (final NumberFormatException e) {
            json.put("status", "error");
            json.put("message", "L'identifiant fourni n'est pas un entier.");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
        }
        // Checking that a room exists with this id
        try {
            final Room temp = DaoFactory.getInstance().getRoomDao().readRoom(roomId);
            if (temp == null) {
                throw new DaoException("Aucune réunion trouvée avec cet identifiant.");
            }
        } catch (final DaoException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
        }

        // Trying to update...
        room.setId(roomId);
        try {
            DaoFactory.getInstance().getRoomDao().updateRoom(room, true);
        } catch (final DaoException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
        }

        // Success !
        final ObjectMapper mapper = new ObjectMapper();
        final URI uri = UriBuilder.fromResource(this.getClass()).build(this);
        return Response.ok(uri).entity(mapper.writeValueAsString(room)).build();
    }

    @DELETE
    @Consumes("application/x-www-form-urlencoded")
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoomById(final MultivaluedMap<String, String> formParams)
            throws JSONException, IOException {
        final JSONObject json = new JSONObject();
        // Checking JWT...
        try {
            AuthenticationService.checkToken(formParams);
        } catch (final ServiceException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(json.toString()).build();
        }
        // verifying given id.
        final String idAsString = ServicesHelper.extractParam(formParams, ROOM_PARAM_ID);
        if (idAsString == null) {
            json.put("status", "error");
            json.put("message", "Vous devez fournir l'identifiant de l'utilisateur à modifier.");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();

        }
        int roomId = -1;
        try {
            roomId = Integer.parseUnsignedInt(idAsString);
        } catch (final NumberFormatException e) {
            json.put("status", "error");
            json.put("message", "L'identifiant fourni n'est pas un entier.");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
        }
        // Checking that a room exists with this id
        final Room room;
        try {
            room = DaoFactory.getInstance().getRoomDao().readRoom(roomId);
            if (room == null) {
                throw new DaoException("Aucune réunion trouvée avec cet identifiant.");
            }
        } catch (final DaoException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
        }

        // Trying to delete...
        try {
            DaoFactory.getInstance().getRoomDao().deleteRoom(room);
        } catch (final DaoException e) {
            json.put("status", "error");
            json.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
        }

        // Success !
        final ObjectMapper mapper = new ObjectMapper();
        final URI uri = UriBuilder.fromResource(this.getClass()).build(this);
        return Response.ok(uri).entity(mapper.writeValueAsString(room)).build();
    }

    private Room checkGivenRoom(final MultivaluedMap<String, String> formParams) throws ServiceException {
        final String name = ServicesHelper.extractParam(formParams, ROOM_PARAM_NAME);
        final String capacity = ServicesHelper.extractParam(formParams, ROOM_PARAM_CAPACITY);
        final String meetings = ServicesHelper.extractParam(formParams, ROOM_PARAM_MEETINGS);
        if (name == null) {
            throw new ServiceException(
                    "Vous devez fournir au moins le nom de la salle.");
        }
        final Room ret = new Room(name);
        if (capacity != null) {
            ret.setCapacity(Integer.parseInt(capacity));
        }
        // adding guests if provided.
        if (meetings != null && !meetings.isBlank()) {
            ret.setMeetings(parseRoomMeetingList(meetings));
        }
        return ret;
    }

    private List<Meeting> parseRoomMeetingList(final String commaSeparatedUsersIds) throws ServiceException {
        final StringTokenizer st = new StringTokenizer(commaSeparatedUsersIds, ",");
        final List<Integer> ids = new ArrayList<Integer>(st.countTokens());
        try {
            while (st.hasMoreTokens()) {
                ids.add(Integer.valueOf(st.nextToken()));
            }
        } catch (final NumberFormatException e) {
            throw new ServiceException("La liste des réunions n'est pas une suite d'identifiants.", e);
        }
        try {
            final List<Meeting> ret = DaoFactory.getInstance().getMeetingDao().readMeetingsByIdList(ids);
            if (ret.size() != ids.size()) {
                throw new ServiceException("La liste des réunions contient au moins un utilisateur non trouvé.");
            }
            return ret;
        } catch (final DaoException e) {
            throw new ServiceException("Impossible de lire la liste des réunions.", e);
        }
    }
}
