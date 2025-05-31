package de.mephisto.vpin.server.scripteval;


import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EvaluationContextTest {

  @Test 
  public void testEvaluate() {
    EvaluationContext evalctxt = new EvaluationContext();
    evalctxt.setVarValue("x", 5);
    evalctxt.setVarValue("y", 7);
    evalctxt.setVarValue("s", "super");
    evalctxt.setVarValue("t", "man");

    assertEquals(5, evalctxt.evaluateNumber("2+3").intValue());
    assertEquals(7, evalctxt.evaluateNumber("x + 2").intValue());
    assertEquals(35, evalctxt.evaluateNumber("x * y").intValue());

    assertEquals("superman", evalctxt.evaluateString("s+t"));
  }

  @Test 
  public void testUndefined() {
    EvaluationContext evalctxt = new EvaluationContext();
    BigDecimal result;
    result = evalctxt.evaluateNumber("x+3");
    assertNull(result);
    assertTrue(evalctxt.isUndefinedVar("x"));

    evalctxt.setVarExpression("x", "y");
    result = evalctxt.evaluateNumber("x+3");
    assertFalse(evalctxt.isUndefinedVar("x"));
    assertTrue(evalctxt.isUndefinedVar("y"));
    assertNull(result);

    evalctxt.setVarExpression("y", "4");
    result = evalctxt.evaluateNumber("x+3");
    assertFalse(evalctxt.isUndefinedVar("x"));
    assertFalse(evalctxt.isUndefinedVar("y"));
    assertEquals(7, result.intValue());
  }

  @Test 
  public void testLateEvaluate() {
    EvaluationContext evalctxt = new EvaluationContext();
    assertNull(evalctxt.evaluateString("a+b"));

    String[] res = { null, null };
    evalctxt.onEvaluateString("\"spider\" + b", s -> res[0] = s);
    evalctxt.onEvaluateString("a + b", s -> res[1] = s);
    assertNull(res[0]);
    assertNull(res[1]);

    evalctxt.setVarValue("b", "man");
    assertEquals("spiderman", res[0]);
    assertNull(res[1]);
    
    evalctxt.setVarValue("a", "super");
    assertEquals("spiderman", res[0]);
    assertEquals("superman", res[1]);
  }

  @Test
  public void testLateCascadeEvaluate() {
    EvaluationContext evalctxt = new EvaluationContext();
    assertNull(evalctxt.evaluateString("a+b"));

    String[] res = { null };
    evalctxt.onEvaluateString("a + b", s -> res[0] = s);
    // a & b unknown
    assertNull(res[0]);
    assertTrue(evalctxt.hasUndefinedVar());

    evalctxt.setVarExpression("a", "\"hello \"");
    evalctxt.setVarExpression("b", "c + \"man\"");
    // still null as c is unknown
    assertNull(res[0]);
    assertTrue(evalctxt.hasUndefinedVar());
    
    evalctxt.setVarValue("c", "super");
    assertEquals("hello superman", res[0]);
    assertFalse(evalctxt.hasUndefinedVar());
  }

  @Test
  public void testLateAndAssignEvaluate() {
    EvaluationContext evalctxt = new EvaluationContext();

    BigDecimal[] res = { null };
    // a & b unknown
    evalctxt.onEvaluateNumber("a + b + c", s -> res[0] = s);

    // (b+b) + b + c
    evalctxt.setVarExpression("a", "b + b");

    // b = 4 ; c = 3
    evalctxt.setVarExpression("b", "1 + (c=3)");

    assertEquals(15, res[0].intValue());
    assertFalse(evalctxt.hasUndefinedVar());
  }

  @Test
  public void testEvaluateLazy() {
    EvaluationContext evalctxt = new EvaluationContext();
    BigDecimal[] res = { null };
    evalctxt.onEvaluateNumber("(5 + (a = b)) * (b=3)", l -> res[0] = l);
    assertEquals(24, res[0].intValue());
  }


  @Test
  public void testEvaluateLists() {
    EvaluationContext evalctxt = new EvaluationContext();
    List<?>[] res = { null };
    evalctxt.onEvaluateList("TO_ARRAY( a = b+c, b=d, c=2, d=c )", l -> res[0] = l);
    @SuppressWarnings("unchecked")
    List<BigDecimal> values = (List<BigDecimal>) res[0]; 
    
    assertEquals(4, values.size());
    assertEquals(4, values.get(0).intValue());
    assertEquals(2, values.get(1).intValue());
    assertEquals(2, values.get(2).intValue());
    assertEquals(2, values.get(3).intValue());
  }


}
