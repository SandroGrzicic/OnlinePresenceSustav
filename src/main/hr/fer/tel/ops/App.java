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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
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
		get(new Route("/brojKorisnika") {
			@Override
			public Object handle(final Request request, final Response response) {
				return string2xml("brojKorisnika", Integer.toString(server.getBrojKorisnika()));
			}
		});

		get(new Route("/zahtjev/:watcher/:pass/:presentity/:vrsta") {
			@Override
			public Object handle(final Request request, final Response response) {
				if (!server.provjeriLogin(request.params(":watcher"), request.params(":pass"))) {
					halt(SC_FORBIDDEN, string2xml("greška", "Korisnički podaci neispravni."));
				}

				final String watcher = request.params(":watcher");
				final VrstaPracenja vrstaPraćenja = VrstaPracenja.char2vrsta(request.params(":vrsta").charAt(0));
				final String presentity = request.params(":presentity");

				return either2xml(response, server.zahtjevZaPraćenjem(watcher, vrstaPraćenja, presentity));
			}
		});

		get(new Route("/zahtjevi/ocisti/:presentity/:pass") {
			@Override
			public Object handle(final Request request, final Response response) {
				if (!server.provjeriLogin(request.params(":presentity"), request.params(":pass"))) {
					halt(SC_FORBIDDEN, string2xml("greška", "Korisnički podaci neispravni."));
				}

				final String presentity = request.params(":presentity");
				final String brojZahtjeva = String.valueOf(server.dohvatiBrojZahtjevaZaPraćenjem(presentity));

				server.očistiZahtjeveZaPraćenjem(presentity);

				return string2xml("Uspješno očišćeno " + brojZahtjeva + " zahtjeva.");
			}
		});

		get(new Route("/zahtjevi/dohvati/:presentity/:pass") {
			@Override
			public Object handle(final Request request, final Response response) {
				if (!server.provjeriLogin(request.params(":presentity"), request.params(":pass"))) {
					halt(SC_FORBIDDEN, string2xml("greška", "Korisnički podaci neispravni."));
				}

				final String presentity = request.params(":presentity");

				return either2xml(response, server.zahtjeviZaPraćenjem(presentity));
			}
		});

		get(new Route("/odgovoriNaZahtjev/:presentity/:pass/:watcher/:vrsta/:odgovor") {
			@Override
			public Object handle(final Request request, final Response response) {
				if (!server.provjeriLogin(request.params(":presentity"), request.params(":pass"))) {
					halt(SC_FORBIDDEN, string2xml("greška", "Korisnički podaci neispravni."));
				}

				final String presentity = request.params(":presentity");
				final VrstaPracenja vrstaPraćenja = VrstaPracenja.char2vrsta(request.params(":vrsta").charAt(0));
				final String watcher = request.params(":watcher");
				// odgovor je potvrdan ako počinje s t
				final boolean odgovor = request.params(":odgovor").toLowerCase().startsWith("t");

				return either2xml(response, server.odgovorNaZahtjevZaPraćenjem(presentity, watcher, vrstaPraćenja, odgovor));
			}
		});

		get(new Route("/prisutnost/promjeni/:presentity/:pass/:prisutnost") {
			@Override
			public Object handle(final Request request, final Response response) {
				if (!server.provjeriLogin(request.params(":presentity"), request.params(":pass"))) {
					halt(SC_FORBIDDEN, string2xml("greška", "Korisnički podaci neispravni."));
				}

				final String presentity = request.params(":presentity");
				Prisutnost prisutnost = null;
				try {
				    prisutnost = Prisutnost.char2prisutnost(request.params(":prisutnost").charAt(0));
				} catch (IllegalArgumentException e) {
					halt(SC_BAD_REQUEST, string2xml("greška", "Nevaljan tip prisutnosti."));
				}

				return either2xml(response, server.promjenaPrisutnosti(presentity, prisutnost));
			}
		});

		get(new Route("/prisutnost/dohvati/:watcher/:pass/:presentity") {
			@Override
			public Object handle(final Request request, final Response response) {
				if (!server.provjeriLogin(request.params(":watcher"), request.params(":pass"))) {
					halt(SC_FORBIDDEN, string2xml("greška", "Korisnički podaci neispravni."));
				}
				final String watcher = request.params(":watcher");
				final String presentity = request.params(":presentity");

				return either2xml(response, server.dohvatiPrisutnost(watcher, presentity));
			}
		});

		get(new Route("ukiniPracenje/presentity/:presentity/:pass/:watcher") {
			@Override
			public Object handle(final Request request, final Response response) {
				if (!server.provjeriLogin(request.params(":presentity"), request.params(":pass"))) {
					halt(SC_FORBIDDEN, string2xml("greška", "Korisnički podaci neispravni."));
				}
				final String presentity = request.params(":presentity");
				final String watcher = request.params(":watcher");

				return either2xml(response, server.ukiniPraćenje(presentity, watcher));
			}
		});

		get(new Route("ukiniPracenje/watcher/:watcher/:pass/:presentity") {
			@Override
			public Object handle(final Request request, final Response response) {
				if (!server.provjeriLogin(request.params(":watcher"), request.params(":pass"))) {
					halt(SC_FORBIDDEN, string2xml("greška", "Korisnički podaci neispravni."));
				}
				final String watcher = request.params(":watcher");
				final String presentity = request.params(":presentity");

				return either2xml(response, server.ukiniPraćenje(presentity, watcher));
			}
		});

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
		return string2xml("odgovor", string);
	}

	/**
	 * Generira XML odgovor na temelju zadanog Stringa, wrappanog u element imeElementa.
	 */
	private static String string2xml(final String imeElementa, final String string) {
		final Document doc = new Document();
		final Element root = new Element("ops");
		doc.setRootElement(root);

		final Element odgovor = new Element(imeElementa);

		odgovor.addContent(string);
		root.addContent(odgovor);
		return xmlOut.outputString(doc);
	}

	/**
	 * Generira XML odgovor na temelju zadanog Eithera.
	 */
	private static String either2xml(final Response httpResponse, final Either<String, ?> either) {
		final Document dokument = new Document();
		final Element root = new Element("ops");
		dokument.setRootElement(root);

		final Element odgovor = new Element(either.getClass().getSimpleName().toLowerCase());
		root.addContent(odgovor);

		// vrati http odgovor sa kodom greške
		if (either.isLeft()) {
			httpResponse.status(SC_BAD_REQUEST);
		}

		if (either.isRight()) {
			final Object right = either.getRight();
			if (right instanceof Collection<?>) {
				for (Object e : (Collection<?>) right) {
					dodajObjekt(odgovor, e);				}
			} else if (right instanceof Map<?, ?>) {
				for (Object e : ((Map<?, ?>) right).keySet()) {
					dodajObjekt(odgovor, e);
				}
			} else {
				odgovor.addContent(either.toString());
			}
		} else {
			odgovor.addContent(either.toString());
		}

		return xmlOut.outputString(dokument);
	}

	/**
	 * Dodaje XML elemente u zadani element, ovisno o zadanom objektu.
	 */
	private static void dodajObjekt(final Element odgovor, final Object e) {
		if (e instanceof Pracenje) {
			final Element praćenjeEl = new Element("praćenje");
			final Pracenje praćenje = (Pracenje) e;
			final Element watcher = new Element("watcher");
			watcher.addContent(praćenje.watcher);
			final Element vrstaPraćenja = new Element("vrstaPraćenja");
			vrstaPraćenja.addContent(praćenje.vrstaPraćenja.toString());

			praćenjeEl.addContent(watcher);
			praćenjeEl.addContent(vrstaPraćenja);

			odgovor.addContent(praćenjeEl);
		}
	}

}
