package com.example.fdu_coursetable;

public class ClassInfo {
    private int fromX;
    private int fromY;
    private int toX;
    private int toY;

    private int classid;
    private String classname;
    private int fromClassNum;
    private int classNumLen;
    private int weekday;
    private String classRoom;
    private String teacherName;
    private String time;
    private Boolean isSingle;
    private Boolean isConfilct;

    public void setPoint(int fromX, int fromY, int toX, int toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    public int getFromX() {
        return fromX;
    }

    public void setFromX(int fromX) {
        this.fromX = fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public void setFromY(int fromY) {
        this.fromY = fromY;
    }

    public int getToX() {
        return toX;
    }

    public void setToX(int toX) {
        this.toX = toX;
    }

    public int getToY() {
        return toY;
    }

    public void setToY(int toY) {
        this.toY = toY;
    }

    public int getClassid() {
        return classid;
    }

    public void setClassid(int classid) {
        this.classid = classid;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public int getFromClassNum() {
        return fromClassNum;
    }

    public void setFromClassNum(int fromClassNum) {
        this.fromClassNum = fromClassNum;
    }

    public int getClassNumLen() {
        return classNumLen;
    }

    public void setClassNumLen(int classNumLen) {
        this.classNumLen = classNumLen;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public String getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    public String getTeacherName() { return teacherName; }

    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public Boolean getSingle() { return isSingle; }

    public void setSingle(Boolean single) { isSingle = single; }

    public Boolean getConfilct() { return isConfilct; }

    public void setConfilct(Boolean confilct) { isConfilct = confilct; }


}
