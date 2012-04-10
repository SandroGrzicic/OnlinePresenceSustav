package net.sandrogrzicic.java.fp;

/**
 * Represents a value of one of two possible types (a disjoint union).
 */
public abstract class Either<L, R> {

	abstract boolean isLeft();
	abstract boolean isRight();

}
