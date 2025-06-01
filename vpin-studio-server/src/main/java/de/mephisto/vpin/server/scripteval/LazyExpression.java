package de.mephisto.vpin.server.scripteval;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;

public class LazyExpression extends Expression {

  public LazyExpression(ExpressionConfiguration configuration) {
    super("", configuration);
  }
}
