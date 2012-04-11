package hr.fer.tel.ops;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Testira aktivno praćenje.
 */
public class AktivnoPracenje {

	Server server;

	final String korisničkoIme1 = "satcom";
	final String korisničkoIme2 = "nsf001";
	final String lozinka1 = "unatco_001";
	final String lozinka2 = "smashthestate";

	@Before
	public void setUp() throws Exception {
		server = new Server();
		server.zahtjevZaRegistracijom(korisničkoIme1, lozinka1);
		server.zahtjevZaRegistracijom(korisničkoIme2, lozinka2);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {

	}

}
