package edu.intech.meetings.services;

import java.io.IOException;
import java.net.URI;

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
import edu.intech.meetings.model.User;
import edu.intech.meetings.utils.PasswordHelper;
import edu.intech.meetings.utils.ServicesHelper;

@Path("/users")
public class UsersService {

	public final static String USER_PARAM_ID = "id";
	public final static String USER_PARAM_NAME = "name";
	public final static String USER_PARAM_PWD = "pwd";
	public final static String USER_PARAM_FIRSTNAME = "firstName";
	public final static String USER_PARAM_MAIL = "mail";

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers() throws JSONException, JsonGenerationException, JsonMappingException, IOException {
		// No token needed to read all users.
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return Response.ok().entity(mapper.writeValueAsString(DaoFactory.getInstance().getUserDao().readAllUsers()))
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
	public Response getUserById(final MultivaluedMap<String, String> formParams)
			throws JSONException, JsonGenerationException, JsonMappingException, IOException {
		// No token needed to read.

		final JSONObject json = new JSONObject();
		final ObjectMapper mapper = new ObjectMapper();

		// verifying given id.
		final String idAsString = ServicesHelper.extractParam(formParams, USER_PARAM_ID);
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
		User user = null;
		try {
			user = DaoFactory.getInstance().getUserDao().readUser(userId);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.NOT_FOUND).entity(json.toString()).build();
		}
		if (user == null) {
			json.put("status", "error");
			json.put("message", "Utilisateur non trouvé");
			return Response.status(Response.Status.NOT_FOUND).entity(json.toString()).build();
		} else {
			return Response.ok().entity(mapper.writeValueAsString(user)).build();
		}
	}

	@POST
	@Path("/add")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUser(final MultivaluedMap<String, String> formParams)
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
		User user;
		// Verifying given parameters
		try {
			user = checkGivenUser(formParams);
		} catch (final ServiceException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		// Trying to create...
		try {
			user = DaoFactory.getInstance().getUserDao().createUser(user, true);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}
		// Success !
		final ObjectMapper mapper = new ObjectMapper();
		final URI serieId = UriBuilder.fromResource(this.getClass()).build(this);
		return Response.created(serieId).entity(mapper.writeValueAsString(user)).build();
	}

	@PUT
	@Path("/update")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUser(final MultivaluedMap<String, String> formParams)
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
		User user;
		// Verifying given parameters
		try {
			user = checkGivenUser(formParams);
		} catch (final ServiceException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}
		// verifying given id.
		final String idAsString = ServicesHelper.extractParam(formParams, USER_PARAM_ID);
		if (idAsString == null) {
			json.put("status", "error");
			json.put("message", "Vous devez fournir au moins l'identifiant, le nom et le mot de passe.");
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
		// Checking that a user exists with this id
		try {
			final User temp = DaoFactory.getInstance().getUserDao().readUser(userId);
			if (temp == null) {
				throw new DaoException("Aucun utilisateur trouvé avec cet identifiant.");
			}
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}

		// Trying to update...
		user.setId(userId);
		try {
			DaoFactory.getInstance().getUserDao().updateUser(user, true);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}

		// Success !
		final ObjectMapper mapper = new ObjectMapper();
		final URI uri = UriBuilder.fromResource(this.getClass()).build(this);
		return Response.ok(uri).entity(mapper.writeValueAsString(user)).build();
	}

	@DELETE
	@Path("/delete")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUserById(final MultivaluedMap<String, String> formParams)
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
		final String idAsString = ServicesHelper.extractParam(formParams, USER_PARAM_ID);
		if (idAsString == null) {
			json.put("status", "error");
			json.put("message", "Vous devez fournir l'identifiant de l'utilisateur à modifier.");
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
		// Checking that a user exists with this id
		final User user;
		try {
			user = DaoFactory.getInstance().getUserDao().readUser(userId);
			if (user == null) {
				throw new DaoException("Aucun utilisateur trouvé avec cet identifiant.");
			}
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(json.toString()).build();
		}

		// Trying to delete...
		try {
			DaoFactory.getInstance().getUserDao().deleteUser(user, true);
		} catch (final DaoException e) {
			json.put("status", "error");
			json.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}

		// Success !
		final ObjectMapper mapper = new ObjectMapper();
		final URI uri = UriBuilder.fromResource(this.getClass()).build(this);
		return Response.ok(uri).entity(mapper.writeValueAsString(user)).build();
	}

	private User checkGivenUser(final MultivaluedMap<String, String> formParams) throws ServiceException {
		final String name = ServicesHelper.extractParam(formParams, USER_PARAM_NAME);
		final String firstName = ServicesHelper.extractParam(formParams, USER_PARAM_FIRSTNAME);
		final String eMail = ServicesHelper.extractParam(formParams, USER_PARAM_MAIL);
		final String password = ServicesHelper.extractParam(formParams, USER_PARAM_PWD);
		if (name == null || password == null) {
			throw new ServiceException("Vous devez fournir au moins le nom et le mot de passe.");
		}
		if (eMail != null && !ServicesHelper.validateEmail(eMail)) {
			throw new ServiceException("Vous devez fournir une adresse email valide.");
		}
		return new User(name, PasswordHelper.generateSecurePassword(password), firstName, eMail);
	}
}
