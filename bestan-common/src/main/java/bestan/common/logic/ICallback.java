package bestan.common.logic;

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.FutureCallback;

public interface ICallback<T> extends Callable<T>, FutureCallback<T> {

}
