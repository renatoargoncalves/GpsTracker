package com.sow.gpstrackerpro.classes;

public enum Fragments {
    FIRST_EXECUTION("first_execution"),
    SIGN_IN_REQUEST("sign_in_request");

    private final String value;

    private Fragments(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}