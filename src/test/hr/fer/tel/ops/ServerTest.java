package hr.fer.tel.ops;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		assertTrue(server.getBrojKorisnika() == 0);
		assertTrue(server.zahtjevZaRegistracijom(korisničkoIme1, lozinka1));
		assertTrue(server.getBrojKorisnika() == 1);

		assertFalse(server.zahtjevZaRegistracijom(korisničkoIme1, lozinka1));
		assertTrue(server.getBrojKorisnika() == 1);

		assertTrue(server.zahtjevZaRegistracijom(korisničkoIme2, lozinka2));
		assertTrue(server.getBrojKorisnika() == 2);

		assertFalse(server.zahtjevZaRegistracijom(korisničkoIme2, lozinka2));
		assertTrue(server.getBrojKorisnika() == 2);
	}

	@Test
	public void testZahtjevZaUkidanjeRegistracije() {
		server.zahtjevZaRegistracijom(korisničkoIme1, lozinka1);
		server.zahtjevZaRegistracijom(korisničkoIme2, lozinka2);
		assertTrue(server.getBrojKorisnika() == 2);

		try {
			server.zahtjevZaUkidanjeRegistracije(korisničkoIme1, lozinka1);
		} catch (NevaljaniKorisnickiPodaciException ignored) {}

		assertTrue(server.getBrojKorisnika() == 1);

		try {
			server.zahtjevZaUkidanjeRegistracije(korisničkoIme2, lozinka2);
		} catch (NevaljaniKorisnickiPodaciException ignored) {}

		assertTrue(server.getBrojKorisnika() == 0);

	}

	@Test(expected=NevaljaniKorisnickiPodaciException.class)
	public void testZahtjevZaUkidanjeRegistracijeException() throws NevaljaniKorisnickiPodaciException {
		server.zahtjevZaRegistracijom(korisničkoIme1, lozinka1);
		server.zahtjevZaUkidanjeRegistracije(korisničkoIme1, lozinka2);
	}
}
