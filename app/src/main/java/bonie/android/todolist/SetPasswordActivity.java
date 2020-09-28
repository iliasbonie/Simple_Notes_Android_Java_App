package bonie.android.todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import bonie.android.todolist.data.NotesContract;
import bonie.android.todolist.data.NotesDbHelper;

public class SetPasswordActivity extends AppCompatActivity {

    //making a global SQLiteDatabase object
    //Setting up the SQL
    private SQLiteDatabase db;

    //making a global DbHelper
    private NotesDbHelper notesDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        Button cancel = findViewById(R.id.cancel);
        Button enter = findViewById(R.id.enter);

        //Setting up the SQL
        //passing the current activity context
        notesDbHelper = new NotesDbHelper(this);

        //if the user clicks the back button then it goes back to the main Activity/previous page
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //if the user clicks the continue button then it saves the password in the database and goes back to the previous page
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //this will create and or open a database to read it
                //similar to --> .open notes.db
                db = notesDbHelper.getReadableDatabase();

                db = notesDbHelper.getWritableDatabase();

                //deleting the current database contents to make sure
                db.execSQL(" DELETE FROM " + NotesContract.PasswordEntry.TABLE_NAME);
                //getting the results from the editText View
                EditText psw = findViewById(R.id.enter_password);

                //converting the TextView and length to a String value
                String password = psw.getText().toString();
                int length = password.length();
                String strLength = Integer.toString(length);
                //adding it to the content Values
                ContentValues values = new ContentValues();
                values.put(NotesContract.PasswordEntry.COLUMN_PASSWORD, password);
                values.put(NotesContract.PasswordEntry.COLUMN_PASSWORD_LENGTH, strLength);

                //insert the values to the Password Table
                db.insert(NotesContract.PasswordEntry.TABLE_NAME, null, values);

                db.close();
                finish();
            }
        });
    }
}
