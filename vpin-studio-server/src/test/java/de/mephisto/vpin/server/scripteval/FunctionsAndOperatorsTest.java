package de.mephisto.vpin.server.scripteval;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FunctionsAndOperatorsTest {

  @Test
  public void testAssign() throws Exception {
    // A shared dataAccessor accross Expression
    EvaluationContext evalctxt = new EvaluationContext();
    BigDecimal result;
    
    result = evalctxt.evaluateNumber("y= 12");
    assertEquals(12, result.intValue());

    // check ability to reuse variables accross Expression
    result = evalctxt.evaluateNumber("y*5");
    assertEquals(60, result.intValue());

    // inline assignment works too
    result = evalctxt.evaluateNumber("4 + (x=3) + x");
    assertEquals(10, result.intValue());
  }

  @Test
  public void testHexa() {
    EvaluationContext evalctxt = new EvaluationContext();
    BigDecimal result;
    
    result = evalctxt.evaluateNumber("0x00001");
    assertEquals(1, result.intValue());

    result = evalctxt.evaluateNumber("&H00003");
    assertEquals(3, result.intValue());

  }

  @Test
  public void testStringConcat() throws Exception {
    EvaluationContext evalctxt = new EvaluationContext();
    String result;

    result = evalctxt.evaluateString("\"hello \" & \"world\"");
    assertEquals("hello world", result);

    evalctxt.setVarValue("gamename", "world");
    result = evalctxt.evaluateString("\"hello \" & gamename");
    assertEquals("hello world", result);

    evalctxt.setVarValue("S1", "vpin-studio");
    evalctxt.setVarValue("s2", 4);

    result = evalctxt.evaluateString("\"hello \" & s1 & \", version \" & s2");
    assertEquals("hello vpin-studio, version 4", result);

  }

  @Test
  public void testLists() {
    EvaluationContext evalctxt = new EvaluationContext();
    List<?> values = evalctxt.evaluateList("TO_ARRAY(1, 2, \"3\", 5.0 - 0.5)");
    assertEquals(4, values.size());
    assertEquals(1, ((BigDecimal) values.get(0)).intValue());
    assertEquals(2, ((BigDecimal) values.get(1)).intValue());
    assertEquals("3", values.get(2));
    assertEquals(4.5, ((BigDecimal) values.get(3)).doubleValue());
  }
}
