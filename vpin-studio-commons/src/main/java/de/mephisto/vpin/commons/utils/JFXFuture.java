package de.mephisto.vpin.commons.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.util.ReturnMessage;
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

  public static <U> JFXFuture<U> onErrorLater(CompletableFuture<U> future, Consumer<Throwable> fn) {
    Function<Throwable, ? extends U> f2 = (ex) -> {
      if (ex instanceof CompletionException) {
        ex = ((CompletionException) ex).getCause();
      }
      JFXRuntimeException jfxe = ex instanceof JFXRuntimeException ? 
        (JFXRuntimeException) ex :
        new JFXRuntimeException(ex.getMessage());
      Platform.runLater(() -> fn.accept(jfxe)); 
      throw jfxe; 
    };
    return new JFXFuture<U>(future.exceptionally(f2));
  }

  public JFXFuture<T> onErrorLater(Consumer<Throwable> fn) {
    return onErrorLater(res, fn);
  }


  //-----------

  public static JFXFuture<Object[]> supplyAllAsync(Supplier<?>... suppliers) {
    CompletableFuture<?>[] futures = new CompletableFuture[suppliers.length];
    for (int i = 0, n = suppliers.length; i < n; i++) {
      futures[i] = CompletableFuture.supplyAsync(suppliers[i]);
    }
    CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);

    return new JFXFuture<Object[]>(allFutures.thenApply(v -> {
      Object[] rets = new Object[suppliers.length];
      for (int i = 0, n = suppliers.length; i < n; i++) {
        rets[i] = futures[i].join();
      }
      return rets;
    }));
  }

  public static <U> JFXFuture<U> supplyAsync(Supplier<U> u) {
    return new JFXFuture<U>(CompletableFuture.supplyAsync(u));
  }

  public static <U> JFXFuture<Void> thenAcceptLater(CompletableFuture<U> future, Consumer<? super U> c) {
    return new JFXFuture<Void>(future.thenAcceptAsync(c, Platform::runLater));
  }

  public JFXFuture<Void> thenAcceptLater(Consumer<? super T> c) {
    return thenAcceptLater(res, c);
  }

  public static <U> JFXFuture<U> onErrorSupply(CompletableFuture<U> future, Function<Throwable, ? extends U> fn) {
    Function<Throwable, ? extends U> f2 = (ex) -> { return fn.apply(ex); };
    return new JFXFuture<U>(future.exceptionally(f2));
  }

  public JFXFuture<T> onErrorSupply(Function<Throwable, ? extends T> fn) {
    return onErrorSupply(res, fn);
  }

  //-------------------------------

  /**
   * Because CompletableFuture encode the exception using toString(), it adds to the message 
   * the type of teh exception, so provode our own RuntimeException that overrides toString() 
   */
  public static void throwException(String error) {
    throw new JFXRuntimeException(error);
  }
  /**
   * Throw an exception only of error message is not null and not empty
   */
  public static void throwExceptionIfError(String error) {
    if (StringUtils.isNotEmpty(error)) {
      throwException(error);
    }
  }
  /**
   * Throw an exception only of error message is not null and not empty
   */
  public static void throwExceptionIfError(ReturnMessage status) {
    if (status != null && ! status.isOk()) {
      throwException(status.getMessage());
    }
  }

  public static class JFXRuntimeException extends RuntimeException {
   
    public JFXRuntimeException(String message) {
      super(message);
    }
    public JFXRuntimeException(String message, Throwable cause) {
      super(message, cause);
    }
    public JFXRuntimeException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
      return getMessage();
    }
  }

  //-----------
  public static void main(String[] args) {
    // To try, adjust code below
    Platform.startup(() -> {

      System.out.println("start");
      JFXFuture.supplyAsync(() -> {
        System.out.println(">> throw exception");
        System.out.println("   on FXThread "+ Platform.isFxApplicationThread());
        //return "world";
        throw new RuntimeException("something wrong"); 
      })
      .thenAcceptLater(message -> {
        System.out.println(">> hello " + message);
        System.out.println("   on FXThread "+ Platform.isFxApplicationThread());
        Platform.exit();
      })
      .onErrorLater(ex -> {
        System.out.println(">> exception: "+ ex.getMessage());
        System.out.println("   on FXThread "+ Platform.isFxApplicationThread());
        Platform.exit();
      })
      ;
      System.out.println("end");
    });
  }
}
