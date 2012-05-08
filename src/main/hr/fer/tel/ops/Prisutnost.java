package hr.fer.tel.ops;

/**
 * Moguća stanja prisutnosti presentitya.
 */
public enum Prisutnost {
	SLOBODAN,
	NEDOSTUPAN,
	ZAUZET;

	/**
	 * Vraća odgovarajuću Prisutnost, ovisno o zadanom charu.
	 */
	public static Prisutnost char2prisutnost(char vrsta) {
		if (vrsta == 's' || vrsta == 'a' || vrsta == 'f') {
			return Prisutnost.SLOBODAN;
		} else if (vrsta == 'n' || vrsta == 'u') {
			return Prisutnost.NEDOSTUPAN;
		} else if (vrsta == 'z' || vrsta == 'b') {
			return Prisutnost.ZAUZET;
		} else {
			throw new IllegalArgumentException("Nevaljana prisutnost!");
		}
	}


}
