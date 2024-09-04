package de.mephisto.vpin.ui.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.application.Platform;

/**
 * A wrapper arround CompletableFuture to simplify async call in JFX context
 * instead of :  new Thread(() -> { code;  Platform.runLater(() -> { onJFXThread; }); }).start();
 * same is : JFXFuture.runAsync(() -> { code; }).thenLater(() -> { onJFXThread; });
 * 
 * Leverage behind CompletableFuture architecture :
 * CompletableFuture<Void> f = ...
 * JFXFuture.thenLater(f, () -> { onJFXThread; });
 * 
 * Exception management :
 * JFXFuture.runAsync().thenLater().onErrorLater(ex -> deal with exception on JavaFX thread );
 * OR
 * CompletableFuture<Void> f = ...
 * JFXFuture.onErrorLater(f, ex -> deal with exception on JavaFX thread );
 */
public class JFXFuture<T> {

  protected CompletableFuture<T> res;

  public JFXFuture(CompletableFuture<T> res) {
    this.res = res;
  }

  public CompletableFuture<T> get() {
    return res;
  }


  //-----------

  public static JFXFuture<Void> runAsync(Runnable f) {
    return new JFXFuture<Void>(CompletableFuture.runAsync(f));
  }

  public static JFXFuture<Void> thenLater(CompletableFuture<?> future, Runnable f) {
    return new JFXFuture<Void>(future.thenRunAsync(f, Platform::runLater));
  }

  public JFXFuture<Void> thenLater(Runnable f) {
    return thenLater(res, f);
  }

  //-----------

  public static <U> JFXFuture<U> supplyAsync(Supplier<U> u) {
    return new JFXFuture<U>(CompletableFuture.supplyAsync(u));
  }

  public static <U> JFXFuture<Void> thenAcceptLater(CompletableFuture<U> future, Consumer<? super U> c) {
    return new JFXFuture<Void>(future.thenAcceptAsync(c, Platform::runLater));
  }

  public JFXFuture<Void> thenAcceptLater(Consumer<? super T> c) {
    return thenAcceptLater(res, c);
  }
    
  //-----------

  public static <U> JFXFuture<U> onErrorLater(CompletableFuture<U> future, Function<Throwable, ? extends U> fn) {
    Function<Throwable, ? extends U> f2 = (ex) -> { Platform.runLater(() -> fn.apply(ex)); return null; };
    return new JFXFuture<U>(future.exceptionally(f2));
  }

  public JFXFuture<T> onErrorLater(Function<Throwable, ? extends T> fn) {
    return onErrorLater(res, fn);
  }

}
