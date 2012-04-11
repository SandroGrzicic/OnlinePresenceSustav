package hr.fer.tel.ops;

import net.sandrogrzicic.java.fp.*;

import java.util.*;

/**
 * Glavni poslužitelj sustava.
 */
public class Server {

	/** Registrirani korisnici. */
	protected final Map<String, Korisnik> korisnici = new HashMap<>();

	/** Registrirani watcheri svih presentitya. */
	protected final Map<String, Set<Pracenje>> watcheriPresentitya = new HashMap<>();

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
			watcheriPresentitya.put(korisničkoIme, new HashSet<Pracenje>());

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

		final Korisnik korisnik = korisnici.get(korisničkoIme);
		if (korisnik != null) {
			if (korisnik.provjeriLozinku(lozinka)) {
				korisnici.remove(korisničkoIme);
				return new Right("Registracija uspješno ukinuta.");
			}
		}
		return new Left("Korisnik sa zadanim podacima ne postoji!");
	}

	/**
	 * Šalje traženom presentityu zahtjev za praćenjem koji potječe od zadanog watchera.
	 */
	public void zahtjevZaPraćenjem(final Pracenje praćenje, final String presentity) {
		korisnici.get(presentity).zahtjevZaPraćenjem(praćenje);
	}

	/**
	 * Šalje traženom presentityu zahtjev za praćenjem koji potječe od zadanog watchera.
	 */
	public void zahtjevZaPraćenjem(final String watcher, final VrstaPracenja vrstaPraćenja, final String presentity) {
		zahtjevZaPraćenjem(new Pracenje(watcher, vrstaPraćenja), presentity);
	}

	/** Šalje zadanom watcheru odgovor na zahtjev za praćenjem traženog presentitya. */
	public void odgovorNaZahtjevZaPraćenjem(final String presentity, final String watcher, final VrstaPracenja vrstaPraćenja, final boolean odgovor) {
		odgovorNaZahtjevZaPraćenjem(presentity, new Pracenje(watcher, vrstaPraćenja), odgovor);
	}

	/** Šalje zadanom watcheru odgovor na zahtjev za praćenjem traženog presentitya. */
	public void odgovorNaZahtjevZaPraćenjem(final String presentity, final Pracenje zahtjev, final boolean odgovor) {
		final Korisnik watcher = korisnici.get(zahtjev.watcher);

		if (odgovor) {
			watcheriPresentitya.get(presentity).add(zahtjev);
			watcher.promjenaPrisutnosti(presentity, korisnici.get(presentity).getPrisutnost());
		} else {
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
	public void promjenaPrisutnosti(final String presentity, final Prisutnost prisutnost) {
		korisnici.get(presentity).prisutnost = prisutnost;

		for (final Pracenje praćenje: watcheriPresentitya.get(presentity)) {
			if (praćenje.vrstaPraćenja == VrstaPracenja.AKTIVNO) {
				korisnici.get(praćenje.watcher).promjenaPrisutnosti(presentity, prisutnost);
			}
		}
	}

	/**
	 * Dohvaća prisutnost zadanog presentitya.
	 * @return Prisutnost wrappana u Right ako watcher ima dozvolu, Left ako nema.
	 */
	@SuppressWarnings("unchecked")
	public Either<String, Prisutnost> dohvatiPrisutnost(final String watcher, final String presentity) {
		return dohvatiPrisutnost(new Pracenje(watcher), presentity);
	}

	/**
	 * Dohvaća prisutnost zadanog presentitya.
	 * @return Prisutnost wrappana u Right ako watcher ima dozvolu, Left ako nema.
	 */
	@SuppressWarnings("unchecked")
	public Either<String, Prisutnost> dohvatiPrisutnost(final Pracenje pracenje, final String presentity) {
		if (watcheriPresentitya.get(presentity).contains(pracenje)) {
			return new Right(korisnici.get(presentity).getPrisutnost());
		} else {
			return new Left("Watcher nema dozvolu za dohvat prisutnosti zadanog entitya!");
		}
	}

	/** Uklanja zadanog watchera sa liste watchera zadanog presentitya. */
	public void ukiniPraćenje(final String presentity, final Pracenje praćenje) {
		watcheriPresentitya.get(presentity).remove(praćenje);
	}

	/** Uklanja zadanog watchera sa liste watchera zadanog presentitya. */
	public void ukiniPraćenje(final String presentity, final String watcher) {
		ukiniPraćenje(presentity, new Pracenje(watcher));
	}

}
