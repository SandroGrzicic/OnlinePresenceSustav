package hr.fer.tel.ops;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test za poslužitelj.
 */
public class ServerTest {

	Server server;

	final String korisničkoIme1 = "satcom";
	final String korisničkoIme2 = "nsf001";
	final String lozinka1 = "unatco_001";
	final String lozinka2 = "smashthestate";

	@Before
	public void setUp() throws Exception {
		server = new Server();
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testZahtjevZaRegistracijom() throws Exception {
		assert(server.getBrojKorisnika() == 0);
		assert(server.zahtjevZaRegistracijom(korisničkoIme1, lozinka1).isRight());
		assert(server.getBrojKorisnika() == 1);

		assert(server.zahtjevZaRegistracijom(korisničkoIme1, lozinka1).isLeft());
		assert(server.getBrojKorisnika() == 1);

		assert(server.zahtjevZaRegistracijom(korisničkoIme2, lozinka2).isRight());
		assert(server.getBrojKorisnika() == 2);

		assert(server.zahtjevZaRegistracijom(korisničkoIme2, lozinka2).isLeft());
		assert(server.getBrojKorisnika() == 2);
	}



	@Test
	public void testZahtjevZaUkidanjeRegistracije() {
		server.zahtjevZaRegistracijom(korisničkoIme1, lozinka1);
		server.zahtjevZaRegistracijom(korisničkoIme2, lozinka2);
		assert(server.getBrojKorisnika() == 2);

		assert(server.zahtjevZaUkidanjeRegistracije(korisničkoIme1, lozinka1).isRight());
		assert(server.getBrojKorisnika() == 1);

		assert(server.zahtjevZaUkidanjeRegistracije(korisničkoIme2, lozinka1).isLeft());
		assert(server.getBrojKorisnika() == 1);

		assert(server.zahtjevZaUkidanjeRegistracije(korisničkoIme2, lozinka2).isRight());
		assert(server.getBrojKorisnika() == 0);

	}

}
