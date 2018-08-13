package bestan.concurrent.test;

import java.util.concurrent.Callable;

public abstract class AbstractTest<T> implements Callable<T> {
	@Override
	public T call() {
		System.out.println("AbstractTest");
		return null;
	}
}
