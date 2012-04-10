package hr.fer.tel.ops;

/**
 * Bačen u slučaju nevaljanih zadanih korisničkih podataka.
 */
public class NevaljaniKorisničkiPodaciException extends Exception {

	public NevaljaniKorisničkiPodaciException(final String poruka) {
		super(poruka);
	}
}
