package bonie.android.todolist;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import com.hanks.passcodeview.PasscodeView;

import bonie.android.todolist.data.NotesContract;
import bonie.android.todolist.data.NotesDbHelper;

public class PasswordActivity extends AppCompatActivity {
    PasscodeView passcodeView;

    //making a global SQLiteDatabase object
    //Setting up the SQL
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        passcodeView = findViewById(R.id.passcodeView);

        //Setting up the SQL
        //passing the current activity context
        //making a global DbHelper
        NotesDbHelper notesDbHelper = new NotesDbHelper(this);

        //this will create and or open a database to read it
        //similar to --> .open notes.db
        db = notesDbHelper.getReadableDatabase();

        String passcode = "null";
        Cursor cursor = db.query(NotesContract.PasswordEntry.TABLE_NAME, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            passcode = cursor.getString(cursor.getColumnIndex(NotesContract.PasswordEntry.COLUMN_PASSWORD));
            cursor.close();
        }else{
            cursor.close();
            Toast.makeText(this,"ERROR GO TO PASSWORD_ACTIVITY", Toast.LENGTH_SHORT ).show();
            finish();
        }
        //set password length and password
        passcodeView.setPasscodeLength(passcode.length()).setLocalPasscode(passcode).setListener(new PasscodeView.PasscodeViewListener() {
            @Override
            public void onFail() {
                setResult(RESULT_CANCELED);
            }

            @Override
            //calls if user enters successful password
            public void onSuccess(String number) {
                setResult(RESULT_OK);
                db.close();
                finish();
            }
        });
    }
}