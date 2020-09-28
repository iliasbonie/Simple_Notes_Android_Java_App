package bonie.android.todolist.data;

import android.provider.BaseColumns;

//Start of the contract class
public final class NotesContract {

    //private constructor so no one can't accidentally make a new object
    private NotesContract(){}

    public static class FolderEntry implements BaseColumns{

        //making the columns names for each one.
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_DESCRIPTION = "description";
        public final static String COLUMN_DATE = "date_created";
        public final static String COLUMN_DATE_VALUE = "date_created_value";
        public final static String COLUMN_DATE_MODIFIED = "date_modified";
        public final static String COLUMN_DATE_MODIFIED_VALUE = "date_modified_value";
        public final static String COLUMN_LOCK = "folder_lock";

        //table name
        public final static String TABLE_NAME = "folder";

        //create table
        public final static String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_DESCRIPTION + " TEXT, " + COLUMN_DATE + " TEXT, " + COLUMN_DATE_VALUE + " INTEGER, " + COLUMN_DATE_MODIFIED + " TEXT, " + COLUMN_DATE_MODIFIED_VALUE + " INTEGER, " + COLUMN_LOCK  + " INTEGER);";
        //delete table
        public final static String DROP_TABLE = "drop table if exists " + TABLE_NAME;
    }

    public static class NotesEntry implements BaseColumns{

        //making the columns names for each one.
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_DESCRIPTION = "description";
        public final static String COLUMN_FOLDER_ID = "folder_id";
        public final static String COLUMN_DATE = "date_created";
        public final static String COLUMN_DATE_VALUE = "date_created_value";
        public final static String COLUMN_DATE_MODIFIED = "date_modified";
        public final static String COLUMN_DATE_MODIFIED_VALUE = "date_modified_value";


        //table name
        public final static String TABLE_NAME = "notes";

        //create table
        public final static String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_DESCRIPTION + " TEXT, " + COLUMN_DATE + " TEXT, " + COLUMN_DATE_VALUE + " INTEGER, " + COLUMN_DATE_MODIFIED + " TEXT, " + COLUMN_DATE_MODIFIED_VALUE + " INTEGER, " + COLUMN_FOLDER_ID + " INTEGER);";
        //delete table
        public final static String DROP_TABLE = "drop table if exists " + TABLE_NAME;

    }

    public static class TrashEntry implements BaseColumns{
        //making the columns names for each one.
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_DESCRIPTION = "description";
        public final static String COLUMN_DATE = "date_created";
        public final static String COLUMN_DATE_VALUE = "date_created_value";
        public final static String COLUMN_DATE_MODIFIED = "date_modified";
        public final static String COLUMN_DATE_MODIFIED_VALUE = "date_modified_value";

        //table name
        public final static String TABLE_NAME = "trash";

        //create table
        public final static String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_DESCRIPTION + " TEXT, " + COLUMN_DATE + " TEXT, " + COLUMN_DATE_VALUE + " INTEGER, " + COLUMN_DATE_MODIFIED + " TEXT, " + COLUMN_DATE_MODIFIED_VALUE + " INTEGER);";
        //delete table
        public final static String DROP_TABLE = "drop table if exists " + TABLE_NAME;
    }

    public static class PasswordEntry implements BaseColumns{
        //making the columns names
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_PASSWORD = "password_text";
        public final static String COLUMN_PASSWORD_LENGTH = "password_length";

        //table name
        public final static String TABLE_NAME = "password";

        //create table
        public final static String CREATE_TABLE = "create table " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_PASSWORD + " TEXT, " + COLUMN_PASSWORD_LENGTH + " INTEGER);";

        //delete table
        public final static String DROP_TABLE = "drop table if exists " + TABLE_NAME;
    }

}

