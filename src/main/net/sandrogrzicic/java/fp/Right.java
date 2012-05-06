package net.sandrogrzicic.java.fp;

import java.util.Objects;

/**
 * Represents one of the possible Either values.
 */
public class Right<T> extends Either<Void, T> {

	private final T right;

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

	@Override
	public String toString() {
		return Objects.toString(right);
	}

}
