package hr.fer.tel.ops;

import net.sandrogrzicic.java.fp.*;

import java.util.*;

/**
 * Glavni poslužitelj sustava.
 */
public class Server {

	/** Registrirani korisnici. */
	protected final Map<String, Korisnik> korisnici = new HashMap<>();

	public Server() {
	}

	/**
	 * Zahtjev za registracijom korisnika sa zadanim podacima.
	 * @return Right ako je registracija uspjela; Left ako korisnik sa zadanim imenom već postoji.
	 */
	@SuppressWarnings("unchecked")
	public Either<String, String> zahtjevZaRegistracijom(final String korisničkoIme, final String lozinka) {
		Objects.requireNonNull(korisničkoIme, "Korisničko ime ne smije biti null!");
		Objects.requireNonNull(lozinka, "Lozinka ne smije biti null!");

		if (!provjeriSloženostLozinke(lozinka)) {
			return new Left("Lozinka nije dovoljno složena!");
		}

		if (!korisnici.containsKey(korisničkoIme)) {
			final Korisnik noviKorisnik = new Korisnik(this, korisničkoIme, lozinka);
			korisnici.put(korisničkoIme, noviKorisnik);
			return new Right("Registracija uspjela.");
		} else {
			return new Left("Korisnik već postoji!");
		}
	}


	/**
	 * Zahtjev za ukidanjem registracije korisnika sa zadanim podacima.
	 * @return Right ako je ukidanje uspjelo; Left ako podaci nisu ispravni.
	 */
	@SuppressWarnings("unchecked")
	public Either<String, String> zahtjevZaUkidanjeRegistracije(final String korisničkoIme, final String lozinka) {
		Objects.requireNonNull(korisničkoIme, "Korisničko ime ne smije biti null!");
		Objects.requireNonNull(lozinka, "Lozinka ne smije biti null!");

		if (provjeriLogin(korisničkoIme, lozinka)) {
			korisnici.remove(korisničkoIme);
			return new Right("Registracija uspješno ukinuta.");
		} else {
			return new Left("Korisnik sa zadanim podacima ne postoji!");
		}
	}

	/**
	 * Šalje traženom presentityu zahtjev za praćenjem koji potječe od zadanog watchera.
	 */
	public Either<String, String> zahtjevZaPraćenjem(final String watcher, final VrstaPracenja vrstaPraćenja, final String presentity) {
		return zahtjevZaPraćenjem(new Pracenje(watcher, vrstaPraćenja), presentity);
	}

	/**
	 * Šalje traženom presentityu zahtjev za praćenjem koji potječe od zadanog watchera.
	 */
	@SuppressWarnings("unchecked")
	public Either<String, String> zahtjevZaPraćenjem(final Pracenje praćenje, final String presentityIme) {
		final Korisnik presentity = korisnici.get(presentityIme);
		if (presentity == null) {
			return new Left("Zadani presentity ne postoji!");
		} else {
			presentity.zahtjevZaPraćenjem(praćenje);
			return new Right("Zahtjev za praćenjem uspješno dodan.");
		}
	}

	/** @return Trenutni zahtjevi za praćenjem zadanog presentitya. */
	@SuppressWarnings("unchecked")
	public Either<String, Set<Pracenje>> zahtjeviZaPraćenjem(final String presentityIme) {
		final Korisnik presentity = korisnici.get(presentityIme);
		if (presentity == null) {
			return new Left("Zadani presentity ne postoji!");
		} else {
			return new Right(Collections.unmodifiableSet(presentity.zahtjeviZaPraćenjem));
		}
	}

	/** Čisti zahtjeve za praćenjem zadanog presentitya. */
	public void očistiZahtjeveZaPraćenjem(final String presentity) {
		korisnici.get(presentity).zahtjeviZaPraćenjem.clear();
	}


	/** @see #odgovorNaZahtjevZaPraćenjem(String, Pracenje, boolean) */
	public void odgovorNaZahtjevZaPraćenjem(final String presentity, final String watcher, final VrstaPracenja vrstaPraćenja, final boolean odgovor) {
		odgovorNaZahtjevZaPraćenjem(presentity, new Pracenje(watcher, vrstaPraćenja), odgovor);
	}

	/** Šalje zadanom watcheru odgovor na zahtjev za praćenjem traženog presentitya i uklanja zahtjev iz popisa. */
	@SuppressWarnings("unchecked")
	public Either<String, String> odgovorNaZahtjevZaPraćenjem(final String presentityIme, final Pracenje zahtjev, final boolean odgovor) {
		final Korisnik watcher = korisnici.get(zahtjev.watcher);
		final Korisnik presentity = korisnici.get(presentityIme);

		if (watcher == null) {
			return new Left("Zadani watcher ne postoji!");
		}
		if (presentity == null) {
			return new Left("Zadani presentity ne postoji!");
		}
		presentity.zahtjeviZaPraćenjem.remove(zahtjev);

		if (odgovor) {
			presentity.watcheri.add(zahtjev);
			if (zahtjev.vrstaPraćenja == VrstaPracenja.AKTIVNO) {
				watcher.promjenaPrisutnosti(presentityIme, presentity.getPrisutnost());
			}
			return new Right("Pozitivan odgovor na zahtjev uspješno poslan.");
		} else {
			return new Right("Negativan odgovor na zahtjev uspješno poslan.");
		}
	}


	public int getBrojKorisnika() {
		return korisnici.size();
	}

	/**
	 * @return Složenost zadane lozinke.
	 */
	protected static boolean provjeriSloženostLozinke(final String lozinka) {
		if (lozinka.length() < 6) {
			return false;
		}
		return true;
	}

	/**
	 * Javlja svim registriranim aktivnim watcherima novu prisutnost zadanog presentitya.
	 */
	@SuppressWarnings("unchecked")
	public Either<String, String> promjenaPrisutnosti(final String presentityIme, final Prisutnost prisutnost) {
		final Korisnik presentity = korisnici.get(presentityIme);

		if (presentity == null) {
			return new Left("Zadani presentity ne postoji!");
		}

		presentity.setPrisutnost(prisutnost);

		for (final Pracenje praćenje: presentity.watcheri) {
			if (praćenje.vrstaPraćenja == VrstaPracenja.AKTIVNO) {
				korisnici.get(praćenje.watcher).promjenaPrisutnosti(presentityIme, prisutnost);
			}
		}
		return new Right("Prisutnost uspješno promijenjena.");
	}

	/** @see #dohvatiPrisutnost(Pracenje, String) */
	@SuppressWarnings("unchecked")
	public Either<String, Prisutnost> dohvatiPrisutnost(final String watcher, final String presentity) {
		return dohvatiPrisutnost(new Pracenje(watcher), presentity);
	}

	/**
	 * Dohvaća prisutnost zadanog presentitya.
	 * @return Prisutnost wrappana u Right ako watcher ima dozvolu, Left ako nema.
	 */
	@SuppressWarnings("unchecked")
	public Either<String, Prisutnost> dohvatiPrisutnost(final Pracenje pracenje, final String presentityIme) {
		final Korisnik presentity = korisnici.get(presentityIme);
		if (presentity == null) {
			return new Left("Zadani presentity ne postoji!");
		}

		if (presentity.watcheri.contains(pracenje)) {
			return new Right(presentity.getPrisutnost());
		} else {
			return new Left("Watcher nema dozvolu za dohvat prisutnosti zadanog entitya!");
		}
	}

	/** @see #ukiniPraćenje(String, Pracenje)  */
	public Either<String, String> ukiniPraćenje(final String presentity, final String watcher) {
		return ukiniPraćenje(presentity, new Pracenje(watcher));
	}

	/** Uklanja zadanog watchera sa liste watchera zadanog presentitya. */
	@SuppressWarnings("unchecked")
	public Either<String, String> ukiniPraćenje(final String presentityIme, final Pracenje praćenje) {
		final Korisnik presentity = korisnici.get(presentityIme);
		if (presentity == null) {
			return new Left("Zadani presentity ne postoji!");
		}

		presentity.watcheri.remove(praćenje);
		return new Right("Praćenje uspješno ukinuto.");
	}


	/** @return Jesu li korisnički podaci ispravni. */
	public boolean provjeriLogin(final String korisničkoIme, final String lozinka) {
		final Korisnik korisnik = korisnici.get(korisničkoIme);
		return korisnik != null && korisnik.provjeriLozinku(lozinka);
	}

	public int dohvatiBrojZahtjevaZaPraćenjem(final String presentity) {
		return korisnici.get(presentity).dohvatiBrojZahtjevaZaPraćenjem();

	}

}
