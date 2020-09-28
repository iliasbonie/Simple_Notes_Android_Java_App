package bonie.android.todolist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import java.util.ArrayList;
import java.util.Collection;

//want to create our own custom ArrayAdapter. Going to extends the base class ArrayAdapter and hold our
//Word object
public class WordAdapter extends ArrayAdapter<WordFolder> implements Filterable{

    //variable responsible for making checkbox visible or not
    private boolean displayCheckBox;

    //the original_list will have it's own independent copy of the list items
    private ArrayList<WordFolder> original_list = new ArrayList<WordFolder>();
    //new_list will point to the word list in the adapter
    private ArrayList<WordFolder> new_list;

    private GridView gridView;
    private ListView listView;
    private int view = 2;


    //constructor - it takes the context and the list of words
    WordAdapter(Context context, ArrayList<WordFolder> word){
        super(context, 0, word);

        //creating a copy of the ArrayList containing all the folders name
        this.original_list.addAll(word);
        //pointing to the current list items
        this.new_list = word;

    }
    //sets the visibility of the checkBox
    public void setCheckBoxVisibility(boolean visible){
        this.displayCheckBox = visible;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.folder_view, parent, false);
        }

        //getting the checkBox view id
        CheckBox checkBox = (CheckBox) listItemView.findViewById(R.id.check_box);

        checkBox.setVisibility(displayCheckBox ? View.VISIBLE : View.GONE);

        //getting the Image view id
        ImageView imageView = (ImageView) listItemView.findViewById(R.id.psw_lock);

        listView = (ListView) listItemView.findViewById(R.id.list_view);
        gridView = (GridView) listItemView.findViewById(R.id.grid_view);



        //Getting the current word
        final WordFolder currentWord = getItem(position);

        //making the 3 text view to match our word_folder.xml
        TextView date_created = (TextView) listItemView.findViewById(R.id.date_created);

        TextView title = (TextView) listItemView.findViewById(R.id.title);

        TextView desc = (TextView) listItemView.findViewById(R.id.desc);

        //using the setText to get the text and set it in the textView
        date_created.setText(currentWord.getDateCreated());

        title.setText(currentWord.getTitle());

        desc.setText(currentWord.getTitleDesc());

        if(original_list.get(position).getLocked().equals("1")){
            imageView.setVisibility(View.VISIBLE);
        }else{
            imageView.setVisibility(View.INVISIBLE);
        }
        //call automatically when checkbox is changed
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            //compound button = the view of the button
            //b = the new state of the checkbox
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //set the value of the checkbox to the CurrentWord
                currentWord.setChecked(b);
            }
        });

        return listItemView;

    }

    //setter - whenever an item is deleted, update this in the word adapter
    public void setOriginal_list(ArrayList<WordFolder> update){
        original_list.clear();
        original_list.addAll(update);
    }
    //change layout
    public void change_layout(int num){
        if(num != 1 && num != 2){
            return;
        }
        view = num;
    }
    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                //new list which contains only filtered items
                ArrayList<WordFolder> filteredList = new ArrayList<>();
                //if input in the search bar is zero/nothing
                if(charSequence == null || charSequence.length() == 0){
                    //pour the entire list in
                    filteredList.addAll(original_list);
                }
                else{
                    //lowercase the string so case doesn't matter
                    String filterPattern = charSequence.toString().toLowerCase().trim();

                    //if it contains the word/letter then add it to the filtered list
                    int i = 0;
                    for(WordFolder item : original_list){
                        if(item.getTitle().toLowerCase().contains(filterPattern)){
                            filteredList.add(item);
                        }
                    }
                }

                //make a new variable and add all of the filtered words and return it
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override

            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //clear the entire list and add it with all the filtered items
                new_list.clear();

                new_list.addAll((ArrayList<WordFolder>) filterResults.values);

                //notify the adapter that the list has changed.
                notifyDataSetChanged();

            }

        };

        return filter;
    }

}
