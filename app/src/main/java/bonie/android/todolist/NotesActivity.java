package bonie.android.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import bonie.android.todolist.data.NotesDbHelper;
import bonie.android.todolist.data.NotesContract;

public class NotesActivity extends AppCompatActivity {
    //making a listView ArrayAdapter
    ArrayList<WordFolder> words = new ArrayList<>();
    
    //final variable 
    public static final int new_note = 1;
    public static final int edit_note = 2;
    public int current_position = 0;

    //keeps track of the notes_id
    private String notes_id = null;

    //making a global SQLiteDatabase object
    //Setting up the SQL
    //passing the current activity context
    private SQLiteDatabase db;

    //making a global DbHelper
    private NotesDbHelper notesDbHelper;

    //keeps track of the sorting algorithm
    private int sortBy = 0;

    //global toolbar
    Toolbar toolbar;
    //setting variable for toolbar picker
    private int whichToolbar = 0;

    //global adapter and ListView
    WordAdapter itemAdapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        //making the toolbar
        whichToolbar = 0;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setting up the SQL
        //passing the current activity context
        notesDbHelper = new NotesDbHelper(this);

        //this will create and or open a database to read it
        //similar to --> .open notes.db
        db = notesDbHelper.getReadableDatabase();

        //read from the database. going to insert the values to the ArrayList and set up listView
        readFromDatabase();

        //set up ListView
        itemAdapter = new WordAdapter(this, words);
        listView = (ListView)findViewById(R.id.list_view);
        //set up text if the list happens to be empty
        TextView tv = (TextView)findViewById(R.id.empty_list);
        listView.setAdapter(itemAdapter);
        listView.setEmptyView(tv);

        //making a FAB object
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = (com.google.android.material.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_btn);
        //function call once you click the FAB button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creates a new intent - when the user clicks the + sign it will open a new page
                Intent intent = new Intent(NotesActivity.this, NewNotes.class);
                //-1 means it is a new note folder
                intent.putExtra("notes_id", "-1");
                Intent data = getIntent();
                intent.putExtra("folder_id",data.getStringExtra("id"));
                startActivityForResult(intent, new_note);
            }
        });

        //if the user clicks on a listView item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //creates a new intent - when the user clicks the + sign it will open a new page
                Intent intent = new Intent(NotesActivity.this, NewNotes.class);

                //this intent will be used to retrieved the data
                Intent data = getIntent();

                db = notesDbHelper.getReadableDatabase();
                //allowing the db to be writeable
                db = notesDbHelper.getWritableDatabase();
                String [] projection = {
                        NotesContract.NotesEntry.COLUMN_ID,
                        NotesContract.NotesEntry.COLUMN_DATE_VALUE,
                        NotesContract.NotesEntry.COLUMN_DATE_MODIFIED_VALUE,
                };
                String selection = NotesContract.NotesEntry.COLUMN_FOLDER_ID + "=?";
                String[] selectionArgs = {data.getStringExtra("id")};

                Cursor cursor = db.query(NotesContract.NotesEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
                String index;
                while(cursor.moveToNext()){
                    //getting the notes_id
                    //i = position of the listView clicked

                    if (words.get(i).getDateCreatedValue().equals(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_VALUE))) && words.get(i).getDateModifiedValue().equals(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED_VALUE)))) {
                        index = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_ID));
                        intent.putExtra("notes_id",index);
                        notes_id = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_ID));
                        break;
                    }
                }

                cursor.close();
                db.close();
                //saving the values to the intent for the next activity to access
                intent.putExtra("setTitle", words.get(i).getTitle());
                intent.putExtra("setDesc", words.get(i).getTitleDesc());

                current_position = i;
                startActivityForResult(intent, edit_note);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                //set the visibility on
                itemAdapter.setCheckBoxVisibility(true);
                //set the adapter with the visibility on
                listView.setAdapter(itemAdapter);
                //changing the current Toolbar
                whichToolbar = 1;
                setSupportActionBar(toolbar);
                //setting the back arrow icon for which the user doesn't want to delete anything
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow_24dp);// set drawable icon
                //if the user clicks the back arrow function
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //undoing the checkBox
                        itemAdapter.setCheckBoxVisibility(false);
                        listView.setAdapter(itemAdapter);
                        //setting the toolbar to how it was previous to long Click
                        whichToolbar = 0;
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        setSupportActionBar(toolbar);
                    }
                });
                return true;
            }
        });
    }
    //function is called after startActivityForResults returns
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //setting variables
        String title = null;
        String desc = null;
        String dateCreated = null;
        String dateValue = null;
        String dateModified;
        String dateModifiedValue;

        //if the user edits or add new notes then manipulate the variables
        if(resultCode == Activity.RESULT_OK){
            //receives the data from the NewFolder intent
            title = data.getStringExtra("title");
            desc = data.getStringExtra("desc");

        }

        //if result code is equal to RESULT_OK from the NewFolder Activity and the request code was a new_note
        if(resultCode == Activity.RESULT_OK && requestCode == new_note){

            //getting the current date here
            String pattern = "yyyyMMddHHmmss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            Date date = new Date();
            dateValue = simpleDateFormat.format(date);
            dateModifiedValue = dateValue;

            //get the current date in --> yyyy/mm/dd HH:mm:ss <-- format
            simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
            dateModified = simpleDateFormat.format(date);

            readFromDatabase();
            sort(sortBy);
            modifiedFolderDate(dateModified,dateModifiedValue);
            itemAdapter.setOriginal_list(words);
            listView.setAdapter(itemAdapter);
        }
        //if the user simply edited the notes then you should just save and update the current note
        //don't make a new note
        else if(resultCode == Activity.RESULT_OK && requestCode == edit_note){

            //getting the current date here
            String pattern = "yyyyMMddHHmmss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            Date date = new Date();
            dateModifiedValue = simpleDateFormat.format(date);

            //get the current date in --> yyyy/mm/dd HH:mm:ss <-- format
            simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
            dateModified = simpleDateFormat.format(date);

            readFromDatabase();
            sort(sortBy);
            modifiedFolderDate(dateModified,dateModifiedValue);
            itemAdapter.setOriginal_list(words);
            //update the list
            listView.setAdapter(itemAdapter);
        }

    }

    //This function will read from the SQLite database and fill in the enter when the user opens up the app
    public void readFromDatabase(){

        //if the list of words exist already then delete it
        if(words != null){
            words.clear();
        }
        db = notesDbHelper.getReadableDatabase();
        //allow us to write/create a table using getWritableDatabase
        db = notesDbHelper.getWritableDatabase();

        //Creating a String[] Projection
        String [] projection = {
                NotesContract.NotesEntry.COLUMN_NAME,
                NotesContract.NotesEntry.COLUMN_DESCRIPTION,
                NotesContract.NotesEntry.COLUMN_DATE,
                NotesContract.NotesEntry.COLUMN_DATE_VALUE,
                NotesContract.NotesEntry.COLUMN_DATE_MODIFIED,
                NotesContract.NotesEntry.COLUMN_DATE_MODIFIED_VALUE,
        };

        //creating a selection String
        //This allows us to write WHERE --> folder_id = 'some_int_value'
        String selection = NotesContract.NotesEntry.COLUMN_FOLDER_ID + "=?";

        //get the id # from the intent
        Intent intent = getIntent();

        //--> WHERE folder_id = id
        String [] selectionArgs = {intent.getStringExtra("id")};

        //Using the query() method and returning it to the Cursor object
        //this returns a cursor object that has all rows with only columns of name and description from table folder
        Cursor cursor = db.query(NotesContract.NotesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        //setting up the variables
        WordFolder temp = new WordFolder();

        //this will increment the cursor until it reaches to last
        //cursor starts before the first row. that's why you don't need to write cursor.moveToFirst() prior. otherwise skips item 1
        while(cursor.moveToNext()){
            //get the index of the Column_name and description then
            //get the String value of both name and description
            temp.setTitle(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_NAME)));
            temp.setDesc(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION)));
            temp.setDateCreated(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE)));
            temp.setDateCreatedValue(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_VALUE)));
            temp.setDateModified(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED)));
            temp.setDateModifiedValue(cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED_VALUE)));
            temp.setLocked("0");

            //add both name and desc to the ArrayList
            words.add(new WordFolder(temp));

            //keeps traversing until cursor returns false
        }
        //close the cursor
        cursor.close();
        db.close();
    }
    //creating the option menu for the toolbar --> edit later
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(whichToolbar == 0){
            //inflate menu
            inflater.inflate(R.menu.menu_search, menu);
            MenuItem searchItem = menu.findItem(R.id.search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Type here to Search");
            //initialize search view
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    itemAdapter.getFilter().filter(newText);
                    return true;
                }
            });
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    itemAdapter.getFilter().filter("");
                    return false;
                }
            });
        }
        else{
            inflater.inflate(R.menu.delete_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final Toolbar toolbar = findViewById(R.id.toolbar);

        //using a switch statement so it checks every item and see which one was clicked
        switch (item.getItemId()){
            case(R.id.search):
                return true;
            case(R.id.delete):

                //loop each arrayList and check if the box was checked or not
                for(int i = 0; i < words.size(); ++i){
                    //if checkbox was checked / if true
                    if(words.get(i).getChecked()){
                        addNotesToTrash(words.get(i));
                        deleteNotes(words.get(i).getDateCreatedValue());
                        //deleting the item for good
                        words.remove(i);
                        //if item removed then don't let i change
                        --i;
                    }
                }
                //resetting toolbar back in original state
                whichToolbar = 0;
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                setSupportActionBar(toolbar);
                //setting the gridView again
                itemAdapter.setCheckBoxVisibility(false);
                itemAdapter.setOriginal_list(words);
                listView.setAdapter(itemAdapter);
                return true;
            case(R.id.share):
                int counter = 0;
                int position = 0;

                for(int i = 0; i < words.size(); ++i){
                    if(words.get(i).getChecked()){
                        ++counter;
                        position = i;
                    }
                    if(counter > 1){
                        Toast.makeText(NotesActivity.this, "You can only share 1 item", Toast.LENGTH_SHORT).show();
                        //resetting toolbar back in original state
                        whichToolbar = 0;
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        setSupportActionBar(toolbar);
                        //setting the listView again
                        listView.setAdapter(itemAdapter);
                        return true;
                    }
                }
                if(counter == 0){
                    Toast.makeText(NotesActivity.this, "Please select an item to share", Toast.LENGTH_SHORT).show();
                    //resetting toolbar back in original state
                    whichToolbar = 0;
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    setSupportActionBar(toolbar);
                    //setting the listView again
                    listView.setAdapter(itemAdapter);
                    return true;
                }
                //creating implicit intents
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("plain/text");
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Notes: " + words.get(position).getTitle());
                intent.putExtra(Intent.EXTRA_TEXT,words.get(position).getTitleDesc());
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivity(intent);
                }
                //resetting toolbar back in original state
                whichToolbar = 0;
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                setSupportActionBar(toolbar);
                //setting the listView again
                listView.setAdapter(itemAdapter);
                return true;
            case(R.id.sort_title):
                sortBy = 1;
                sort(sortBy);
                itemAdapter.setOriginal_list(words);
                //setting the gridView again
                itemAdapter.notifyDataSetChanged();
                return true;
            case(R.id.sort_date_created):
                sortBy = 2;
                sort(sortBy);
                itemAdapter.setOriginal_list(words);
                itemAdapter.notifyDataSetChanged();
                return true;
            case(R.id.sort_date_modified):
                sortBy = 3;
                sort(sortBy);
                itemAdapter.setOriginal_list(words);
                itemAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //update later
    public void deleteNotes(String dateValue){
        db = notesDbHelper.getReadableDatabase();
        //receiving permission to update the SQLite database
        db = notesDbHelper.getWritableDatabase();
        //deleting the folder for good
        db.execSQL(" DELETE FROM " + NotesContract.NotesEntry.TABLE_NAME + " WHERE " + NotesContract.NotesEntry.COLUMN_DATE_VALUE + "=\"" + dateValue + "\";");
        db.close();
    }
    //add the current notes to the trash db
    public void addNotesToTrash(WordFolder temp){
        //getting the folder id for later
        Intent intent = getIntent();

        //creating content values and filling up the data to the Trash DB
        ContentValues values = new ContentValues();
        values.put(NotesContract.TrashEntry.COLUMN_NAME, temp.getTitle());
        values.put(NotesContract.TrashEntry.COLUMN_DESCRIPTION, temp.getTitleDesc());
        values.put(NotesContract.TrashEntry.COLUMN_DATE, temp.getDateCreated());
        values.put(NotesContract.TrashEntry.COLUMN_DATE_VALUE, temp.getDateCreatedValue());
        values.put(NotesContract.TrashEntry.COLUMN_DATE_MODIFIED, temp.getDateModified());
        values.put(NotesContract.TrashEntry.COLUMN_DATE_MODIFIED_VALUE, temp.getDateModifiedValue());

        db = notesDbHelper.getReadableDatabase();
        db = notesDbHelper.getWritableDatabase();
        //insert the values to the Trash Table
        db.insert(NotesContract.TrashEntry.TABLE_NAME, null, values);
        db.close();
    }
    //gets the int value and determines which sorting algorithm to go to
    public void sort(int i){
        //if list is empty
        if(words.size() == 0){
            return;
        }
        //creating temp variable
        WordFolder temp = new WordFolder();

        switch (i){
            case(1):
                sortByAlpha(temp);
                break;
            case(2):
                sortByCreated(temp);
                break;
            case(3):
                sortByModified(temp);
                break;
            default:
        }
    }
    //Sort the ArrayList by alphabetical order
    public void sortByAlpha(WordFolder temp){
        //sorting the words array
        for(int i = 0; i < words.size(); ++i){
            for(int j = i + 1; j < words.size(); ++j){
                if(words.get(i).getTitle().compareToIgnoreCase(words.get(j).getTitle()) > 0){
                    //get the temp array to hold one of the values
                    temp.equals(words.get(i));
                    //switch the 2 values together
                    words.get(i).equals(words.get(j));
                    words.get(j).equals(temp);
                }
            }
        }
    }
    public void sortByCreated(WordFolder temp){
        //sorting the words array
        for(int i = 0; i < words.size(); ++i){
            for(int j = i + 1; j < words.size(); ++j){
                if(words.get(i).getDateCreatedValue().compareToIgnoreCase(words.get(j).getDateCreatedValue()) > 0){
                    //get the temp array to hold one of the values
                    temp.equals(words.get(i));
                    //switch the 2 values together
                    words.get(i).equals(words.get(j));
                    words.get(j).equals(temp);
                }
            }
        }
    }
    public void sortByModified(WordFolder temp){
        //sorting the words array
        for(int i = 0; i < words.size(); ++i){
            for(int j = i + 1; j < words.size(); ++j){
                if(words.get(j).getDateModifiedValue().compareToIgnoreCase(words.get(i).getDateModifiedValue()) > 0){
                    //get the temp array to hold one of the values
                    temp.equals(words.get(i));
                    //switch the 2 values together
                    words.get(i).equals(words.get(j));
                    words.get(j).equals(temp);
                }
            }
        }
    }
    //after modifying and or creating a new notes; we must modified the folders modified date as well
    public void modifiedFolderDate(String date, String dateValue){
        db = notesDbHelper.getReadableDatabase();
        db = notesDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED,date);
        values.put(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED_VALUE,dateValue);

        String selection = NotesContract.FolderEntry.COLUMN_ID + "=?";

        //get the folder_id column value
        Intent intent = getIntent();
        String[] selectionArgs = new String[]{intent.getStringExtra("id")};

        //update to the database
        db.update(NotesContract.FolderEntry.TABLE_NAME,values,selection,selectionArgs);

        //saving value
        intent = new Intent();
        intent.putExtra("date_modified", date);
        intent.putExtra("date_modified_value",dateValue);

        //close database
        db.close();
        setResult(RESULT_OK,intent);

    }

}
