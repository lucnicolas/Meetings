
package edu.intech.meetings.services;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import edu.intech.meetings.dao.DaoFactory;
import edu.intech.meetings.exceptions.ServiceException;
import edu.intech.meetings.model.User;
import edu.intech.meetings.utils.PasswordHelper;
import edu.intech.meetings.utils.ServicesHelper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Path("/authentication")
public class AuthenticationService {

	public final static String AUTH_PARAM_TOKEN = "token";
	public final static long TOKEN_LIFETIME_MS = 3600000; // 1h
	private final static String SECRET_API_KEY = "ViveLesGrenouilles";
	private final static String TOKEN_SUBJECT = "EvalS5 API";

	// The JWT signature algorithm we will be using to sign the password in DB and
	// the token
	private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

	@POST
	@Path("/login/")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response autenticateUser(final MultivaluedMap<String, String> formParams) {

		final String userName = ServicesHelper.extractParam(formParams, UsersService.USER_PARAM_NAME);
		final String userPwd = ServicesHelper.extractParam(formParams, UsersService.USER_PARAM_PWD);
		if (userName == null || userName.isBlank() || userPwd == null || userPwd.isBlank()) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		try {

			// Authenticate the user using the credentials provided
			authenticate(userName, userPwd);

			// Issue a token for the user
			final String token = issueToken(userName, TOKEN_LIFETIME_MS);

			// Return the token on the response
			return Response.ok(token).build();

		} catch (final Exception e) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}
	}

	// The issued token must be associated to a user
	private String issueToken(final String issuer, final long ttlMillis) {

		final String id = UUID.randomUUID().toString();

		// The JWT signature algorithm we will be using to sign the token

		final long nowMillis = System.currentTimeMillis();
		final Date now = new Date(nowMillis);

		// We will sign our JWT with our ApiKey secret
		final byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_API_KEY);
		final Key signingKey = new SecretKeySpec(apiKeySecretBytes, this.signatureAlgorithm.getJcaName());

		// Let's set the JWT Claims
		final JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(TOKEN_SUBJECT).setIssuer(issuer)
				.signWith(this.signatureAlgorithm, signingKey);

		// if it has been specified, let's add the expiration
		if (ttlMillis >= 0) {
			final long expMillis = nowMillis + ttlMillis;
			final Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		// Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}

	private User authenticate(final String username, final String password) throws Exception {
		// Throw an Exception if the credentials are invalid
		final User ret = DaoFactory.getInstance().getUserDao().readUserByName(username);
		if (ret != null) {
			if (PasswordHelper.verifyUserPassword(password, ret.getPassword())) {
				return ret;
			}
		}
		throw new ServiceException("Utilisateur inconnu ou mauvais mot de passe.");
	}

	public static String checkToken(final MultivaluedMap<String, String> formParams) throws ServiceException {
		final String token = ServicesHelper.extractParam(formParams, AUTH_PARAM_TOKEN);
		if (token == null || token.isBlank()) {
			throw new ServiceException("Vous devez fournir un token d'identification valide");
		}
		return ServicesHelper.parseJWT(token, SECRET_API_KEY);
	}

}