package org.hypertrace.core.documentstore.mongo;

import static org.hypertrace.core.documentstore.Collection.UNSUPPORTED_QUERY_OPERATION;
import static org.hypertrace.core.documentstore.mongo.MongoUtils.PREFIX;

import com.mongodb.BasicDBObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hypertrace.core.documentstore.Filter;
import org.hypertrace.core.documentstore.OrderBy;

class MongoQueryParser {

  static Map<String, Object> parseFilter(Filter filter) {
    if (filter.isComposite()) {
      Filter.Op op = filter.getOp();
      switch (op) {
        case OR:
        case AND:
          {
            List<Map<String, Object>> childMapList =
                Arrays.stream(filter.getChildFilters())
                    .map(MongoQueryParser::parseFilter)
                    .filter(map -> !map.isEmpty())
                    .collect(Collectors.toList());
            if (!childMapList.isEmpty()) {
              return Map.of(PREFIX + op.name().toLowerCase(), childMapList);
            } else {
              return Collections.emptyMap();
            }
          }
        default:
          throw new UnsupportedOperationException(
              String.format("Boolean operation:%s not supported", op));
      }
    } else {
      Filter.Op op = filter.getOp();
      Object value = filter.getValue();
      Map<String, Object> map = new HashMap<>();
      switch (op) {
        case EQ:
          map.put(filter.getFieldName(), value);
          break;
        case LIKE:
          // Case insensitive regex search
          map.put(
              filter.getFieldName(), new BasicDBObject("$regex", value).append("$options", "i"));
          break;
        case NOT_IN:
          map.put(filter.getFieldName(), new BasicDBObject("$nin", value));
          break;
        case IN:
          map.put(filter.getFieldName(), new BasicDBObject("$in", value));
          break;
        case CONTAINS:
          map.put(filter.getFieldName(), new BasicDBObject("$elemMatch", filter.getValue()));
          break;
        case GT:
          map.put(filter.getFieldName(), new BasicDBObject("$gt", value));
          break;
        case LT:
          map.put(filter.getFieldName(), new BasicDBObject("$lt", value));
          break;
        case GTE:
          map.put(filter.getFieldName(), new BasicDBObject("$gte", value));
          break;
        case LTE:
          map.put(filter.getFieldName(), new BasicDBObject("$lte", value));
          break;
        case EXISTS:
          map.put(filter.getFieldName(), new BasicDBObject("$exists", true));
          break;
        case NOT_EXISTS:
          map.put(filter.getFieldName(), new BasicDBObject("$exists", false));
          break;
        case NEQ:
          // $ne operator in Mongo also returns the results, where the key does not exist in the
          // document. This is as per semantics of EQ vs NEQ. So, if you need documents where
          // key exists, consumer needs to add additional filter.
          // https://github.com/hypertrace/document-store/pull/20#discussion_r547101520
          map.put(filter.getFieldName(), new BasicDBObject("$ne", value));
          break;
        case AND:
        case OR:
        default:
          throw new UnsupportedOperationException(UNSUPPORTED_QUERY_OPERATION);
      }
      return map;
    }
  }

  static LinkedHashMap<String, Object> parseOrderBys(List<OrderBy> orderBys) {
    LinkedHashMap<String, Object> orderByMap = new LinkedHashMap<>();
    for (OrderBy orderBy : orderBys) {
      orderByMap.put(orderBy.getField(), (orderBy.isAsc() ? 1 : -1));
    }
    return orderByMap;
  }
}
