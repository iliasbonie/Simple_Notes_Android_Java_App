package bonie.android.todolist;

//This WordFolder just simply defines a Word. It takes 2 Strings - a title and sub description
//It's methods are just getters
public class WordFolder {
    private String mTitle;
    private String mTitleDesc;
    private String mDateCreated;
    private String mDateCreatedValue;
    private String mDateModified;
    private String mDateModifiedValue;

    private boolean isChecked;
    private String isLocked;

    //constructor

    WordFolder(){
        mTitle = null;
        mTitleDesc = null;
        mDateCreated = null;
        mDateCreatedValue = null;
        mDateModified = null;
        mDateModifiedValue = null;
        isChecked = false;
        isLocked = null;
    }
    WordFolder(String Title, String TitleDesc, String Date, String DateValue, String DateModified, String DateModifiedValue, String Lock){
        mTitle = Title;
        mTitleDesc = TitleDesc;
        mDateCreated = Date;
        mDateCreatedValue = DateValue;
        mDateModified = DateModified;
        mDateModifiedValue = DateModifiedValue;
        isChecked = false;
        isLocked = Lock;
    }
    //copy constructor
    WordFolder(WordFolder copy){
        mTitle = copy.getTitle();
        mTitleDesc = copy.getTitleDesc();
        mDateCreated = copy.getDateCreated();
        mDateCreatedValue = copy.getDateCreatedValue();
        mDateModified = copy.getDateModified();
        mDateModifiedValue = copy.getDateModifiedValue();
        isChecked = copy.getChecked();
        isLocked = copy.getLocked();
    }
    //getters
    public String getTitle(){
        return mTitle;
    }
    public String getTitleDesc(){
        return mTitleDesc;
    }
    public String getDateCreated(){return mDateCreated;}
    public String getDateCreatedValue(){return mDateCreatedValue;}
    public String getDateModified(){return mDateModified;}
    public String getDateModifiedValue(){return mDateModifiedValue;}
    public boolean getChecked(){return isChecked;}
    public String getLocked(){return isLocked;}


    //setters
    public void setTitle(String Title){mTitle = Title;}
    public void setDesc(String Desc){mTitleDesc = Desc;}
    public void setDateCreated(String Date){mDateCreated = Date;}
    public void setDateCreatedValue(String Date){mDateCreatedValue = Date;}
    public void setDateModified(String Date){mDateModified = Date;}
    public void setDateModifiedValue(String Date){mDateModifiedValue = Date;}
    public void setChecked(boolean check){isChecked = check;}
    public void setLocked(String lock){isLocked = lock;}

    //equal function
    public void equals(WordFolder obj){
        mTitle = obj.getTitle();
        mTitleDesc = obj.getTitleDesc();
        mDateCreated = obj.getDateCreated();
        mDateCreatedValue = obj.getDateCreatedValue();
        mDateModified = obj.getDateModified();
        mDateModifiedValue = obj.getDateModifiedValue();
        isLocked = obj.getLocked();
    }
}
