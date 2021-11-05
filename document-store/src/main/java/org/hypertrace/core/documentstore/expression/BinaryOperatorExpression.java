package org.hypertrace.core.documentstore.expression;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class BinaryOperatorExpression implements OperatorExpression {
  private final Expression operand1; // Operand on the left side of the expression
  private final BinaryOperator operation;
  private final Expression operand2; // Operand on the right side of the expression

  @Override
  public String toString() {
    return String.format("%s(%s, %s)", operation.name(), operand1.toString(), operand2.toString());
  }
}