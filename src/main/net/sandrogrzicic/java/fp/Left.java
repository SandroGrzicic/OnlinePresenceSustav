package net.sandrogrzicic.java.fp;

/**
 * Represent one of the possible Either values.
 */
public class Left<T> extends Either<T, Void> {

	protected final T left;

	public Left() {
		left = null;
	}

	public Left(T result) {
		left = result;
	}

	@Override
	boolean isLeft() {
		return true;
	}

	@Override
	boolean isRight() {
		return false;
	}
}
