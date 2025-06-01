package de.mephisto.vpin.server.scripteval;

import java.util.ArrayList;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

@FunctionParameter(name = "args", isVarArg = true)
public class ToArrayFunction extends AbstractFunction {

  @Override
  public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues) throws EvaluationException {
    ArrayList<Object> values = new ArrayList<>();
    for (EvaluationValue parameter : parameterValues) {
      if (parameter.isExpressionNode()) {
        parameter = expression.evaluateSubtree(parameter.getExpressionNode());
      }
      values.add(parameter.getValue());
    }
    return EvaluationValue.arrayValue(values);
  }

}