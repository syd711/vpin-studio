package de.mephisto.vpin.server.scripteval;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.operators.AbstractOperator;
import com.ezylang.evalex.operators.InfixOperator;
import com.ezylang.evalex.parser.Token;

import static com.ezylang.evalex.operators.OperatorIfc.OPERATOR_PRECEDENCE_ADDITIVE;

@InfixOperator(precedence = OPERATOR_PRECEDENCE_ADDITIVE)
public class InfixConcatOperator extends AbstractOperator {

  @Override
  public EvaluationValue evaluate(Expression expression, Token operatorToken, EvaluationValue... operands) throws EvaluationException {
    EvaluationValue leftOperand = operands[0];
    EvaluationValue rightOperand = operands[1];
    return expression.convertValue(leftOperand.getStringValue() + rightOperand.getStringValue());
  }

}
