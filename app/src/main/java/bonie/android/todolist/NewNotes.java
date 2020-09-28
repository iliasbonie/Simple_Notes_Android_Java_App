package bonie.android.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import bonie.android.todolist.data.NotesContract;
import bonie.android.todolist.data.NotesDbHelper;

public class NewNotes extends AppCompatActivity {

    //making a global SQLiteDatabase object
    //Setting up the SQL
    //passing the current activity context
    private SQLiteDatabase db;

    //making a global DbHelper
    private NotesDbHelper notesDbHelper;

    String notes_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_notes);

        //Setting up the SQL
        //passing the current activity context
        notesDbHelper = new NotesDbHelper(this);

        //this will create and or open a database to read it
        //similar to --> .open notes.db
        db = notesDbHelper.getReadableDatabase();

        //making the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow_24dp);// set drawable icon
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //getting permission to order to export pdf files
        ActivityCompat.requestPermissions(NewNotes.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        //fill values if possible
        Intent intent = getIntent();
        String title = intent.getStringExtra("setTitle");
        String desc = intent.getStringExtra("setDesc");
        notes_id = intent.getStringExtra("notes_id");
        if(title != null || desc != null){
            TextView Title = (TextView)findViewById(R.id.input_folder_name);
            Title.setText(title);
            TextView Desc = (TextView)findViewById(R.id.folder_desc);
            Desc.setText(desc);
        }

    }


    //this function will call if one of the items in the option menu gets clicked on
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //using a switch statement so it checks every item and see which one was clicked
        switch (item.getItemId()){
            case(R.id.save):
                save();
                return true;
            case(R.id.save_pdf):
                createMyPDF();
                return true;
            case(R.id.save_txt):
                createTxtFile();
                return true;
            case(R.id.share_pdf):
                //Create the PDF
                createMyPDF();
                //get the Title from the notes page
                EditText title = findViewById(R.id.input_folder_name);
                String myTitle = title.getText().toString();
                //retrieve the PDF path name
                File fileLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), myTitle+".pdf");
                Uri pathUri = FileProvider.getUriForFile(NewNotes.this,"bonie.android.todolist.provider",fileLocation);

                //creating implicit intents
                //create email intent and attach the pdf
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Notes: " + myTitle);
                intent.putExtra(Intent.EXTRA_STREAM, pathUri);

                if(intent.resolveActivity(getPackageManager()) != null){
                    this.startActivity(Intent.createChooser(intent,"Send email..."));
                }
                return true;
            case(R.id.share_txt):
                createTxtFile();
                //get the Title from the notes page
                title = findViewById(R.id.input_folder_name);
                myTitle = title.getText().toString();
                //retrieve the txt file path name
                File textFilePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "ToDoList/" + myTitle+".txt");
                Uri txtUri = FileProvider.getUriForFile(NewNotes.this, "bonie.android.todolist.provider",textFilePath);

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Notes: " + myTitle);
                emailIntent.putExtra(Intent.EXTRA_STREAM, txtUri);

                if(emailIntent.resolveActivity(getPackageManager()) != null){
                    this.startActivity(Intent.createChooser(emailIntent,"Send email..."));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //this personal created function will be responsible for saving the contents of the document and adding it to the adapter
    public void save(){
        //making a word object
        WordFolder word = new WordFolder();

        //make TextView variables and cast the contents to a string and save it to a String variable
        TextView name = findViewById(R.id.input_folder_name);
        word.setTitle(name.getText().toString());

        TextView descText = findViewById(R.id.folder_desc);
        word.setDesc(descText.getText().toString());

        //saving the data to pass to previous activity
        //if the entire string is empty for both title and desc then just don't save
        if(word.getTitle().trim().isEmpty() && word.getTitleDesc().trim().isEmpty()){
            setResult(RESULT_CANCELED);
        }
        else{


            Intent intent = new Intent();
            //storing the current EditText into the intent to go back and use in previous activity
            intent.putExtra("title", word.getTitle());
            intent.putExtra("desc",word.getTitleDesc());

            setResult(RESULT_OK, intent);

            //if the user clicks save and it is currently a new note
            if(notes_id.equals("-1")){

                //if brand new note then get the date of the note
                //getting the current date here
                Calendar calendar = Calendar.getInstance();
                word.setDateCreated(DateFormat.getDateInstance().format(calendar.getTime()));
                //dateCreated = DateFormat.getDateInstance().format(calendar.getTime());

                //getting the current date here
                String pattern = "yyyyMMddHHmmss";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                Date date = new Date();
                word.setDateCreatedValue(simpleDateFormat.format(date));
                //dateValue = simpleDateFormat.format(date);
                word.setDateModifiedValue(word.getDateCreatedValue());
                //dateModifiedValue = dateValue;

                //get the current date in --> yyyy/mm/dd HH:mm:ss <-- format
                simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
                word.setDateModified(simpleDateFormat.format(date));
                //dateModified = simpleDateFormat.format(date);

                //adds to the SQLite database for future use
                addToDatabase(word);
            }else{

                //setting up variables
                String dateModified;
                String dateModifiedValue;

                db = notesDbHelper.getReadableDatabase();

                //getting ready to update the database
                db = notesDbHelper.getWritableDatabase();



                //getting the current date here
                String pattern = "yyyyMMddHHmmss";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                Date date = new Date();
                word.setDateModifiedValue(simpleDateFormat.format(date));

                //get the current date in --> yyyy/mm/dd HH:mm:ss <-- format
                simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
                word.setDateModified(simpleDateFormat.format(date));

                //creating the ContentValues and inputting the updated values
                ContentValues values = new ContentValues();
                values.put(NotesContract.NotesEntry.COLUMN_NAME,word.getTitle());
                values.put(NotesContract.NotesEntry.COLUMN_DESCRIPTION, word.getTitleDesc());
                values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED,word.getDateModified());
                values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED_VALUE,word.getDateModifiedValue());

                String selection = NotesContract.NotesEntry.COLUMN_ID + "=?";
                String[] selectionArgs = new String[]{notes_id};

                //updating the database using the .update method
                db.update(NotesContract.NotesEntry.TABLE_NAME,values, selection, selectionArgs);
                db.close();
            }

        }
    }

    //creating the option menu for the toolbar --> edit later
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //creating PDF
    public void createMyPDF(){
        EditText title = findViewById(R.id.input_folder_name);
        EditText desc = findViewById(R.id.folder_desc);

        //creating a pdf document
        PdfDocument myPdfDocument = new PdfDocument();

        //making a pageInfo to store the info needed to build the pdf.
        //first arg = pageWidth, second arg = pageHeight, third arg = page number
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300,600,1).create();

        //PdfDocument.Page gets your current page and request the current pdfDocument and PageInfo you had before
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        //Paint - used for styling
        Paint myPaint = new Paint();

        //warning it will return the editText by one line. if you have multiple lines you need to deal with that
        String myTitle = title.getText().toString();
        String myDesc = desc.getText().toString();
        int x = 10, y = 25;

        //getting the total string by combining both title and desc
        String title_desc = myTitle + "\n" + myDesc;

        //getting line by line on each String and inputting it to the page for the PDF document
        for (String line: title_desc.split("\n")){
            //drawText takes 4 arguments
            //String - the text to store in the PDF, int x = the horizontal position, int y = vertical position, Paint = the style

            myPage.getCanvas().drawText(line, x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();

        }
        //when you are finish making your page
        myPdfDocument.finishPage(myPage);

        //setting the file path name of the pdf and exporting it to the device
        String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/" + myTitle + ".pdf";
        File myFile = new File(myFilePath);

        try {
            myPdfDocument.writeTo(new FileOutputStream(myFile));
        } catch (Exception e){
            e.printStackTrace();
        }

        //when you are done making your document
        myPdfDocument.close();
    }
    //create text files
    public void createTxtFile(){
        EditText title = findViewById(R.id.input_folder_name);
        EditText desc = findViewById(R.id.folder_desc);
        String myTitle = title.getText().toString();
        String myDesc = desc.getText().toString();
        try{
            //gets the path and makes a folder called "ToDoList"
            File root = new File(Environment.getExternalStorageDirectory(), "ToDoList");
            //if folder doesn't exist then it makes one
            if(!root.exists()){
                root.mkdirs();
            }
            //creates the .txt file
            File gpxfile = new File(root, myTitle+".txt");
            FileWriter writer = new FileWriter(gpxfile);
            //append information and closes
            writer.append(myTitle + "\n" + myDesc);
            writer.flush();
            writer.close();

            //if caught an error
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //this function will add to the SQLite database for future use
    public void addToDatabase(WordFolder word){

        db = notesDbHelper.getReadableDatabase();
        //Creating a ContentValues object
        ContentValues values = new ContentValues();

        Intent intent = getIntent();

        //adding the key-value pairs to the object
        values.put(NotesContract.NotesEntry.COLUMN_NAME, word.getTitle());
        values.put(NotesContract.NotesEntry.COLUMN_DESCRIPTION, word.getTitleDesc());
        values.put(NotesContract.NotesEntry.COLUMN_DATE, word.getDateCreated());
        values.put(NotesContract.NotesEntry.COLUMN_DATE_VALUE, word.getDateCreatedValue());
        values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED, word.getDateModified());
        values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED_VALUE, word.getDateModifiedValue());
        values.put(NotesContract.NotesEntry.COLUMN_FOLDER_ID,Integer.parseInt(Objects.requireNonNull(intent.getStringExtra("folder_id"))));

        //insert the values to the Folder Table
        db.insert(NotesContract.NotesEntry.TABLE_NAME, null, values);
        db.close();

    }

}