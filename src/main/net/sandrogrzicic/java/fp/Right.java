package net.sandrogrzicic.java.fp;

/**
 * Represent one of the possible Either values.
 */
public class Right<T> extends Either<Void, T> {

	protected final T right;

	public Right() {
		right = null;
	}

	public Right(T result) {
		right = result;
	}

	@Override
	boolean isLeft() {
		return false;
	}

	@Override
	boolean isRight() {
		return true;
	}
}
