package de.mephisto.vpin.ui.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
public class JFXFuture {

  protected CompletableFuture<Void> res;

  public JFXFuture(CompletableFuture<Void> res) {
    this.res = res;
  }

  public static JFXFuture runAsync(Runnable f) {
    return new JFXFuture(CompletableFuture.runAsync(f));
  }

  public static JFXFuture thenLater(CompletableFuture<Void> future, Runnable f) {
    return new JFXFuture(future.thenRun(() -> Platform.runLater(f)));
  }
  public static JFXFuture onErrorLater(CompletableFuture<Void> future, Function<Throwable, Void> fn) {
    Function<Throwable, Void> f2 = (ex) -> { Platform.runLater(() -> fn.apply(ex)); return null; };
    return new JFXFuture(future.exceptionally(f2));
  }

  public JFXFuture thenLater(Runnable f) {
    return thenLater(res, f);
  }

  public JFXFuture onErrorLater(Function<Throwable, Void> fn) {
    return onErrorLater(res, fn);
  }

  public CompletableFuture<Void> get() {
    return res;
  }
}
