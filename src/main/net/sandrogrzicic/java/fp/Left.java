package net.sandrogrzicic.java.fp;

import java.util.Objects;

/**
 * Represents one of the possible Either values.
 */
public class Left<T> extends Either<T, Void> {

	private final T left;

	public Left(T result) {
		left = result;
	}

	@Override
	public boolean isLeft() {
		return true;
	}

	@Override
	public boolean isRight() {
		return false;
	}

	@Override
	public T getLeft() {
		return left;
	}

	@Override
	public Void getRight() {
		throw new RuntimeException("Left doesn't have a Right!");
	}

	@Override
	public String toString() {
		return Objects.toString(left);
	}
}
