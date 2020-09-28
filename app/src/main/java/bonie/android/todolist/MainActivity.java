package bonie.android.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import bonie.android.todolist.data.NotesDbHelper;
import bonie.android.todolist.data.NotesContract;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AlertDialog dialog;
    private EditText popupFolderName;

    private DrawerLayout drawer;
    //making a global ArrayList object
    ArrayList<WordFolder> words = new ArrayList<>();

    //making a global SQLiteDatabase object
    //Setting up the SQL
    private SQLiteDatabase db;

    //making a global DbHelper
    private NotesDbHelper notesDbHelper;

    //setting variable for toolbar picker
    private int whichToolbar = 0;

    //remembers which sorting to go to
    //1 = title, 2 = created, 3 = modified, 0 = default
    private int sortBy = 0;

    //making Toolbar, GridView, and custom Adapter all global variables
    Toolbar toolbar;
    WordAdapter itemAdapter;
    GridView gridView;

    //final variable
    public static final int folder_lock = 1;
    public static final int new_password = 2;
    public static final int folder_click = 3;
    public static final int folder_delete_lock = 4;

    //getting the current position of the arrayList
    public int current_position = 0;

    //navigation item selected - when one of the options in the drawer nav gets clicked
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.nav_lock:
                db = notesDbHelper.getWritableDatabase();
                Cursor cursor = db.query(NotesContract.PasswordEntry.TABLE_NAME, null,null,null,null,null,null);

                //if there is no rows in the database then go straight to setPassword activity
                if(cursor.getCount() == 0){
                    intent = new Intent(this, SetPasswordActivity.class);
                    startActivity(intent);
                }else{
                    //if there is a row then you must input password before going to activity
                    intent = new Intent(this, PasswordActivity.class);
                    startActivityForResult(intent, new_password);
                }
                cursor.close();
                break;
            case R.id.about:
                intent = new Intent(this, about.class);
                startActivity(intent);
                break;
            case R.id.trash:
                intent = new Intent(this, TrashActivity.class);
                startActivity(intent);
            default:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //onBackPressed - when you click back to the previous activity. if drawer is open then it closes
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
     }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //making the toolbar
        whichToolbar = 0;
        toolbar = findViewById(R.id.toolbar);

        //getting the drawer Layout ID
        drawer = findViewById(R.id.drawer_layout);
        //getting the nav view ID
        NavigationView navigationView = findViewById(R.id.nav_view);
        //setting the nav view to this current Activity
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        //what happens when the hamburger icon gets clicked
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calls the drawer navigation - appearing to the screen

                if(!drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.openDrawer(GravityCompat.START);
                }
                else{
                    drawer.closeDrawer(GravityCompat.END);
                }
            }
        });

        //Setting up the SQL
        //passing the current activity context
        notesDbHelper = new NotesDbHelper(this);

        //this will create and or open a database to read it
        //similar to --> .open notes.db
        db = notesDbHelper.getReadableDatabase();

        //calls function and read input from the SQLite database only to fill in to the grid-view
        readFromDatabase();
        //set up the gridView
        itemAdapter = new WordAdapter(this, words);
        gridView = findViewById(R.id.grid_view);
        setEmptyView(true);


        itemAdapter.setCheckBoxVisibility(false);
        gridView.setAdapter(itemAdapter);
        //making a FAB object
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab_btn);
        //function call once you click the FAB button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewFolderDialog();
            }
        });

        //setOnItemClickListener - this will call a function if one of the gridView items get clicked on
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                db = notesDbHelper.getReadableDatabase();
                current_position = i;
                //setFolderLock();
                if(words.get(i).getLocked().equals("1")){
                    Intent intent = new Intent(MainActivity.this, PasswordActivity.class);
                    startActivityForResult(intent, folder_lock);
                }
                else {
                    setEmptyView(true);
                    //creates a new intent - goes to the NotesActivity when clicked
                    Intent intent = new Intent(MainActivity.this, NotesActivity.class);

                    //trying to get unique id
                    String created = words.get(i).getDateCreatedValue();
                    String modified = words.get(i).getDateModifiedValue();
                    String index;

                    //allow us to write/create a table using getWritableDatabase
                    db = notesDbHelper.getWritableDatabase();

                    //traverse the entire folder table and get the 'id' that the user clicked on and save the folder id to the intent for the next activity to use
                    Cursor cursor = db.query(NotesContract.FolderEntry.TABLE_NAME, null, null, null, null, null, null);
                    while (cursor.moveToNext()) {
                        if (created.equals(cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_VALUE))) && modified.equals(cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED_VALUE)))) {
                            index = cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_ID));
                            intent.putExtra("id", index);
                            break;
                        }
                    }
                    cursor.close();
                    db.close();
                    startActivityForResult(intent, folder_click);
                }
            }

        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                setEmptyView(true);
                //set the visibility on
                itemAdapter.setCheckBoxVisibility(true);
                //set the adapter with the visibility on
                //itemAdapter.notifyDataSetChanged();
                gridView.setAdapter(itemAdapter);
                //changing the current Toolbar
                whichToolbar = 1;
                setSupportActionBar(toolbar);
                //setting the back arrow icon for which the user doesn't want to delete anything
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow_24dp);// set drawable icon
                //if the user clicks the back arrow function
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //undoing the checkBox
                        itemAdapter.setCheckBoxVisibility(false);
                        //setting the checkbox back to false on the objects
                        resetChecked();
                        gridView.setAdapter(itemAdapter);
                        //setting the toolbar to how it was previous to long Click
                        whichToolbar = 0;
                        //setting back to the hamburger menu
                        setSupportActionBar(toolbar);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

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
        db = notesDbHelper.getReadableDatabase();
        setEmptyView(true);
        //if result code is equal to RESULT_OK from the NewFolder Activity
        if(resultCode == Activity.RESULT_OK && requestCode == folder_lock){
            db = notesDbHelper.getReadableDatabase();
            setEmptyView(true);
            //creates a new intent - goes to the NotesActivity when clicked
            Intent intent = new Intent(MainActivity.this, NotesActivity.class);

            //trying to get unique id
            String created = words.get(current_position).getDateCreatedValue();
            String modified = words.get(current_position).getDateModifiedValue();
            String index;

            //allow us to write/create a table using getWritableDatabase
            db = notesDbHelper.getWritableDatabase();

            //traverse the entire folder table and get the 'id' that the user clicked on and save the folder id to the intent for the next activity to use
            Cursor cursor = db.query(NotesContract.FolderEntry.TABLE_NAME, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                if (created.equals(cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_VALUE))) && modified.equals(cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED_VALUE)))) {
                    index = cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_ID));
                    intent.putExtra("id", index);
                    break;
                }
            }
            cursor.close();
            db.close();
            startActivity(intent);
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == new_password){
            Intent intent = new Intent(MainActivity.this, SetPasswordActivity.class);
            startActivity(intent);
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == folder_click){
            words.get(current_position).setDateModified(data.getStringExtra("date_modified"));
            words.get(current_position).setDateModifiedValue(data.getStringExtra("date_modified_value"));

            sort(sortBy);
            itemAdapter.setOriginal_list(words);
            itemAdapter.notifyDataSetChanged();
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == folder_delete_lock){
            db = notesDbHelper.getReadableDatabase();
            db = notesDbHelper.getWritableDatabase();
            //loop each arrayList and check if the box was checked or not
            for(int i = 0; i < words.size(); ++i){
                //if checkbox was checked / if true
                if(words.get(i).getChecked()){
                    delete_folder(words.get(i).getDateCreatedValue(), words.get(i).getDateModifiedValue());
                    //deleting the item for good
                    words.remove(i);
                    //if item removed then don't let i change
                    --i;
                }
            }
            //resetting toolbar back in original state
            itemAdapter.setCheckBoxVisibility(false);
            whichToolbar = 0;
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            setSupportActionBar(toolbar);
            itemAdapter.setOriginal_list(words);
            //setting the gridView again
            itemAdapter.notifyDataSetChanged();

            //closing the database
            db.close();
        }
    }

    //This function will read from the SQLite database and fill in the enter when the user opens up the app
    public void readFromDatabase(){

        //open database
        db = notesDbHelper.getReadableDatabase();
        //allow us to write/create a table using getWritableDatabase
        db = notesDbHelper.getWritableDatabase();

        //Creating a String[] Projection
        String [] projection = {
                NotesContract.FolderEntry.COLUMN_NAME,
                NotesContract.FolderEntry.COLUMN_DESCRIPTION,
                NotesContract.FolderEntry.COLUMN_DATE,
                NotesContract.FolderEntry.COLUMN_DATE_VALUE,
                NotesContract.FolderEntry.COLUMN_DATE_MODIFIED,
                NotesContract.FolderEntry.COLUMN_DATE_MODIFIED_VALUE,
                NotesContract.FolderEntry.COLUMN_LOCK,
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
            index = cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_NAME);
            temp.setTitle(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DESCRIPTION);
            temp.setDesc(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE);
            temp.setDateCreated(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_VALUE);
            temp.setDateCreatedValue(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED);
            temp.setDateModified(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED_VALUE);
            temp.setDateModifiedValue(cursor.getString(index));
            index = cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_LOCK);
            temp.setLocked(cursor.getString(index));

            //add both name and desc to the ArrayList
            words.add(new WordFolder(temp));

            //keeps traversing until cursor returns false
        }

        //close the cursor
        cursor.close();

        //close database
        db.close();
    }

    //good
    //this function will add to the SQLite database for future use
    public void addToDatabase(WordFolder temp){

        //Creating a ContentValues object
        ContentValues values = new ContentValues();

        //adding the key-value pairs to the object
        values.put(NotesContract.FolderEntry.COLUMN_NAME, temp.getTitle());
        values.put(NotesContract.FolderEntry.COLUMN_DESCRIPTION, temp.getTitleDesc());
        values.put(NotesContract.FolderEntry.COLUMN_DATE, temp.getDateCreated());
        values.put(NotesContract.FolderEntry.COLUMN_DATE_VALUE, temp.getDateCreatedValue());
        values.put(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED, temp.getDateModified());
        values.put(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED_VALUE, temp.getDateModifiedValue());
        values.put(NotesContract.FolderEntry.COLUMN_LOCK, temp.getLocked());

        db = notesDbHelper.getReadableDatabase();
        db = notesDbHelper.getWritableDatabase();
        //insert the values to the Folder Table
        db.insert(NotesContract.FolderEntry.TABLE_NAME, null, values);
        db.close();
    }
    //creating the option menu for the toolbar --> edit later
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // initialize menu inflater
        MenuInflater inflater = getMenuInflater();
        if(whichToolbar == 0){
            //inflate menu
            inflater.inflate(R.menu.menu_search, menu);
            MenuItem searchItem = menu.findItem(R.id.search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Type here to Search");

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
            case(R.id.delete):
                //open database
                db = notesDbHelper.getReadableDatabase();

                setEmptyView(true);
                //set flag for 1 time use
                int flag = 0;
                for(int i = 0; i < words.size(); ++i){
                    //if one of the requested folders is locked. need to go to the password activity
                    if(words.get(i).getLocked().equals("1") && words.get(i).getChecked()){
                        flag = 1;
                        break;
                    }
                }
                if(flag == 1){
                    db.close();
                    //if there is a row then you must input password before going to activity
                    Intent intent = new Intent(this, PasswordActivity.class);
                    startActivityForResult(intent, folder_delete_lock);
                }
                else{
                    //loop each arrayList and check if the box was checked or not
                    for(int i = 0; i < words.size(); ++i){
                        //if checkbox was checked / if true
                        if(words.get(i).getChecked()){
                            delete_folder(words.get(i).getDateCreatedValue(), words.get(i).getDateModifiedValue());
                            //deleting the item for good
                            words.remove(i);
                            //if item removed then don't let i change
                            --i;
                        }
                    }
                    //resetting toolbar back in original state
                    itemAdapter.setCheckBoxVisibility(false);
                    whichToolbar = 0;
                    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
                    setSupportActionBar(toolbar);
                    itemAdapter.setOriginal_list(words);
                    //setting the gridView again
                    itemAdapter.notifyDataSetChanged();

                    //closing the database
                    db.close();
                }
                return true;
            case(R.id.lock):
                //open database
                db = notesDbHelper.getReadableDatabase();
                //setting up variables
                String selection;
                String [] selectionArgs;
                String [] projection = {
                        NotesContract.FolderEntry.COLUMN_ID,
                        NotesContract.FolderEntry.COLUMN_DATE_VALUE,
                };
                //allow us to write/create a table using getWritableDatabase
                db = notesDbHelper.getWritableDatabase();
                Cursor cursor;
                String index;
                //loop each arrayList and check if the box was checked or not
                for(int i = 0; i < words.size(); ++i){
                    //set the folder lock if the box is checked
                    if(words.get(i).getChecked()){
                        //if so then set it
                        words.get(i).setLocked("1");
                        //get the folder_id and looping through to match
                        cursor = db.query(NotesContract.FolderEntry.TABLE_NAME,projection,null,null,null,null,null);
                        while(cursor.moveToNext()){
                            if(cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_VALUE)).equals(words.get(i).getDateCreatedValue())){
                                index = cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_ID));
                                selection = NotesContract.FolderEntry.COLUMN_ID + " = ?";
                                selectionArgs = new String[]{index};
                                ContentValues values = new ContentValues();
                                values.put(NotesContract.FolderEntry.COLUMN_LOCK, words.get(i).getLocked());
                                db.update(NotesContract.FolderEntry.TABLE_NAME, values,selection, selectionArgs);
                                break;
                            }
                        }
                        cursor.close();
                    }
                }
                itemAdapter.setOriginal_list(words);
                itemAdapter.notifyDataSetChanged();

                //close
                db.close();
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

    //deleting item from database
    public void delete_folder(String created, String modified){
        //receiving permission to update the SQLite database
        db = notesDbHelper.getWritableDatabase();

        //traverse the entire folder table and get the 'id' that the user clicked on and save the folder id to the intent for the next activity to use
        String index = null;
        Cursor cursor = db.query(NotesContract.FolderEntry.TABLE_NAME, null, null,null,null,null,null);
        while(cursor.moveToNext()){
            if(created.equals(cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_VALUE))) && modified.equals(cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_DATE_MODIFIED_VALUE)))){
                index = cursor.getString(cursor.getColumnIndex(NotesContract.FolderEntry.COLUMN_ID));
                break;
            }
        }
        cursor.close();
        //going to delete the notes from the folder if any
        delete_notes(index);

        //deleting the folder for good
        db.execSQL(" DELETE FROM " + NotesContract.FolderEntry.TABLE_NAME + " WHERE " + NotesContract.FolderEntry.COLUMN_DATE_VALUE + "=\"" + created + "\";");
        //db.close();
    }
    //deleting the notes from inside the folder
    public void delete_notes(String index){
        db = notesDbHelper.getWritableDatabase();
        //deleting all the notes that shares the same folder id
        db.execSQL(" DELETE FROM " + NotesContract.NotesEntry.TABLE_NAME + " WHERE " + NotesContract.NotesEntry.COLUMN_FOLDER_ID + "=\"" + index + "\";");
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
            gridView.setEmptyView(empty);
        }else{
            empty.setVisibility(View.GONE);
            gridView.setEmptyView(noResults);
        }
    }

    //responsible for the popup window. all functionality goes here
    public void createNewFolderDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View popupFolder = getLayoutInflater().inflate(R.layout.folder_popup,null);
        popupFolderName = popupFolder.findViewById(R.id.folder_name);
        Button popupAdd = popupFolder.findViewById(R.id.add_button);
        Button popupCancel = popupFolder.findViewById(R.id.cancel_button);

        dialogBuilder.setView(popupFolder);
        dialog = dialogBuilder.create();
        dialog.show();

        popupCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        popupAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //define save button

                //make TextView variables and cast the contents to a string and save it to a String variable
                String title = popupFolderName.getText().toString();

                //getting the current date here
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = new Date();
                String dateCreatedValue = simpleDateFormat.format(date);

                //get the current date in --> month day, year  <-- format
                Calendar calendar = Calendar.getInstance();
                String dateCreated = DateFormat.getDateInstance().format(calendar.getTime());

                //get the current date in --> yyyy/mm/dd HH:mm:ss <-- format
                simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
                String dateModified = simpleDateFormat.format(date);

                WordFolder temp = new WordFolder(title,null,dateCreated,dateCreatedValue,dateModified,dateCreatedValue,"0");
                //this function will add to the database for future use
                addToDatabase(temp);

                //receives the data from the NewFolder intent and adding it to the ArrayList
                words.add(new WordFolder(temp));

                sort(sortBy);
                itemAdapter.setOriginal_list(words);
                itemAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }
    //setting all the checks back to false
    public void resetChecked(){
        for(int i = 0; i < words.size(); ++i){
            words.get(i).setChecked(false);
        }
    }
}