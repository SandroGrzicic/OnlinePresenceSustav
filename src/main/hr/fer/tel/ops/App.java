package hr.fer.tel.ops;

import net.sandrogrzicic.java.fp.Either;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import spark.Request;
import spark.Response;
import spark.Route;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static spark.Spark.get;

/**
 * Entry point za web aplikaciju.
 */
public class App {

	private static final XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());

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

				return either2response(response, odgovor, SC_BAD_REQUEST);
			}
		});

		// zahtjev za ukidanjem registracije
		get(new Route("/unreg/:login/:pass") {
			@Override
			public Object handle(Request request, Response response) {
				String korisničkoIme = request.params(":login");
				String lozinka = request.params(":pass");

				Either<String, String> odgovor = s.zahtjevZaUkidanjeRegistracije(korisničkoIme, lozinka);

				return either2response(response, odgovor, SC_BAD_REQUEST);
			}
		});

	}

	/** Generira String odgovor na temelju zadanog Eithera. */
	private static String either2response(final Response httpResponse, final Either<String, String> odgovor, final int greškaKod) {
		final Document doc = new Document();
		final Element root = new Element("ops");
		doc.setRootElement(root);

		final Element odgovorEl = new Element(odgovor.getClass().getSimpleName().toLowerCase());

		if (odgovor.isLeft()) {
			httpResponse.status(greškaKod);
		}

		odgovorEl.addContent(odgovor.toString());
		root.addContent(odgovorEl);
		return xmlOut.outputString(doc);
	}


	private static String xml(String name, Object value) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + name +">" + value + "</"+ name + ">";
	}
}
