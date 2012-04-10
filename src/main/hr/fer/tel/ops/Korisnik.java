package hr.fer.tel.ops;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * Predstavlja korisnika sustava.
 * Lozinci se dodaje salt i hashira se prije spremanja.
 */
public class Korisnik {

	/** Korisničko ime ovog korisnika. */
	public final String korisničkoIme;

	protected final byte[] lozinka;
	protected final byte[] lozinkaSalt;

	protected static SecureRandom random = new SecureRandom();

	protected static final MessageDigest messageDigest;

	static {
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ignored) {
			// nemoguće: svaka implementacija mora imati implementirane MD5, SHA-1 i SHA-256 algoritme
			throw new AssertionError("Trenutna Java platforma ne podržava zahtjevan MessageDigest algoritam.");
		}
	}

	/**
	 * Kreira novog Korisnika sa zadanim ne-null korisničkim imenom i lozinkom.
	 */
	public Korisnik(final String korisničkoIme, final String lozinka) {
		this.korisničkoIme = Objects.requireNonNull(korisničkoIme, "Korisničko ime ne smije biti null!");

		Objects.requireNonNull(lozinka, "Lozinka ne smije biti null!");
		this.lozinkaSalt = new BigInteger(128, random).toByteArray();
		this.lozinka = hashiraj(lozinka, lozinkaSalt);
	}

	/**
	 * @return Saltana i hashirana lozinka.
	 */
	protected static synchronized byte[] hashiraj(final String lozinka, final byte[] salt) {
		messageDigest.reset();
		messageDigest.update(lozinka.getBytes());
		messageDigest.update(salt);

		return messageDigest.digest();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final Korisnik korisnik = (Korisnik) o;

		return korisničkoIme.equals(korisnik.korisničkoIme);
	}

	@Override
	public int hashCode() {
		return korisničkoIme.hashCode();
	}

	/**
	 * @return Je li zadana lozinka ispravna za ovog korisnika.
	 */
	public boolean provjeriLozinku(final String lozinka) {
		return Arrays.equals(this.lozinka, hashiraj(lozinka, this.lozinkaSalt));
	}
}
