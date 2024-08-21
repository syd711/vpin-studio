package de.mephisto.vpin.ui.tables.panels;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.property.ObjectPropertyBase;
import javafx.concurrent.Task;

public abstract class BaseLoadingModel<T extends BaseLoadingModel<?>> extends ObjectPropertyBase<T> {

  /**
   * Background loader
   */
  private static final Executor Executor = Executors.newFixedThreadPool(10, runnable -> {
      Thread t = new Thread(runnable);
      t.setDaemon(true);
      return t ;
  });

  private boolean loadRequested;
  public boolean loaded;
    
  @SuppressWarnings("unchecked")
  protected BaseLoadingModel() {
    set((T) this);
  }

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

  public abstract void loaded();


  @Override
  public T getBean() {
    return null;
  }

    /**
     * Whether or not the value has been loaded.
     */
    public final boolean isLoaded() {
        if (!loadRequested) {
        loadRequested = true ;
        Executor.execute(new LoadingTask<>(this));
        }
        return loaded;
    }

  private static class LoadingTask<T extends BaseLoadingModel<?>> extends Task<Boolean> {

    private T model;

    LoadingTask(T model) {
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
