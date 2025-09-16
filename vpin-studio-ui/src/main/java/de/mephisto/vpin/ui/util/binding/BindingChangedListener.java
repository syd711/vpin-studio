package de.mephisto.vpin.ui.util.binding;

@FunctionalInterface
public interface BindingChangedListener {

  void beanPropertyChanged(Object bean, String key, Object value);
}
