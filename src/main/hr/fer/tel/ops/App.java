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

	private static final XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());

	private static String podržaneRute;

	/** Pokreće web aplikaciju. */
	public static void main(String[] args) {
		final Server s = new Server();


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

		podržaneRute = dohvatiRute();

		// ispisuje sve podržane rute koristeći Reflection API.
		get(new Route("/") {
			@Override
			public Object handle(final Request request, final Response response) {
				return podržaneRute;
			}
		});
	}


	/** Vraća sve podržane rute koristeći Reflection. */
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
			for (final Object route: routes) {
				sb.append(route.toString().split(", ")[1]).append("\n");
			}
			return sb.toString();
		} catch (Exception ignored)  {
			return ignored.getMessage();
		}
	}

	/** Generira XML odgovor na temelju zadanog Stringa. */
	private static String string2xml(final String string) {
		final Document doc = new Document();
		final Element root = new Element("ops");
		doc.setRootElement(root);

		final Element odgovorEl = new Element("odgovor");

		odgovorEl.addContent(string);
		root.addContent(odgovorEl);
		return xmlOut.outputString(doc);
	}

	/** Generira XML odgovor na temelju zadanog Eithera. */
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


	private static String xml(String name, Object value) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + name +">" + value + "</"+ name + ">";
	}
}
