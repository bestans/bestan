package bestan.common.concurrent;

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.ListenableFuture;

public interface BCallback<T> extends ListenableFuture<T>, Callable<T> {

}
