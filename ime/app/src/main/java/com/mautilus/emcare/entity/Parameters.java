package com.mautilus.emcare.entity;

public class Parameters {
    private Integer id;
    private String key;
    private String value;

    public static final String TABLE_NAME = "em_parameters";
    public static final String FIELD_ID_NAME = "em_id";
    public static final String FIELD_KEY_NAME = "em_key";
    public static final String FIELD_VALUE_NAME = "em_value";
    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + " ("
            + FIELD_ID_NAME + " INTEGER PRIMARY KEY, "
            + FIELD_KEY_NAME + " VARCHAR(200), "
            + FIELD_VALUE_NAME + " VARCHAR(200));";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Parameters(Integer id, String type, String time) {
        this.id = id;
        this.key = key;
        this.value = value;
    }
}