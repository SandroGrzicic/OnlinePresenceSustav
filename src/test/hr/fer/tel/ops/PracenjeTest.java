package hr.fer.tel.ops;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Testira aktivno praćenje.
 */
public class PracenjeTest {

	Server server;

	final String korisnik1 = "satcom";
	final String korisnik2 = "nsf001";
	final String lozinka1 = "unatco_001";
	final String lozinka2 = "smashthestate";

	@Before
	public void setUp() throws Exception {
		server = new Server();
		server.zahtjevZaRegistracijom(korisnik1, lozinka1);
		server.zahtjevZaRegistracijom(korisnik2, lozinka2);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAktivnoPraćenje() throws Exception {
		final Pracenje praćenje = new Pracenje(korisnik1, VrstaPracenja.AKTIVNO);
		server.zahtjevZaPraćenjem(praćenje, korisnik2);
		server.odgovorNaZahtjevZaPraćenjem(korisnik2, praćenje, true);
		assert(
			server.korisnici.get(korisnik1).dohvatiPrisutnostZa(korisnik2).getRight() ==
				Prisutnost.SLOBODAN
		);

		server.promjenaPrisutnosti(korisnik2, Prisutnost.ZAUZET);
		assert(
			server.korisnici.get(korisnik1).dohvatiPrisutnostZa(korisnik2).getRight() ==
				Prisutnost.ZAUZET
		);

		server.ukiniPraćenje(korisnik2, praćenje);

		server.promjenaPrisutnosti(korisnik1, Prisutnost.NEDOSTUPAN);
		assert(
			server.korisnici.get(korisnik1).dohvatiPrisutnostZa(korisnik2).getRight() ==
				Prisutnost.ZAUZET
		);

	}

}
