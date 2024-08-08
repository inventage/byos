package byos;

public enum StringComparisonOperator {
    _eq("_eq"),
    _neq("_neq"),
    _lt("_lt"),
    _lte("_lte"),
    _gt("_gt"),
    _gte("_gte"),
    _like("_like"),
    _ilike("_illike"),
    _regex("_regex"),
    _iregex("_iregex"),
    _in("_in"),
    _nin("_nin"),
    _is_null("_is_null");

    public final String operatorName;

    StringComparisonOperator(String operatorName) {
        this.operatorName = operatorName;
    }

}
