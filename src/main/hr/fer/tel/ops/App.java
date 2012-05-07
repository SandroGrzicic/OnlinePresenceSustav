package hr.fer.tel.ops;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import net.sandrogrzicic.java.fp.Either;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import spark.*;
import spark.route.SimpleRouteMatcher;

import javax.servlet.http.Cookie;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static spark.Spark.before;
import static spark.Spark.get;

/**
 * Entry point za web aplikaciju.
 */
public class App {

	/**
	 * Instanca Server klase.
	 */
	private final Server s;
	/**
	 * Cache podržanih ruta.
	 */
	private String podržaneRute;

	/**
	 * Formatira izlazni XML.
	 */
	private static final XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());

	long authenticationTimeout = 1000; //in milliseconds

	private Date adminAuthenticated = null;
	final private String adminCookieKey = "auth";
	private String adminCookie = "admin";

	public App() {
		this.s = new Server();
	}

	/**
	 * Pokreće ovu web aplikaciju.
	 */
	public void pokreni() {
		postaviAuth();

		postaviRute();

		podržaneRute = dohvatiRute();

		// ispisuje sve podržane rute koristeći Reflection API.
		get(new Route("/") {
			@Override
			public Object handle(final Request request, final Response response) {
				return podržaneRute;
			}
		});

	}

	public boolean isAdmin(String user, String pass) {
		return user.equals("temp") && pass.equals("temp");
	}

	public Cookie setAdmin(String ip) {
		this.adminCookie = UUID.randomUUID().toString();
		this.adminAuthenticated = new Date();
		Cookie c = new Cookie(adminCookieKey, adminCookie);
		c.setMaxAge((int) (authenticationTimeout / 1000));
		return c;
	}

	private void postaviAuth() {
		before(new Filter("/unreg/*") {
			@Override
			public void handle(Request request, Response response) {
				boolean authenticated = false;

				if (adminAuthenticated != null) {
					final Date now = new Date();
					if (now.getTime() - adminAuthenticated.getTime() < authenticationTimeout) {
						for (Cookie c : request.raw().getCookies()) {
							if (c.getName().equals(adminCookieKey)) {
								if (c.getValue().equals(adminCookie))
									authenticated = true;
							}

						}
					}
				}


				if (!authenticated) {
					String authHeader = request.headers("Authorization");
					if ((authHeader != null) && (authHeader.startsWith("Basic"))) {
						authHeader = authHeader.substring("Basic".length()).trim();

						try {
							authHeader = new String(Base64.decode(authHeader));
							System.out.println(authHeader);
							String user = authHeader.split(":")[0];
							String password = authHeader.split(":")[1];

							if (isAdmin(user, password)) {
								Cookie c = setAdmin(request.ip());
								response.raw().addCookie(c);
								authenticated = true;
							}

						} catch (Base64DecodingException ignored) {}

					}
				}

				if (!authenticated) {
					response.header("WWW-Authenticate", "Basic");
					halt(401, "Please login");
				}
			}
		});


	}

	/**
	 * Postavlja rute koje ćemo podržati.
	 */
	private void postaviRute() {
		// zahtjev za registracijom
		get(new Route("/reg/:login/:pass") {
			@Override
			public Object handle(Request request, Response response) {
				String korisničkoIme = request.params(":login");
				String lozinka = request.params(":pass");

				Either<String, String> odgovor = s.zahtjevZaRegistracijom(korisničkoIme, lozinka);

				return either2xml(response, odgovor, SC_BAD_REQUEST);
			}
		});

		// zahtjev za ukidanjem registracije
		get(new Route("/unreg/:login/:pass") {
			@Override
			public Object handle(Request request, Response response) {
				String korisničkoIme = request.params(":login");
				String lozinka = request.params(":pass");

				Either<String, String> odgovor = s.zahtjevZaUkidanjeRegistracije(korisničkoIme, lozinka);

				return either2xml(response, odgovor, SC_BAD_REQUEST);
			}
		});

		// vraća broj registriranih korisnika.
		get(new Route("/count") {
			@Override
			public Object handle(final Request request, final Response response) {
				return string2xml(Integer.toString(s.getBrojKorisnika()));
			}
		});

		get(new Route("/zahtjev/:presentity/:vrsta") {
			@Override
			public Object handle(final Request request, final Response response) {
				final VrstaPracenja vrstaPraćenja;

				final char reqVrstaPraćenja = request.params(":vrsta").charAt(0);
				if (reqVrstaPraćenja == 'a') {
					vrstaPraćenja = VrstaPracenja.AKTIVNO;
				} else if (reqVrstaPraćenja == 'p') {
					vrstaPraćenja = VrstaPracenja.PASIVNO;
				} else {
					vrstaPraćenja = VrstaPracenja.NEDEFINIRANO;
				}

				final String presentity = request.params(":presentity");

				request.raw().getCookies();

				s.zahtjevZaPraćenjem("watcher", vrstaPraćenja, presentity);
				return null;
			}
		});


	}

	/**
	 * Pokreće web aplikaciju.
	 */
	public static void main(String[] args) {
		new App().pokreni();
	}


	/**
	 * Vraća sve trenutno podržane rute koristeći Reflection API.
	 */
	private static String dohvatiRute() {
		final StringBuilder sb = new StringBuilder("REST metode: \n\n");
		try {
			final Field routeMatcher = Spark.class.getDeclaredField("routeMatcher");
			routeMatcher.setAccessible(true);
			final SimpleRouteMatcher simpleRouteMatcher = (SimpleRouteMatcher) routeMatcher.get(null);

			final Field routesField = simpleRouteMatcher.getClass().getDeclaredField("routes");
			routesField.setAccessible(true);

			@SuppressWarnings("unchecked")
			final List<?> routes = (List<?>) routesField.get(simpleRouteMatcher);
			for (final Object route : routes) {
				sb.append(route.toString().split(", ")[1]).append("\n");
			}
			return sb.toString();
		} catch (Exception ignored) {
			return ignored.getMessage();
		}
	}

	/**
	 * Generira XML odgovor na temelju zadanog Stringa.
	 */
	private static String string2xml(final String string) {
		final Document doc = new Document();
		final Element root = new Element("ops");
		doc.setRootElement(root);

		final Element odgovorEl = new Element("odgovor");

		odgovorEl.addContent(string);
		root.addContent(odgovorEl);
		return xmlOut.outputString(doc);
	}

	/**
	 * Generira XML odgovor na temelju zadanog Eithera.
	 */
	private static String either2xml(final Response httpResponse, final Either<String, String> either, final int greškaKod) {
		final Document doc = new Document();
		final Element root = new Element("ops");
		doc.setRootElement(root);

		final Element odgovorEl = new Element(either.getClass().getSimpleName().toLowerCase());

		if (either.isLeft()) {
			httpResponse.status(greškaKod);
		}

		odgovorEl.addContent(either.toString());
		root.addContent(odgovorEl);
		return xmlOut.outputString(doc);
	}

}
