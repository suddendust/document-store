package org.hypertrace.core.documentstore.query;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hypertrace.core.documentstore.expression.impl.AggregateExpression;
import org.hypertrace.core.documentstore.expression.type.SelectingExpression;

/**
 * A generic selection definition that supports expressions with optional aliases (used in the
 * response).
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SelectionSpec {

  @NotNull SelectingExpression expression;

  // Alias is optional. Handling missing aliases can be implemented in the respective parsers
  String alias;

  public boolean isAggregation() {
    return expression instanceof AggregateExpression;
  }

  public static SelectionSpec of(final SelectingExpression expression) {
    return SelectionSpec.of(expression, null);
  }

  public static SelectionSpec of(final SelectingExpression expression, final String alias) {
    return new SelectionSpec(expression, alias);
  }
}
