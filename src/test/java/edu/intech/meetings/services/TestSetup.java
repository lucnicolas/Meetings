package edu.intech.meetings.services;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.intech.meetings.dao.DaoFactory;
import edu.intech.meetings.model.User;
import edu.intech.meetings.services.AuthenticationService;
import edu.intech.meetings.servletListener.MeetingsContextListener;
import edu.intech.meetings.utils.PasswordHelper;

/**
 * Cette classe est le seul moyen d'exécuter du code <b><u>avant tous les tests
 * de toutes les classes de test</u></b>, les méthodes annotées avec
 * {@link BeforeAll} et {@link AfterAll} étant limitées à une seule classe de
 * test.<br>
 * <br>
 * Ceci est nécessaire pour émuler le démarrage du serveur via
 * {@link MeetingsContextListener#contextInitialized(javax.servlet.ServletContextEvent)}.<br>
 * qui est chargé de créer l'EntityManagerFactory. On émule aussi de cette façon
 * la destruction de cette EntityManagerFactory en apellant
 * {@link MeetingsContextListener#contextDestroyed(javax.servlet.ServletContextEvent)}.<br>
 * <br>
 * Pour simplifier la rédaction des tests, cette classe est utilisée dans la
 * classe abstraite {@link AbstractTest}. Il suffit de faire hériter toutes les
 * classes de test de cette dernière pour rendre disponibles les éléments
 * suivants :
 * <ul>
 * <li>Un {@link EntityManager} connecté à la base de données ainsi que les
 * instances de DAO gérés par la {@link DaoFactory}.</li>
 * <li>Au moins un utilisateur dans la base que l'on peut récupérer dans les
 * tests via la variable statique {@link TestSetup#testUser}. Cet utilisateur a,
 * entre autres, les caratéristiques suivantes :
 * <ul>
 * <li>Son nom est {@value #ROOT_USER_NAME}. On peut récupérer ce nom dans les
 * tests via la constante {@link TestSetup#ROOT_USER_NAME}</li>
 * <li>Son mot de passe est {@value #ROOT_USER_PWD}. On peut récupérer ce mot
 * dans passe dans les tests via la constante
 * {@link TestSetup#ROOT_USER_PWD}</li>
 * </ul>
 * </li>
 * <li>Un token valide pour toute la durée des tests (si vous ne changez pas la
 * valeur de la constante {@link AuthenticationService#TOKEN_LIFETIME_MS} et que
 * vos test durent moins d'une heure). On peut récupérer ce token dans les tests
 * via la variable statique {@link TestSetup#token}</li>
 * </ul>
 *
 * @author martin
 *
 */
public class TestSetup implements BeforeAllCallback, CloseableResource {

	final static String ROOT_USER_NAME = "Root";
	final static String ROOT_USER_PWD = "RootPass";
	static MeetingsContextListener ctx;
	static User testUser;
	static String token;

	@Override
	public void beforeAll(final ExtensionContext context) throws Exception {
		// We need to use a unique key here, across all usages of this particular
		// extension.
		final String uniqueKey = this.getClass().getName();
		final Object value = context.getRoot().getStore(GLOBAL).get(uniqueKey);
		if (value == null) {
			// First test container invocation.
			context.getRoot().getStore(GLOBAL).put(uniqueKey, this);
			ctx = new MeetingsContextListener();
			ctx.contextInitialized(null);
			testUser = new User(ROOT_USER_NAME, PasswordHelper.generateSecurePassword(ROOT_USER_PWD), "RootFirstName",
					"eMail@email.com");
			testUser = DaoFactory.getInstance().getUserDao().createUser(testUser, true);
			final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
			params.add("name", ROOT_USER_NAME);
			params.add("pwd", ROOT_USER_PWD);
			final AuthenticationService authenticationService = new AuthenticationService();
			final Response ret = authenticationService.autenticateUser(params);
			token = (String) ret.getEntity();
		}
	}

	@Override
	public void close() throws Throwable {
		DaoFactory.getInstance().getUserDao().deleteUser(testUser, true);
		ctx.contextDestroyed(null);
	}

}
