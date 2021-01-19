package edu.intech.meetings.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.intech.meetings.exceptions.ServiceException;
import edu.intech.meetings.services.AuthenticationService;

/**
 * Classe servant à tester les services d'authentification de la classe
 * {@link AuthenticationService}. Cette classe hérite de {@link AbstractTest}
 * afin que les tests lancés puissent profiter des initialisation et fermetures
 * globales proposées par la classe {@link TestSetup}.
 * 
 * @author martin
 *
 */
public class AuthenticationServiceTest extends AbstractTest {

	final AuthenticationService authenticationService = new AuthenticationService();

	@Test
	public void testAutenticateUser() {
		final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		// Premier test sans paramètres.
		Response ret = this.authenticationService.autenticateUser(params);
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
				"Le status de la réponse devrait être FORBIDDEN (403).");
		// Test avec paramètres bidon
		params.add("name", "hfdshgrdqg45f6dsq5f61");
		params.add("pwd", "1");
		ret = this.authenticationService.autenticateUser(params);
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ret.getStatus(),
				"Le status de la réponse devrait être FORBIDDEN (403).");
		// Test avec user créé lors de l'init du test.
		params.clear();
		params.add("name", TestSetup.ROOT_USER_NAME);
		params.add("pwd", TestSetup.ROOT_USER_PWD);
		ret = this.authenticationService.autenticateUser(params);
		assertEquals(Response.Status.OK.getStatusCode(), ret.getStatus(),
				"Le status de la réponse devrait être OK (200).");
		// Vérification du token reçu.
		final String token = (String) ret.getEntity();
		params.clear();
		params.add("token", token);
		try {
			assertEquals(AuthenticationService.checkToken(params), TestSetup.ROOT_USER_NAME,
					"La vérification du token devrait renvoyer le nom d'utilisateur " + TestSetup.ROOT_USER_NAME);
		} catch (final ServiceException e) {
			fail(e.getMessage());
		}
		// Vérification si token vide.
		params.clear();
		assertThrows(ServiceException.class, () -> {
			AuthenticationService.checkToken(params);
		}, "La vérification du token vide devrait renvoyer une " + ServiceException.class.getName());
	}

}
