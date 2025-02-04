package de.mephisto.vpin.ui.util;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class WaitNProgressModel<U, T> extends ProgressModel<U> {

  private int size;
  private Iterator<U> objects;
  
  private Function<U, String> message;
  private Function<U, T> function;

  public WaitNProgressModel(String title, List<U> objects, String message, Function<U, T> function) {
    this(title, objects, u -> message, function);
  }
  public WaitNProgressModel(String title, List<U> objects, String message, Consumer<U> function) {
    this(title, objects, u -> message, u -> { function.accept(u); return null; });
  }
  public WaitNProgressModel(String title, List<U> objects, Function<U, String> message, Consumer<U> function) {
    this(title, objects, message, u -> { function.accept(u); return null; });
  }
  public WaitNProgressModel(String title, List<U> objects, Function<U, String> message, Function<U, T> function) {
    super(title);
    this.message = message;
    this.size = objects.size();
    this.objects = objects.iterator();
    this.function = function;
  }

  public boolean isShowSummary() {
    return false;
  }
  public boolean isIndeterminate() {
    return size == 1;
  }

  public int getMax() {
    return size;
  }

  public boolean hasNext() {
    return objects.hasNext();
  }

  public U getNext() {
    return objects.next();
  }

  public String nextToString(U next) {
    return message.apply(next);
  }

  public void processNext(ProgressResultModel progressResultModel, U next) throws Exception {
    T result = function.apply(next);
    progressResultModel.addProcessed(result);
  }
}
