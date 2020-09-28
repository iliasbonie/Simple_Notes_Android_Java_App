package bonie.android.todolist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NotesDbHelper extends SQLiteOpenHelper {
    //Creating the DB name and version
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "notes.db";

    //constructor but passes up to the hierarchy
    public NotesDbHelper(Context context) {
          //args take DB name. factor, version# and context.
          super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    //this will execute and create or read the table
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NotesContract.FolderEntry.CREATE_TABLE);
        db.execSQL(NotesContract.NotesEntry.CREATE_TABLE);
        db.execSQL(NotesContract.PasswordEntry.CREATE_TABLE);
        db.execSQL(NotesContract.TrashEntry.CREATE_TABLE);
    }

    @Override
    //this will delete the table and recreate it again with updated information
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(NotesContract.FolderEntry.DROP_TABLE);
        db.execSQL(NotesContract.NotesEntry.DROP_TABLE);
        db.execSQL(NotesContract.PasswordEntry.DROP_TABLE);
        db.execSQL(NotesContract.TrashEntry.DROP_TABLE);
        onCreate(db);
    }
}

