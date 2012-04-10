package hr.fer.tel.ops;

import net.sandrogrzicic.java.fp.Either;
import net.sandrogrzicic.java.fp.Left;
import net.sandrogrzicic.java.fp.Right;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Glavni poslužitelj sustava.
 */
public class Server {

	protected final Map<String, Korisnik> korisnici;

	public Server() {
		korisnici = new HashMap<>();
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
			korisnici.put(korisničkoIme, new Korisnik(korisničkoIme, lozinka));
			return new Right("Registracija uspjela.");
		} else {
			return new Left("Korisnik već postoji!");
		}
	}



	/**
	 * Zahtjev za ukidanjem registracije korisnika sa zadanim podacima.
	 * @throws NevaljaniKorisnickiPodaciException
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

}
