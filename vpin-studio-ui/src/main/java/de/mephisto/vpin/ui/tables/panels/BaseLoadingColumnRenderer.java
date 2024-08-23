package de.mephisto.vpin.ui.tables.panels;

import javafx.scene.Node;

@FunctionalInterface
public interface BaseLoadingColumnRenderer<T, M> {

    Node render(T object, M model);

}
