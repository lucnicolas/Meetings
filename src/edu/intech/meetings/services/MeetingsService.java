package edu.intech.meetings.services;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.intech.meetings.dao.DaoFactory;
import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.exceptions.ServiceException;
import edu.intech.meetings.model.Meeting;
import edu.intech.meetings.model.User;
import edu.intech.meetings.utils.ServicesHelper;

@Path("/meetings")
public class MeetingsService {

	public final static String MEETING_PARAM_ID = "id";
	public final static String MEETING_PARAM_TITLE = "title";
	public final static String MEETING_PARAM_START = "start";
	public final static String MEETING_PARAM_DURATION = "duration";
	public final static String MEETING_PARAM_GUESTS = "guests";

	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat(Meeting.DATETIME_PATTERN, Locale.FRANCE);
	static {
		// Cette ligne est nécessaire pour que le formateur refuse les date incoérentes
		// comme "33/22/2020 40:73".
		DATE_FORMATTER.setLenient(false);
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMeetings() throws JSONException, JsonGenerationException, JsonMappingException, IOException {

		// No token needed to read all meetings.
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return Response.ok()
					.entity(mapper.writeValueAsString(DaoFactory.getInstance().getMeetingDao().readAllMeetings()))
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
	public Response getMeetingById(final MultivaluedMap<String, String> formParams)
			throws JSONException, JsonGenerationException, JsonMappingException, IOException {
		// No token needed to read all meetings.

		final JSONObject json = new JSONObject();
		// verifying given id.
		final String idAsString = ServicesHelper.extractParam(formParams, MEETING_PARAM_ID);
		if (idAsString == null) {
			json.put("status", "error");
			json.put("message", "Vous devez fournir l'identifiant.");
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		int meetingId = -1;
		try {
			meetingId = Integer.parseUnsignedInt(idAsString);
		} catch (final NumberFormatException e) {
			json.put("status", "error");
			json.put("message", "L'identifiant fourni n'est pas un entier.");
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		Meeting meeting = null;
		try {
			meeting = DaoFactory.getInstance().getMeetingDao().readMeeting(meetingId);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}
		if (meeting == null) {
			json.put("status", "error");
			json.put("message", "Meeting non trouvé");
			return Response.status(Response.Status.NOT_FOUND).entity(json.toString()).build();
		} else {
			final ObjectMapper mapper = new ObjectMapper();
			return Response.ok().entity(mapper.writeValueAsString(meeting)).build();
		}
	}

	@GET
	@Path("/getByUserId")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMeetingsByUserId(final MultivaluedMap<String, String> formParams)
			throws JSONException, JsonGenerationException, JsonMappingException, IOException {
		// No token needed to read meetings.

		final JSONObject json = new JSONObject();
		// verifying given id.
		final String idAsString = ServicesHelper.extractParam(formParams, MEETING_PARAM_ID);
		if (idAsString == null) {
			json.put("status", "error");
			json.put("message", "Vous devez fournir l'identifiant.");
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		int userId = -1;
		try {
			userId = Integer.parseUnsignedInt(idAsString);
		} catch (final NumberFormatException e) {
			json.put("status", "error");
			json.put("message", "L'identifiant fourni n'est pas un entier.");
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		List<Meeting> meetings = null;
		try {
			meetings = DaoFactory.getInstance().getMeetingDao().readAllMeetingsWithUser(userId);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}
		final ObjectMapper mapper = new ObjectMapper();
		return Response.ok().entity(mapper.writeValueAsString(meetings)).build();
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addMeeting(final MultivaluedMap<String, String> formParams)
			throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		final JSONObject json = new JSONObject();
		// Checking JWT...
		try {
			AuthenticationService.checkToken(formParams);
		} catch (final ServiceException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.FORBIDDEN).entity(json.toString()).build();
		}
		Meeting meeting;
		// Verifying given parameters
		try {
			meeting = checkGivenMeeting(formParams);
		} catch (final ServiceException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		// Trying to create...
		try {
			DaoFactory.getInstance().getMeetingDao().createMeeting(meeting, true);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}
		// Success !
		final ObjectMapper mapper = new ObjectMapper();
		final URI serieId = UriBuilder.fromResource(this.getClass()).build(this);
		return Response.created(serieId).entity(mapper.writeValueAsString(meeting)).build();
	}

	@PUT
	@Consumes("application/x-www-form-urlencoded")
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateMeeting(final MultivaluedMap<String, String> formParams)
			throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		final JSONObject json = new JSONObject();
		// Checking JWT...
		try {
			AuthenticationService.checkToken(formParams);
		} catch (final ServiceException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.FORBIDDEN).entity(json.toString()).build();
		}
		Meeting meeting;
		// Verifying given parameters
		try {
			meeting = checkGivenMeeting(formParams);
		} catch (final ServiceException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		// verifying given id.
		final String idAsString = ServicesHelper.extractParam(formParams, MEETING_PARAM_ID);
		if (idAsString == null) {
			json.put("status", "error");
			json.put("message", "Vous devez fournir l'identifiant de la réunion à modifier.");
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();

		}
		int meetingId = -1;
		try {
			meetingId = Integer.parseUnsignedInt(idAsString);
		} catch (final NumberFormatException e) {
			json.put("status", "error");
			json.put("message", "L'identifiant fourni n'est pas un entier.");
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		// Checking that a meeting exists with this id
		try {
			final Meeting temp = DaoFactory.getInstance().getMeetingDao().readMeeting(meetingId);
			if (temp == null) {
				throw new DaoException("Aucune réunion trouvée avec cet identifiant.");
			}
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}

		// Trying to update...
		meeting.setId(meetingId);
		try {
			DaoFactory.getInstance().getMeetingDao().updateMeeting(meeting, true);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}

		// Success !
		final ObjectMapper mapper = new ObjectMapper();
		final URI uri = UriBuilder.fromResource(this.getClass()).build(this);
		return Response.ok(uri).entity(mapper.writeValueAsString(meeting)).build();
	}

	@DELETE
	@Consumes("application/x-www-form-urlencoded")
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteMeetingById(final MultivaluedMap<String, String> formParams)
			throws JSONException, JsonGenerationException, JsonMappingException, IOException {
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
		final String idAsString = ServicesHelper.extractParam(formParams, MEETING_PARAM_ID);
		if (idAsString == null) {
			json.put("status", "error");
			json.put("message", "Vous devez fournir l'identifiant de l'utilisateur à modifier.");
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();

		}
		int meetingId = -1;
		try {
			meetingId = Integer.parseUnsignedInt(idAsString);
		} catch (final NumberFormatException e) {
			json.put("status", "error");
			json.put("message", "L'identifiant fourni n'est pas un entier.");
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		// Checking that a meeting exists with this id
		final Meeting meeting;
		try {
			meeting = DaoFactory.getInstance().getMeetingDao().readMeeting(meetingId);
			if (meeting == null) {
				throw new DaoException("Aucune réunion trouvée avec cet identifiant.");
			}
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}

		// Trying to delete...
		try {
			DaoFactory.getInstance().getMeetingDao().deleteMeeting(meeting);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}

		// Success !
		final ObjectMapper mapper = new ObjectMapper();
		final URI uri = UriBuilder.fromResource(this.getClass()).build(this);
		return Response.ok(uri).entity(mapper.writeValueAsString(meeting)).build();
	}

	private Meeting checkGivenMeeting(final MultivaluedMap<String, String> formParams) throws ServiceException {
		final String title = ServicesHelper.extractParam(formParams, MEETING_PARAM_TITLE);
		final String startAsString = ServicesHelper.extractParam(formParams, MEETING_PARAM_START);
		final String durationAsString = ServicesHelper.extractParam(formParams, MEETING_PARAM_DURATION);
		final String guests = ServicesHelper.extractParam(formParams, MEETING_PARAM_GUESTS);
		if (title == null || startAsString == null || durationAsString == null) {
			throw new ServiceException(
					"Vous devez fournir au moins le nom, la date/heure de début et la durée de la réunion.");
		}
		// Start date/time conversion...
		Date start = null;
		try {
			start = DATE_FORMATTER.parse(startAsString);
		} catch (final ParseException e) {
			throw new ServiceException(
					"La date de début fournie pour la réunion ne peut être convertie (format attendu : {"
							.concat(Meeting.DATETIME_PATTERN).concat("} reçu : {").concat(startAsString).concat("})."));
		}
		// Duration conversion...
		int duration = -1;
		try {
			duration = Integer.parseUnsignedInt(durationAsString);
		} catch (final NumberFormatException e) {
			throw new ServiceException(
					"La durée fournie n'est pas un entier.");
		}
		final Meeting ret = new Meeting(title, start, duration);
		// adding guests if provided.
		if (guests != null && !guests.isBlank()) {
			ret.setGuests(parseMeetingUsersList(guests));
		}
		return ret;
	}

	private List<User> parseMeetingUsersList(final String commaSeparatedUsersIds) throws ServiceException {
		final StringTokenizer st = new StringTokenizer(commaSeparatedUsersIds, ",");
		final List<Integer> ids = new ArrayList<Integer>(st.countTokens());
		try {
			while (st.hasMoreTokens()) {
				ids.add(Integer.valueOf(st.nextToken()));
			}
		} catch (final NumberFormatException e) {
			throw new ServiceException("La liste des invités de la réunion n'est pas une suite d'identifiants.", e);
		}
		try {
			final List<User> ret = DaoFactory.getInstance().getUserDao().readUsersByIdList(ids);
			if (ret.size() != ids.size()) {
				throw new ServiceException("La liste des invités de la réunion contient au moins un utilisateur non trouvé.");
			}
			return ret;
		} catch (final DaoException e) {
			throw new ServiceException("Impossible de lire la liste des invités de la réunion.", e);
		}
	}
}
