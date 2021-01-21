package edu.intech.meetings.utils;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;

import edu.intech.meetings.exceptions.ServiceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class ServicesHelper {

	// Email Regex java
	private static final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";

	// static Pattern object, since pattern is fixed
	private static final Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

	/**
	 * This method validates the input email address with EMAIL_REGEX pattern
	 * 
	 * @param email E-mail adress to test
	 * @return <code>true</code> is given parameter is a valid email adress,
	 *         <code>false</code> otherwise.
	 */
	public static boolean validateEmail(final String email) {
		final Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/**
	 * Extracts a parameter from a received multivalued map.
	 *
	 * @param formParams The map to extract parameter from.
	 * @param paramName  name of the parameter to get.
	 * @return Found parameter value or <code>null</code> if not found.
	 */
	public static String extractParam(final MultivaluedMap<String, String> formParams, final String paramName) {
		final List<String> temp = formParams.get(paramName);
		if (temp != null && temp.size() >= 1) {
			return temp.get(0);
		}
		return null;
	}

	/**
	 * This method will test given jwt token and returns the correponding profile
	 *
	 * @param jwt          Token to test.
	 * @param secretApiKey Secret key of the API.
	 * @return User name contained in token.
	 * @throws Exception if given String is not a valid JWT or if token is expired.
	 */
	public static String parseJWT(final String jwt, final String secretApiKey) throws ServiceException {

		Claims claims = null;
		try {
			claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secretApiKey))
					.parseClaimsJws(jwt).getBody();
		} catch (final Exception e) {
			throw new ServiceException("Token invalide ou périmé");
		}

		final Date now = new Date(System.currentTimeMillis());
		if (now.after(claims.getExpiration())) {
			throw new ServiceException("La durée de validité du Token est expirée.");
		}
		return claims.getIssuer();
	}
}