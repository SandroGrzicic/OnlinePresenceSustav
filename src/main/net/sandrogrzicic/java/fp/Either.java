package net.sandrogrzicic.java.fp;

/**
 * Represents a value of one of two possible types (a disjoint union).
 */
public abstract class Either<L, R> {

	public abstract boolean isLeft();
	public abstract boolean isRight();

	public abstract L getLeft();
	public abstract R getRight();
}

