package com.defaup.newsgateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.graphics.Color;
import android.os.Bundle;
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
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity
{
    private Menu main_menu = null;
    private HashMap<String, ArrayList<Source>> sourcesMap = new HashMap<>();

    // Left drawer handle variables
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ArrayList<String> drawerItemList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setCustomActionBar(); // need to be done before adding the drawer to the action bar

        // Drawer Layout Menu
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerListView = findViewById(R.id.drawerList);
        drawerListView.setAdapter(new ArrayAdapter<>(this,R.layout.drawer_layout_item, drawerItemList));
        drawerListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onDrawerItemClicked(position);
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

    // Get reference to Right List Item Menu to change it dynamically
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu); //don't have the about without this
        main_menu = menu;
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
        String category = item.getTitle().toString();
        updateDrawerItemList(category);

        return super.onOptionsItemSelected(item);
    }

    // Fill LEFT Menu with news sources for the given category
    private void updateDrawerItemList(String category)
    {
        // The category that we get here comes from the hashmap so nullptr exception
        drawerItemList.clear();
        ArrayList<Source> sources = sourcesMap.get(category);
        for(Source source : sources)
            drawerItemList.add(source.getName());
        ((ArrayAdapter) drawerListView.getAdapter()).notifyDataSetChanged();
    }
    // LEFT Menu Handle
    private void onDrawerItemClicked(int position)
    {
        drawerLayout.closeDrawer(drawerListView);
    }



/*** Post Async ***/

    // Set HashMap with Key= Category of sources, Value=Source objects
    // Set Menu Item list with categories
    public void onPostSourceDownload(HashMap<String, ArrayList<Source>> sourcesMap)
    {
        this.sourcesMap.clear();
        this.sourcesMap.putAll(sourcesMap);
        for(String key: sourcesMap.keySet())
        {
            main_menu.add(key);
        }
    }
}
