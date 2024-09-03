package de.mephisto.vpin.ui.tables.panels;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.mephisto.vpin.ui.util.JFXFuture;
import javafx.application.Platform;
import javafx.beans.property.ObjectPropertyBase;
import javafx.concurrent.Task;

public abstract class BaseLoadingModel<T, M> extends ObjectPropertyBase<M> {

  protected T bean;

  /**
   * Background loader
   */
  private static final ExecutorService executor = Executors.newFixedThreadPool(10);

  protected boolean loadRequested;
  public boolean loaded;
    
  @SuppressWarnings("unchecked")
  protected BaseLoadingModel(T object) {
    set((M) this);
    this.bean = object;
  }

  public T getBean() {
    return bean;
  }

  public void setBean(T object) {
    this.bean = object;
    fireValueChangedEvent();
  }

  public abstract boolean sameBean(T object);


  public void reload() {
    loadRequested = false;
    loaded = false;
    // force reload process
    isLoaded();
  }

  /** Invoked from Executor thread */
  protected void doLoad() throws Exception {
    load();

    // update the table
    loaded = true;
    Platform.runLater(() -> {
      fireValueChangedEvent();
      loaded();
    });
  }    

  /** Invoked from Executor thread */
  protected void loadFailed() {
    loaded = true;
    Platform.runLater(() -> {
      fireValueChangedEvent();
    });
  }

  public abstract void load();

  public void loaded() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    @SuppressWarnings("rawtypes")
    Class<? extends BaseLoadingModel> clazz = this.getClass();
    return Objects.equals(this.bean, clazz.cast(o).bean);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(bean);
  }

  @Override
  public String toString() {
    return getClass().getName() + " \"" + getName() + "\"";
  }

  /**
   * Whether or not the value has been loaded.
   */
  public final boolean isLoaded() {
    if (!loadRequested) {
      loadRequested = true ;
      executor.execute(new LoadingTask<>(this));
    }
    return loaded;
  }

  public static <M extends BaseLoadingModel<?, ?>> void loadAllThenLater(List<M> models, Runnable callback) {
    JFXFuture.runAsync(() -> {
      try {
        boolean allLoaded = false;
        while (!allLoaded) {
          allLoaded = true;
          for (M model : models) {
            allLoaded &= model.isLoaded();
          }
          // wait a bit
          Thread.sleep(1000);
        }
      }
      catch (InterruptedException ie)  {
      }
    }).thenLater(callback);
  }

  private static class LoadingTask<M extends BaseLoadingModel<?, ?>> extends Task<Boolean> {

    private M model;

    LoadingTask(M model) {
      this.model = model;
    }

    @Override
    protected Boolean call() throws Exception {
      model.doLoad();
      return true;
    }

    @Override
    protected void failed() {
      model.loadFailed();
    }

  }

}
