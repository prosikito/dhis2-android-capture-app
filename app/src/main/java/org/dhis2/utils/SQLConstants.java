package org.dhis2.utils;

public class SQLConstants {

    private SQLConstants() {
        // hide public constructor
    }

    public static final String SELECT = " SELECT ";
    public static final String ALL = " * ";
    public static final String FROM = " FROM ";
    public static final String JOIN = " JOIN ";
    public static final String ON = " ON ";
    public static final String POINT = ".";
    public static final String EQUAL = " = ";
    public static final String NOT_EQUAL = " != ";
    public static final String QUOTE = "'";
    public static final String WHERE = " WHERE ";
    public static final String AND = " AND ";
    public static final String QUESTION_MARK = " ? ";
    public static final String LIMIT_1 = " LIMIT_1 ";
    public static final String TABLE_POINT_COLUMN_EQUAL = " %s.%s = ";
    public static final String TABLE_POINT_COLUMN = " %s.%s ";
    public static final String VARIABLE = " %s ";
}
