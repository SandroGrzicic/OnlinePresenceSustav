package hr.fer.tel.ops;

/**
 * Vrsta praćenja presentitya.
 */
public enum VrstaPracenja {
	AKTIVNO,
	PASIVNO,
	NEDEFINIRANO;

	/** Vraća VrstuPraćenja, ovisno o zadanom charu. */
	public static VrstaPracenja char2vrsta(char vrsta) {
		if (vrsta == 'a') {
			return VrstaPracenja.AKTIVNO;
		} else if (vrsta == 'p') {
			return VrstaPracenja.PASIVNO;
		} else {
			return VrstaPracenja.NEDEFINIRANO;
		}
	}
}
