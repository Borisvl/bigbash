package de.zalando.bigbash.entities;

public enum FieldType {

    INTEGER,
    REAL,
    TEXT,
    DATE;

    public static FieldType fromString(final String s) {
        if (s.toUpperCase().equals(INTEGER.toString()) || s.toUpperCase().equals("INT")) {
            return FieldType.INTEGER;
        } else if (s.toUpperCase().equals(REAL.toString())) {
            return FieldType.REAL;
        } else if (s.toUpperCase().equals(DATE.toString())) {
            return FieldType.DATE;
        } else {
            return FieldType.TEXT;
        }
    }

}
