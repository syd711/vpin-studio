package de.mephisto.vpin.server.scripteval;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.operators.AbstractOperator;
import com.ezylang.evalex.operators.InfixOperator;
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Token.TokenType;

@InfixOperator(precedence = 1, operandsLazy = true)
public class InfixAssignOperator extends AbstractOperator {

  @Override
  public EvaluationValue evaluate(Expression expression, Token operatorToken, EvaluationValue... operands) throws EvaluationException {
    String name = operands[0].getExpressionNode().getToken().getValue();
    try {
      EvaluationValue v = expression.evaluateSubtree(operands[1].getExpressionNode());
      expression.with(name, v.getValue());
      return v;
    }
    catch (EvaluationException ee) {
      if (expression instanceof LazyExpression) {
        expression.getDataAccessor().setData(name, operands[1]);
        Token tok = new Token(-1, name, TokenType.VARIABLE_OR_CONSTANT);
        return EvaluationValue.expressionNodeValue(new ASTNode(tok));
      }
      // else
      throw ee;
    }
  }

}
