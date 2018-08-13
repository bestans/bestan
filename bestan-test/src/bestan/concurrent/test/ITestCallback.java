package bestan.concurrent.test;

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.FutureCallback;

public interface ITestCallback<T> extends Callable<T>, FutureCallback<T>{

	default T call() {
		System.out.println("ITestCallback");
		return null;
	}
}
