package hr.fer.tel.ops;

import java.util.Objects;

/**
 * Praćenje nekog presentitya. Sadrži watchera koji prati presentity i vrstu praćenja.
 * Pri usporedbi, ova klasa se može smatrati wrapperom za String - ime watchera.
 * Immutable.
 */
public class Pracenje {

	public final String watcher;

	/** Ovo polje ne sudjeluje u hashCode/equals metodama. */
	public final VrstaPracenja vrstaPraćenja;

	public Pracenje(final String watcher, final VrstaPracenja vrstaPraćenja) {
		Objects.requireNonNull(watcher);
		Objects.requireNonNull(vrstaPraćenja);

		this.watcher = watcher;
		this.vrstaPraćenja = vrstaPraćenja;
	}

	/** Konstruktor koji se koristi kada nije definirana vrsta praćenja. */
	public Pracenje(final String watcher) {
		this(watcher, VrstaPracenja.NEDEFINIRANO);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final Pracenje praćenje = (Pracenje) o;

		return watcher.equals(praćenje.watcher);

	}

	@Override
	public int hashCode() {
		return watcher.hashCode();
	}

	@Override
	public String toString() {
		return "(watcher='" + watcher + "', vrstaPraćenja=" + vrstaPraćenja + ')';
	}
}
