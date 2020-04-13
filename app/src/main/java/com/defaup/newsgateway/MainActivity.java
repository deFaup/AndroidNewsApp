package com.defaup.newsgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
    private Menu main_menu = null;
    private HashMap<String, ArrayList<Source>> sourcesMap = new HashMap<>();

    // Left drawer handle variables
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ArrayList<String> drawerItemList = new ArrayList<>();

    private String chosenCategory = "";
    private static final String TAG = "Greg_MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        setCustomActionBar(); // need to be done before adding the drawer to the action bar

        // Drawer Layout Menu
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerListView = findViewById(R.id.drawerList);
        drawerListView.setAdapter(new ArrayAdapter<>(this,R.layout.drawer_layout_item, drawerItemList));
        drawerListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onLeftMenuItemClicked(position);
            }
        });
        actionBarDrawerToggle = new ActionBarDrawerToggle(   // <== Important!
                this,          /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        );
        if (getSupportActionBar() != null) {  // <== Important! Make the drawer visible
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Download news sources
        if (savedInstanceState == null)
            new AsyncSourceDownload(this).execute();
    }

    private void setCustomActionBar()
    {
        TextView title = new TextView(this);
        title.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        //NOT working for action bar //title.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        title.setText(getString(R.string.app_name));
        title.setTextColor(Color.BLACK);
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(22);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(title);
    }

/**** MENUS ****/

    // Create Right Menu dynamically if possible
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.menu_main, menu); //don't have the about without this
        main_menu = menu;

        if(!sourcesMap.isEmpty())
            updateRightMenu(this.sourcesMap);
        return super.onCreateOptionsMenu(menu);
    }

    // RIGHT Menu handle (menu item)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // Return if LEFT Menu (drawer menu) is at the origin of the call
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        if (item.getItemId() == R.id.menuAbout)
            return true;
        //TODO do the about activity with credentials to NEWS API

        // Update RIGHT menu
        chosenCategory = item.getTitle().toString();
        updateLeftMenu(chosenCategory);

        return super.onOptionsItemSelected(item);
    }

    // set news sources for the given category
    private void updateLeftMenu(String category)
    {
        // The category that we get here comes from the hashmap so nullptr exception
        drawerItemList.clear();
        ArrayList<Source> sources = sourcesMap.get(category);
        for(Source source : sources)
            drawerItemList.add(source.getName());
        ((ArrayAdapter) drawerListView.getAdapter()).notifyDataSetChanged();
    }

    private void onLeftMenuItemClicked(int position)
    {
        drawerLayout.closeDrawer(drawerListView);
    }
    
    // After onRestoreState has occured we need to set back the menu to its saved state
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG, "onPostCreate: ");
        actionBarDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: ");
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }


/*** Post Async ***/
    // Set HashMap with Key= Category of sources, Value=Source objects
    // Set Menu Item list with categories
    public void onPostSourceDownload(HashMap<String, ArrayList<Source>> sourcesMap)
    {
        Log.d(TAG, "onPostSourceDownload: ");
        updateHashMap(sourcesMap);
        updateRightMenu(sourcesMap);
    }
    private void updateHashMap(HashMap<String, ArrayList<Source>> sourcesMap)
    {
        this.sourcesMap.clear();
        this.sourcesMap.putAll(sourcesMap);
    }
    private void updateRightMenu(HashMap<String, ArrayList<Source>> sourcesMap)
    {
        for(String key: sourcesMap.keySet())
            main_menu.add(key);
    }


/*** Save state ***/
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putString(getString(R.string.NEWS_CATEGORY), chosenCategory);
        outState.putSerializable(getString(R.string.HASHMAP), sourcesMap);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "restoreInstanceState: ");
        chosenCategory = savedInstanceState.getString(getString(R.string.NEWS_CATEGORY));
        updateHashMap(
                (HashMap<String, ArrayList<Source>>) savedInstanceState.getSerializable(getString(R.string.HASHMAP)));

        // build right menu not possible
        // build left menu OK
        if(!chosenCategory.isEmpty())
            updateLeftMenu(chosenCategory);
    }


/*** Boot order ***/
/*
    onCreate:
    onStart:
    onPostCreate:
    onCreateOptionsMenu:
    onPostSourceDownload:

    // screeen is tilted
    onSaveInstanceState:

    onCreate:
    onStart:
    onRestoreInstanceState:
    onPostCreate:
    onCreateOptionsMenu: -- menu is created after restore ! so we can't fill the menu dynamically the menu before that point
    onPostSourceDownload:

    To restore menu:
    onSaveInstanceState: save the hashmap and the category that was chosen by the user
    onRestoreInstanceState: restore both of them, restore the left menu if the user had chosen a category
    onCreateOptionsMenu: restore the menu if the hashmap is not empty
*/
}
