package byos;

import static byos.ByosConstants.*;

public enum StringComparisonOperator {
    _eq(COMPARISON_EQ),
    _neq(COMPARISON_NEQ),
    _lt(COMPARISON_LT),
    _lte(COMPARISON_LTE),
    _gt(COMPARISON_GT),
    _gte(COMPARISON_GTE),
    _like(COMPARISON_LIKE),
    _ilike(COMPARISON_ILLIKE),
    _regex(COMPARISON_REGEX),
    _iregex(COMPARISON_IREGEX),
    _in(COMPARISON_IN),
    _nin(COMPARISON_NIN),
    _is_null(COMPARISON_IS_NULL);

    public final String operatorName;

    StringComparisonOperator(String operatorName) {
        this.operatorName = operatorName;
    }

}
