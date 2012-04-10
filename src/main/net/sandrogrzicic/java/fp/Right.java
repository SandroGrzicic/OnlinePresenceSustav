package net.sandrogrzicic.java.fp;

/**
 * Represent one of the possible Either values.
 */
public class Right<T> extends Either<Void, T> {

	public final T right;

	public Right(T result) {
		right = result;
	}

	@Override
	public boolean isLeft() {
		return false;
	}

	@Override
	public boolean isRight() {
		return true;
	}

	@Override
	public Void getLeft() {
		throw new RuntimeException("Right doesn't have a Left!");
	}

	@Override
	public T getRight() {
		return right;
	}

}
