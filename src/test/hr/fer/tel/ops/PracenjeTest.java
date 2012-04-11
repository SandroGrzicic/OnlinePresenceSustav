package hr.fer.tel.ops;

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

	@Test
	public void testAktivnoPraćenje() throws Exception {
		assert(server.korisnici.get(korisnik1).dohvatiPrisutnostZa(korisnik2).isLeft());

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

		server.promjenaPrisutnosti(korisnik2, Prisutnost.NEDOSTUPAN);
		assert(
			server.korisnici.get(korisnik1).dohvatiPrisutnostZa(korisnik2).getRight() ==
				Prisutnost.ZAUZET
		);
	}

	@Test
	public void testPasivnoPraćenje() throws Exception {
		assert(server.korisnici.get(korisnik1).dohvatiPrisutnostZa(korisnik2).isLeft());

		final Pracenje praćenje = new Pracenje(korisnik1, VrstaPracenja.PASIVNO);
		server.zahtjevZaPraćenjem(praćenje, korisnik2);
		server.odgovorNaZahtjevZaPraćenjem(korisnik2, praćenje, true);
		assert(server.korisnici.get(korisnik1).dohvatiPrisutnostZa(korisnik2).isLeft());

		server.promjenaPrisutnosti(korisnik2, Prisutnost.NEDOSTUPAN);
		assert(server.dohvatiPrisutnost(praćenje, korisnik2).getRight() == Prisutnost.NEDOSTUPAN);
		assert(server.korisnici.get(korisnik1).dohvatiPrisutnostZa(korisnik2).isLeft());

		server.ukiniPraćenje(korisnik2, praćenje);
		assert(server.dohvatiPrisutnost(praćenje, korisnik2).isLeft());
	}

}
