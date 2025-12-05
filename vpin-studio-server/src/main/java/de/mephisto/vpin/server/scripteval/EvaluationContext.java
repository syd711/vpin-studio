package de.mephisto.vpin.server.scripteval;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.MapBasedDataAccessor;
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token.TokenType;

public class EvaluationContext {
  private final static Logger LOG = LoggerFactory.getLogger(EvaluationContext.class);

  private MapBasedDataAccessor dataAccessor;
  private ExpressionConfiguration configuration;

  private LazyExpression backgroundExpression;

  private Set<String> undefinedVars = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
  private Set<String> onEvalVars = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

  private Map<ASTNode, Consumer<EvaluationValue>> onEvalExpressions = new HashMap<>();

  /** A temporary storage that can be used to store values during the analysis */
  private Map<String, Object> tempStorage = new HashMap<>();

  public EvaluationContext() {
    this.dataAccessor = new MapBasedDataAccessor() {
      @Override
      public void setData(String variable, EvaluationValue value) {
        if (value.isExpressionNode()) {
          setVarExpression(variable, value.getExpressionNode());
        } else {
          super.setData(variable, value);
          if (undefinedVars.remove(variable) || onEvalVars.remove(variable)) {
            // variable was undefined or under evaluation, trigger the recalculations
            evaluateOnEvalExpressions();
          }
        }          
      }
    };
    configuration = ExpressionConfiguration.builder()
      .dataAccessorSupplier(() -> dataAccessor)
      .build()
      .withAdditionalFunctions(
        Map.entry("TO_ARRAY", new ToArrayFunction())
      )
      .withAdditionalOperators(
        Map.entry("&", new InfixConcatOperator()),
        Map.entry("=", new InfixAssignOperator())
      );

    backgroundExpression = new LazyExpression(configuration);
  }

  private EvaluationValue evaluateExpression(String expressionString) {
    Expression e = new Expression(cleanExpression(expressionString), configuration);
    ASTNode subtree = null;
    try {
      subtree = e.getAbstractSyntaxTree();
      return e.evaluateSubtree(subtree);
    }
    catch(EvaluationException ee) {
      LOG.info("Cannot evaluate {}:", expressionString);
      if (subtree != null) {
        for (String var : getAllVariablesForNode(subtree)) {
          if (dataAccessor.getData(var) == null && !onEvalVars.contains(var)) {
            addUndefinedVar(var);
            LOG.info("-> Variable or constant value for '{}' not found", var);
          }
        }
      }
      return null;
    }
    catch(ParseException pe) {
      LOG.warn("Cannot parse undefined variables for {}: {}", expressionString, pe.getMessage());
      return null;
    }
  }

  private String cleanExpression(String expressionString) {
    // replace hexa sequence
    expressionString = expressionString.replace("&H", "0x");
    expressionString = expressionString.replace("&h", "0x");
    // protect \
    expressionString = expressionString.replace("\\", "\\\\");
    return expressionString;
  }

  public BigDecimal evaluateNumber(String expressionString) {
    EvaluationValue ev = evaluateExpression(expressionString);
    return ev != null ? ev.getNumberValue() : null;
  }

  public String evaluateString(String expressionString) {
    EvaluationValue ev = evaluateExpression(expressionString);
    return ev != null ? ev.getStringValue() : null;
  }

  public List<?> evaluateList(String expressionString) {
    EvaluationValue ev = evaluateExpression(expressionString);
    return ev != null ? ev.getArrayValue() : null;
  }

  //-------------------

  @SuppressWarnings("unchecked")
  public <T> T getTempValue(String name) {
    return (T) tempStorage.get(name);
  }

  public void setTempValue(String name, Object value) {
    if (value == null) {
      tempStorage.remove(name);
    } else {
      tempStorage.put(name, value);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T removeTempValue(String name) {
    return (T) tempStorage.remove(name);
  }

  //-------------------

  public boolean hasUndefinedVar() {
    return !undefinedVars.isEmpty();
  }

  /**
   * Check presence of an undefinedVars, in a case insentive way
   */
  public boolean isUndefinedVar(String var) {
    return undefinedVars.contains(var);
  }

  /**
   * Public method so that caller can specify a specific variable to capture
   */
  private boolean addUndefinedVar(String var) {
    return undefinedVars.add(var);
  }


  @SuppressWarnings("unchecked")
  public <T> T getVarValue(String var) {
    EvaluationValue ev = dataAccessor.getData(var);
    return ev != null ? (T) ev.getValue() : null;
  }

  /**
   * Set the value for a variable, if this is was a monitored ones, will trigger recalculations 
   */
  public void setVarValue(String var, Object value) {
    dataAccessor.setData(var, EvaluationValue.of(value, configuration));
  }

  public void setVarExpression(String var, String varExp) {
    boolean success = onEvaluateExpression(varExp, ev -> {
      dataAccessor.setData(var, ev);
    });
    // remove the undefined vars only if the var expression can be evaluated 
    if (success) {
      undefinedVars.remove(var);
      onEvalVars.add(var);
    } 
    else {
      //...a bit too verbose
      //LOG.error("Expression for variable '{}'' cannot be evaluated, the variable remains undefined !", var);
    }
  }

  public void setVarExpression(String var, ASTNode subtree) {
    onEvaluateExpression(subtree, ev -> {
      dataAccessor.setData(var, ev);
    });
    // remove the undefined vars only if the var expression can be evaluated 
    undefinedVars.remove(var);
    onEvalVars.add(var);
  }

  private List<String> getAllVariablesForNode(ASTNode node) {
    List<String> vars = new ArrayList<>();
    if (node.getToken().getType() == TokenType.VARIABLE_OR_CONSTANT
          && !backgroundExpression.getConstants().containsKey(node.getToken().getValue())) {
      vars.add(node.getToken().getValue());
    }
    for (ASTNode child : node.getParameters()) {
      vars.addAll(getAllVariablesForNode(child));
    }
    return vars;
  }

  //-------------------

  /**
   * Parse the expression and lazy evaluate it
   * @param expressionString the Expression to evaluate
   * @param consumer The OCnsumer when Expression is evaluated
   * @return true if expression is taken in account
   */
  private boolean onEvaluateExpression(String expressionString, Consumer<EvaluationValue> consumer) {
    try {
      Expression e = new Expression(cleanExpression(expressionString), configuration);
      ASTNode subtree = e.getAbstractSyntaxTree();
      onEvaluateExpression(subtree, consumer);
      if (!onEvalExpressions.isEmpty()) {
        evaluateOnEvalExpressions();
      }
      return true;
    }
    catch(ParseException pe) {
//      LOG.warn("Cannot parse {},  : {}", expressionString, pe.getMessage());
      return false;
    }
  }

  private void onEvaluateExpression(ASTNode subtree, Consumer<EvaluationValue> consumer) {
    try {
      EvaluationValue ev = backgroundExpression.evaluateSubtree(subtree);
      if (ev != null && !ev.isExpressionNode()) {
        consumer.accept(ev);
        return;
      }
    }
    catch(EvaluationException ee) {
      //LOG.info("Cannot evaluate subtree");
      for (String var : getAllVariablesForNode(subtree)) {
        if (dataAccessor.getData(var) == null && !onEvalVars.contains(var) && !undefinedVars.contains(var)) {
          addUndefinedVar(var);
          //LOG.info("-> Variable or constant value for '{}' not found", var);
        }
      }
    }
    // not evaluated
    onEvalExpressions.put(subtree, consumer);
  }

  /**
   * Call the consumer when the context will be able to evaluate teh expression
   * @param consumer The Consumer that will consume the evaluated value
   * @return Return true if the expression is valid and a value could be returned 
   */
  public boolean onEvaluateString(String expressionString, Consumer<String> consumer) {
    return onEvaluateExpression(expressionString, ev-> consumer.accept(ev.getStringValue()));
  }

  /**
   * Call the consumer when the context will be able to evaluate teh expression
   * @param consumer The Consumer that will consume the evaluated value
   * @return Return true if the expression is valid and a value could be returned 
   */
  public boolean onEvaluateNumber(String expressionString, Consumer<BigDecimal> consumer) {
    return onEvaluateExpression(expressionString, ev-> consumer.accept(ev.getNumberValue()));
  }

  public boolean onEvaluateList(String expressionString, Consumer<List<?>> consumer) {
    return onEvaluateExpression(expressionString, ev-> consumer.accept(ev.getArrayValue()));
  }

  
  boolean hasDirty = false;
  boolean isOnEval = false;

  private void evaluateOnEvalExpressions() {
    if (isOnEval) {
      // nested called while already evaluating => flag
      hasDirty = true;
      return;
    }

    // start the evaluations
    isOnEval = true;
    hasDirty = false;

    for (Iterator<Map.Entry<ASTNode, Consumer<EvaluationValue>>> iter = onEvalExpressions.entrySet().iterator(); iter.hasNext(); ) {
      try {
        Map.Entry<ASTNode, Consumer<EvaluationValue>> entry = iter.next();
        EvaluationValue ev = backgroundExpression.evaluateSubtree(entry.getKey());
        if (ev != null) {
          entry.getValue().accept(ev);
          iter.remove();
        }
      }
      catch (Exception e) {
        // not the time yet... ignore error
      }
    }
    isOnEval = false;

    // while we get modifications, continue to evaluate
    if (hasDirty) {
      evaluateOnEvalExpressions();
    }
  }
}
