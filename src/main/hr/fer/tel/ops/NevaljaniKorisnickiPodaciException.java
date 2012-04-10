package hr.fer.tel.ops;

/**
 * Bačen u slučaju nevaljanih zadanih korisničkih podataka.
 */
public class NevaljaniKorisnickiPodaciException extends Exception {

	public NevaljaniKorisnickiPodaciException(final String poruka) {
		super(poruka);
	}
}
