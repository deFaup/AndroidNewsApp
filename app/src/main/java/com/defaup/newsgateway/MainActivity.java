package com.defaup.newsgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
{
    private Menu main_menu = null;
    //TreeMap with Key=  Categories of news , Value=List of media sources for this category
    private Map<String, ArrayList<Source>> sourcesMap = new TreeMap<>();;

    // media category picked by the user
    private String chosenCategory = "";

    // Left aka Drawer Layout Menu (menu with news media sources specific to chosenCategory)
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private List<Source> drawerItemList = new ArrayList<>();

    private static final String TAG = "Greg_MainActivity";

    private List<Fragment> fragments;
    private PageViewerAdapter pageAdapter;
    private ViewPager pager;

    // Upon receipt of a broadcast msg from our service articles List is set
    private NewsReceiver newsReceiver;
    private List<Article> articles;

    private ImageView background;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        setCustomActionBar(); // need to be done before adding the drawer to the action bar

        createLeftMenu();

        // Start service: MainAct broadcast to its service the news source whose articles need to
        // be downloaded. When this is done the service do the reverse and send back the articles
        Intent intent = new Intent(MainActivity.this, NewsService.class);
        intent.putExtra("RECEIVER_INTENT", getString(R.string.INTENT_TO_SERVICE));
        intent.putExtra("BROADCAST_INTENT", getString(R.string.INTENT_TO_MAIN));
        startService(intent);

        // MainActiviy receiver: NewsReceiver
        newsReceiver = new NewsReceiver();

        // ViewPager to swipe between fragments (==articles)
        // PageAdapter to handle the swiping (with the FragmentManager)
        fragments = new ArrayList<>();
        pageAdapter = new PageViewerAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.pager);
        pager.setAdapter(pageAdapter);

        // Background (background present while no articles have been displayed)
        background = findViewById(R.id.background);

        // Download news sources (need to be completed after onCreateOptionsMenu otherwise nullptr excep on main_menu)
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

    // Create Right Menu dynamically (and fill it if possible)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.menu_main, menu); //don't have the about item without this
        main_menu = menu;

        // Restore the menu after rotation
        if(!sourcesMap.isEmpty())
            updateRightMenu(this.sourcesMap);
        return super.onCreateOptionsMenu(menu);
    }
    private void updateRightMenu(Map<String, ArrayList<Source>> sourcesMap)
    {
        for(String key: sourcesMap.keySet())
            main_menu.add(key);
    }
    // RIGHT Menu handle (menu item)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // Return if LEFT Menu is at the origin of the call
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        if (item.getItemId() == R.id.menuAbout)
            return true;
        //TODO do the about activity with credentials to NEWS API

        // Save the news category selected by the user to restore it when needed
        chosenCategory = item.getTitle().toString();
        updateLeftMenu(chosenCategory);

        return true;
    }

    private void createLeftMenu()
    {
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerListView = findViewById(R.id.drawerList);

        SourceAdapter sourceAdapter =
                new SourceAdapter(this, R.layout.drawer_layout_item, drawerItemList);
        drawerListView.setAdapter(sourceAdapter);

        drawerListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onLeftMenuItemClicked(position);
            }
        });
        // create the drawer icon and icon handler
        actionBarDrawerToggle = new ActionBarDrawerToggle
        (
            this,          /* host Activity */
            drawerLayout,         /* DrawerLayout object */
            R.string.drawer_open, /* "open drawer" description for accessibility */
            R.string.drawer_close     /* "close drawer" description for accessibility */
        );
        // Make the drawer icon visible
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
    private void updateLeftMenu(String category)
    {
        if (category == null || category.isEmpty()) return;

        // The category that we get here comes from the map so nullptr exception
        drawerItemList.clear();
        drawerItemList.addAll(sourcesMap.get(category));
        ((SourceAdapter) drawerListView.getAdapter()).notifyDataSetChanged();
    }
    private void onLeftMenuItemClicked(int position)
    {
        // Make background image disappear
        background.setVisibility(View.GONE);

        // set news media source as action bar title
        TextView title = (TextView) getSupportActionBar().getCustomView();
        title.setText(drawerItemList.get(position).getName());

        // Send brodacast to Service
        Log.d(TAG, "onLeftMenuItemClicked: sending broadcast to Service");
        Intent intent = new Intent(getString(R.string.INTENT_TO_SERVICE));
        intent.putExtra(Intent.ACTION_ATTACH_DATA, drawerItemList.get(position));
        sendBroadcast(intent);

        drawerLayout.closeDrawer(drawerListView);
    }
    
    // After onRestoreState has occured we need to set back the menu to its saved state?
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
    public void onPostSourceDownload(Map<String, ArrayList<Source>> sourcesMap)
    {
        Log.d(TAG, "onPostSourceDownload: ");
        updateTreeMap(sourcesMap);
        updateRightMenu(sourcesMap);
    }
    private void updateTreeMap(Map<String, ArrayList<Source>> sourcesMap)
    {
        this.sourcesMap.clear();
        this.sourcesMap.putAll(sourcesMap);
    }


/*** Save state ***/
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putString(getString(R.string.NEWS_CATEGORY), chosenCategory);
        outState.putSerializable(getString(R.string.HASHMAP), (TreeMap)sourcesMap);
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "restoreInstanceState: ");
        chosenCategory = savedInstanceState.getString(getString(R.string.NEWS_CATEGORY));
        updateTreeMap(
                (Map<String, ArrayList<Source>>) savedInstanceState.getSerializable(getString(R.string.HASHMAP)));

        // build right menu not possible
        // build left menu OK
        if(!chosenCategory.isEmpty())
            updateLeftMenu(chosenCategory);
    }


/*** Receiver ***/
    @Override
    protected void onResume()
    {
        IntentFilter intentFilter = new IntentFilter(getString(R.string.INTENT_TO_MAIN));
        registerReceiver(newsReceiver, intentFilter);
        super.onResume();
    }
    @Override
    protected void onStop()
    {
        unregisterReceiver(newsReceiver);

        Intent intent = new Intent(MainActivity.this, NewsService.class);
        stopService(intent);

        super.onStop();
    }
    /* News Receiver */
    class NewsReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action == null || !action.equals(getString(R.string.INTENT_TO_MAIN))) {return;}

            if (intent.hasExtra(Intent.ACTION_ATTACH_DATA))
            {
                List<Article> articles = (ArrayList) intent.getSerializableExtra(Intent.ACTION_ATTACH_DATA);
                updateViewPager(articles);
                Log.d(TAG, "MainActivity onReceive: ");
            }
        }
    }


/*** ViewPager Adapter and Fragment ***/
    private class PageViewerAdapter extends FragmentPagerAdapter
    {
        private long baseId = 0;

        PageViewerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }
    private void updateViewPager(List<Article> articles) {
        updateViewPager(articles, 0);
    }
    private void updateViewPager(List<Article> articles, int currentItem)
    {
        if(articles == null || articles.isEmpty())
            return;

        this.articles = articles;
        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);

        fragments.clear();
        for (int i = 0; i < articles.size(); i++)
        {
            // article i is displayed as "article i+1 out of articles.size()
            fragments.add(
                    FragmentArticle.newInstance(articles.get(i), i+1, articles.size()));
        }

        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(currentItem);
    }
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