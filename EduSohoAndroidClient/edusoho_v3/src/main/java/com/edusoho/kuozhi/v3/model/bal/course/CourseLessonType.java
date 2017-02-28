package com.edusoho.kuozhi.v3.model.bal.course;

public enum CourseLessonType {

    DOCUMENT, VIDEO, TEXT, TESTPAPER, AUDIO, PPT, EMPTY, CHAPTER, UNIT, LIVE, DEFAULT, FLASH;

    public static CourseLessonType value(String typeName) {
        CourseLessonType type;
        try {
            type = valueOf(typeName.toUpperCase());
        } catch (Exception e) {
            type = DEFAULT;
        }
        return type;
    }

    public StringBuilder getType() {
        StringBuilder stringBuilder = new StringBuilder(toString().toLowerCase());
        char first = Character.toUpperCase(stringBuilder.charAt(0));
        stringBuilder.deleteCharAt(0);
        stringBuilder.insert(0, first);
        return stringBuilder;
    }
}
