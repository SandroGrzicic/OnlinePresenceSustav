package hr.fer.tel.ops;

import net.sandrogrzicic.java.fp.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Predstavlja korisnika sustava.
 * Lozinci se dodaje salt i hashira se prije spremanja.
 */
public class Korisnik {

	protected final Server server;

	/** Korisničko ime ovog korisnika. */
	public final String korisničkoIme;

	protected final byte[] lozinka;
	protected final byte[] lozinkaSalt;

	protected Prisutnost prisutnost = Prisutnost.SLOBODAN;

	/** Trenutni neodgovoreni zahtjevi za praćenjem od strane watchera za ovog presentitya. */
	protected final Set<Pracenje> zahtjeviZaPraćenjem = new HashSet<>();

	/** Trenutna stanja prisutnosti presentitya koje prati ovaj watcher. */
	protected final Map<String, Prisutnost> prisutnosti = new HashMap<>();

	protected static final SecureRandom random = new SecureRandom();

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
	public Korisnik(final Server server, final String korisničkoIme, final String lozinka) {
		this.server = server;
		this.korisničkoIme = Objects.requireNonNull(korisničkoIme, "Korisničko ime ne smije biti null!");

		Objects.requireNonNull(lozinka, "Lozinka ne smije biti null!");
		this.lozinkaSalt = new BigInteger(128, random).toByteArray();
		this.lozinka = hashiraj(lozinka, lozinkaSalt);
	}

	public Prisutnost getPrisutnost() {
		return prisutnost;
	}

	/** Postavlja trenutnu prisutnost ovog presentitya. */
	public void setPrisutnost(final Prisutnost prisutnost) {
		this.prisutnost = prisutnost;
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

	/**
	 * Prima zahtjev za praćenjem od strane potencijalnog watchera koji želi pratiti ovaj presentity.
	 */
	public void zahtjevZaPraćenjem(final Pracenje zahtjev) {
		zahtjeviZaPraćenjem.add(zahtjev);
	}

	/**
	 * Dojavljuje ovom watcheru da je zadani presentity promijenio stanje prisutnosti.
	 */
	public void promjenaPrisutnosti(final String presentity, final Prisutnost prisutnost) {
		prisutnosti.put(presentity, prisutnost);
	}

	/** @return trenutna prisutnost zadanog watchanog presentitya wrappana u Right, inače vraća Left. */
	@SuppressWarnings("unchecked")
	public Either<String, Prisutnost> dohvatiPrisutnostZa(final String presentity) {
		if (prisutnosti.containsKey(presentity)) {
			return new Right(prisutnosti.get(presentity));
		} else {
			return new Left("Zadani presentity nije aktivno praćen!");
		}

	}
}
