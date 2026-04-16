CREATE
ALIAS TO_NUMBER AS $$
Long toNumber(String value) {
    return value == null ? null : Long.valueOf(value);
}
$$;