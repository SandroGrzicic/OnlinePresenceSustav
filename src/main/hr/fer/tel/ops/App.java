package hr.fer.tel.ops;

import net.sandrogrzicic.java.fp.Either;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.route.SimpleRouteMatcher;

import java.lang.reflect.Field;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static spark.Spark.get;

/**
 * Entry point za web aplikaciju.
 */
public class App {

	/**
	 * Instanca Server klase.
	 */
	private final Server server;
	/**
	 * Cache podržanih ruta.
	 */
	private String podržaneRute;

	/**
	 * Formatira izlazni XML.
	 */
	private static final XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());

	public App() {
		this.server = new Server();
	}

	/**
	 * Pokreće web aplikaciju.
	 */
	public static void main(String[] args) {
		new App().pokreni();
	}

	/**
	 * Pokreće ovu web aplikaciju.
	 */
	public void pokreni() {

		postaviRute();

		// index: koristi Reflection za prikaz svih dostupnih ruta
		podržaneRute = dohvatiRute();
		// ispisuje sve podržane rute koristeći Reflection API.
		get(new Route("/") {
			@Override
			public Object handle(final Request request, final Response response) {
				return podržaneRute;
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

				Either<String, String> odgovor = server.zahtjevZaRegistracijom(korisničkoIme, lozinka);

				return either2xml(response, odgovor);
			}
		});

		// zahtjev za ukidanjem registracije
		get(new Route("/unreg/:login/:pass") {
			@Override
			public Object handle(Request request, Response response) {
				String korisničkoIme = request.params(":login");
				String lozinka = request.params(":pass");

				Either<String, String> odgovor = server.zahtjevZaUkidanjeRegistracije(korisničkoIme, lozinka);

				return either2xml(response, odgovor);
			}
		});

		// vraća broj registriranih korisnika.
		get(new Route("/count") {
			@Override
			public Object handle(final Request request, final Response response) {
				return string2xml(Integer.toString(server.getBrojKorisnika()));
			}
		});

		get(new Route("/zahtjev/:watcher/:pass/:presentity/:vrsta") {
			@Override
			public Object handle(final Request request, final Response response) {

				final String poruka = provjeriLoginPodatke(request.params(":watcher"), request.params(":pass"));
				if (poruka != null) {
					return poruka;
				}

				final VrstaPracenja vrstaPraćenja;

				final char reqVrstaPraćenja = request.params(":vrsta").charAt(0);
				if (reqVrstaPraćenja == 'a') {
					vrstaPraćenja = VrstaPracenja.AKTIVNO;
				} else if (reqVrstaPraćenja == 'p') {
					vrstaPraćenja = VrstaPracenja.PASIVNO;
				} else {
					vrstaPraćenja = VrstaPracenja.NEDEFINIRANO;
				}

				final String watcher = request.params(":watcher");
				final String presentity = request.params(":presentity");

				return either2xml(response, server.zahtjevZaPraćenjem(watcher, vrstaPraćenja, presentity));
			}
		});

		get(new Route("/ocisti/:presentity/:pass") {
			@Override
			public Object handle(final Request request, final Response response) {

				final String poruka = provjeriLoginPodatke(request.params(":presentity"), request.params(":pass"));
				if (poruka != null) {
					return poruka;
				}

				final String presentity = request.params(":presentity");

				final String brojZahtjeva = String.valueOf(server.dohvatiBrojZahtjevaZaPraćenjem(presentity));

				server.očistiZahtjeveZaPraćenjem(presentity);

				return string2xml("Uspješno očišćeno " + brojZahtjeva + " zahtjeva.");
			}
		});

	}

	/**
	 * Provjerava ispravnost korisničkih podataka. Ako su neispravni, vraća odgovarajući XML odgovor,
	 * inače vraća null.
	 */
	private String provjeriLoginPodatke(final String korisničkoIme, final String lozinka) {
		if (server.provjeriLogin(korisničkoIme, lozinka)) {
			return null;
		} else {
			return string2xml("Korisnički podaci neispravni.");
		}
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
	private static String either2xml(final Response httpResponse, final Either<String, String> either) {
		final Document dokument = new Document();
		final Element root = new Element("ops");
		dokument.setRootElement(root);

		final Element odgovorEl = new Element(either.getClass().getSimpleName().toLowerCase());
		root.addContent(odgovorEl);

		// vrati http odgovor sa kodom greške
		if (either.isLeft()) {
			httpResponse.status(SC_BAD_REQUEST);
		}

		odgovorEl.addContent(either.toString());

		return xmlOut.outputString(dokument);
	}

}
