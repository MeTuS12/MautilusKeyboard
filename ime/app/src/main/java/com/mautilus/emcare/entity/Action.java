package com.mautilus.emcare.entity;

public class Action {

    public static final int STEP_COUNT_EVENT = 17;

    private Integer id;
    private String type;
    private Long time;
    private Long timeStart;
    private Integer addedCount;
    private Integer removedCount;

    public static final String TABLE_NAME = "em_action";
    public static final String FIELD_ID_NAME = "em_id";
    public static final String FIELD_TYPE_NAME = "em_type";
    public static final String FIELD_TIME_NAME = "em_time";
    public static final String FIELD_TIME_START_NAME = "em_time_start";
    public static final String FIELD_ADDED_NAME = "em_added_count";
    public static final String FIELD_REMOVED_NAME = "em_removed_count";
    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + " ("
            + FIELD_ID_NAME + " INTEGER PRIMARY KEY, "
            + FIELD_TYPE_NAME + " VARCHAR(200), "
            + FIELD_ADDED_NAME + " INTEGER, "
            + FIELD_REMOVED_NAME + " INTEGER, "
            + FIELD_TIME_START_NAME + " INTEGER, "
            + FIELD_TIME_NAME + " INTEGER);";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Action(Integer id, String type, Long time, Long timeStart) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.timeStart = timeStart;
    }

    public Integer getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(Integer addedCount) {
        this.addedCount = addedCount;
    }

    public Integer getRemovedCount() {
        return removedCount;
    }

    public void setRemovedCount(Integer removedCount) {
        this.removedCount = removedCount;
    }

    public Long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Long timeStart) {
        this.timeStart = timeStart;
    }
}