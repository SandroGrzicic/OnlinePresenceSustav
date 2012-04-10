package hr.fer.tel.ops;

import net.sandrogrzicic.java.fp.Either;
import net.sandrogrzicic.java.fp.Left;
import net.sandrogrzicic.java.fp.Right;

import java.util.*;

/**
 * Glavni poslužitelj sustava.
 */
public class Server {

	/** Registrirani korisnici. */
	protected final Map<String, Korisnik> korisnici = new HashMap<>();

	/** Registrirani watcheri svih presentitya. */
	protected final Map<String, Set<Korisnik>> watcheriPresentitya = new HashMap<>();

	/** Trenutna stanja prisutnosti svih presentitya. */
	protected final Map<String, Prisutnost> prisutnosti = new HashMap<>();

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
			final Korisnik noviKorisnik = new Korisnik(korisničkoIme, lozinka);
			korisnici.put(korisničkoIme, noviKorisnik);
			watcheriPresentitya.put(korisničkoIme, new HashSet<Korisnik>());
			prisutnosti.put(korisničkoIme, Prisutnost.SLOBODAN);

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


	public void zahtjevZaPraćenjem(final String watcher, final String presentity, final VrstaPraćenja vrstaPraćenja) {
		korisnici.get(presentity).zahtjevZaPraćenjem(new ZahtjevZaPraćenjem(this, watcher, vrstaPraćenja));
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

	public void odgovorNaZahtjevZaPraćenjem(final String presentityIme, final String watcherIme, final boolean odgovor) {
		final Korisnik watcher = korisnici.get(watcherIme);

		if (odgovor) {
			watcheriPresentitya.get(presentityIme).add(watcher);
			watcher.odgovorenoNaZahtjevZaPraćenjem(presentityIme, true);
			watcher.promjenaPrisutnosti(presentityIme, prisutnosti.get(presentityIme));
		} else {
			watcher.odgovorenoNaZahtjevZaPraćenjem(presentityIme, false);
		}

	}
}
