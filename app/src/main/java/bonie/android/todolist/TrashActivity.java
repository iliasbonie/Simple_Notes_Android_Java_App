package bonie.android.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

import bonie.android.todolist.data.NotesContract;
import bonie.android.todolist.data.NotesDbHelper;

public class TrashActivity extends AppCompatActivity {
    //making a listView ArrayAdapter
    ArrayList<WordFolder> words = new ArrayList<>();
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
        setContentView(R.layout.activity_trash);

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
        setEmptyView(true);

    }

    //This function will read from the SQLite database and fill in the enter when the user opens up the app
    public void readFromDatabase(){

        //open database
        db = notesDbHelper.getReadableDatabase();
        //allow us to write/create a table using getWritableDatabase
        db = notesDbHelper.getWritableDatabase();

        //Creating a String[] Projection
        String [] projection = {
                NotesContract.TrashEntry.COLUMN_NAME,
                NotesContract.TrashEntry.COLUMN_DESCRIPTION,
                NotesContract.TrashEntry.COLUMN_DATE,
                NotesContract.TrashEntry.COLUMN_DATE_VALUE,
                NotesContract.TrashEntry.COLUMN_DATE_MODIFIED,
                NotesContract.TrashEntry.COLUMN_DATE_MODIFIED_VALUE,
        };

        //Using the query() method and returning it to the Cursor object
        //this returns a cursor object that has all rows with only columns of name and description from table folder
        Cursor cursor = db.query(NotesContract.FolderEntry.TABLE_NAME, projection, null, null, null, null, null);

        //setting up the variables
        int index;
        WordFolder temp = new WordFolder();
        //this will increment the cursor until it reaches to last
        //cursor starts before the first row. thats why you don't need to write cursor.moveToFirst() prior. otherwise skips item 1

        while(cursor.moveToNext()){
            //get the index of the Column_name and description
            //get the String value of both name and description
            index = cursor.getColumnIndex(NotesContract.TrashEntry.COLUMN_NAME);
            temp.setTitle(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.TrashEntry.COLUMN_DESCRIPTION);
            temp.setDesc(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.TrashEntry.COLUMN_DATE);
            temp.setDateCreated(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.TrashEntry.COLUMN_DATE_VALUE);
            temp.setDateCreatedValue(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.TrashEntry.COLUMN_DATE_MODIFIED);
            temp.setDateModified(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.TrashEntry.COLUMN_DATE_MODIFIED_VALUE);
            temp.setDateModifiedValue(cursor.getString(index));

            //add both name and desc to the ArrayList
            words.add(new WordFolder(temp));

            //keeps traversing until cursor returns false
        }

        //close the cursor
        cursor.close();

        //close database
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
                    setEmptyView(false);

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

    //this function will call if one of the items in the option menu gets clicked on
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //using a switch statement so it checks every item and see which one was clicked
        switch (item.getItemId()){
            case(R.id.search):
                setEmptyView(false);
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
    //Sort by date created
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
    //Sort by recent date modified
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
    public void setEmptyView(boolean emptyList){
        TextView empty = findViewById(R.id.empty_list);
        TextView noResults = findViewById(R.id.empty_result);
        if(emptyList){
            noResults.setVisibility(View.GONE);
            listView.setEmptyView(empty);
        }else{
            empty.setVisibility(View.GONE);
            listView.setEmptyView(noResults);
        }
    }

}
