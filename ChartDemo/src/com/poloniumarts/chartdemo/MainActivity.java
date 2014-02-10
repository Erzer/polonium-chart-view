package com.poloniumarts.chartdemo;

import java.util.ArrayList;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.poloniumarts.chartdemo.DefaultChartFragment.OnFragmentInteractionListener;

public class MainActivity extends FragmentActivity implements OnFragmentInteractionListener{

	private Fragment currentFragment = null;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_navigation_drawer, /* nav drawer icon to replace 'Up' caret */
        R.string.app_name, /* "open drawer" description */
        R.string.app_name /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(R.string.app_name);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
               //getActionBar().setTitle(R.string.app_name);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        setFragment(DefaultChartFragment.newInstance());

        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        DrawerAdapter adapter = new DrawerAdapter();
        mDrawerList.setAdapter(adapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(adapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private void setFragment(Fragment fragment) {
        setFragment(fragment, null);
    }

    private void setFragment(Fragment fragment, String name) {
        if (fragment != currentFragment) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (name != null) ft.addToBackStack(name);
            ft.replace(R.id.content_frame, fragment).commit();
            currentFragment = fragment;
        }
    }

    @Override
    public void openDrawer() {
        mDrawerLayout.openDrawer(mDrawerList);
    }

    @Override
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    class DrawerAdapter extends BaseAdapter implements OnItemClickListener{

        ArrayList<String> title = new ArrayList<String>();
        ArrayList<Integer> group = new ArrayList<Integer>();
        ArrayList<Integer> level = new ArrayList<Integer>();
        SparseIntArray selected = new SparseIntArray();
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        
        public DrawerAdapter() {
            addItem("Line Chart", 0xff33b5e5, 0, null);
            addItem("Default", 0xff33b5e5, 0, DefaultChartFragment.newInstance());
            addItem("Multiline", 0xff33b5e5, 0, MultilineChartFragment.newInstance("Multiline"));
            addItem("Marked points", 0xff33b5e5, 0, MarkedPointsChartFragment.newInstance("Marked points"));
            addItem("Smooth lines", 0xff33b5e5, 0, SmoothLinesChartFragment.newInstance("Smooth lines"));
            addItem("Custom grid", 0xff33b5e5, 0, CustomGridFragment.newInstance("Custom grid"));
            
            setSelected(1);
        }

        public void setSelected(int position) {
            selected.put(group.get(position), position);
        }

        public void addItem(String title, int group, int level, Fragment fragment) {
            this.title.add(title);
            this.group.add(group);
            this.level.add(level);
            this.fragments.add(fragment);
        }

        @Override
        public int getCount() {
            return title.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            return !isEnabled(position) ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
        @Override
        public boolean isEnabled(int position) {
            return fragments.get(position) != null;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (getItemViewType(position) == 0) {
                    convertView = getLayoutInflater().inflate(R.layout.item_title, null);
                }
                if (getItemViewType(position) == 1) {
                    convertView = getLayoutInflater().inflate(R.layout.item_item, null);
                }
            }
            TextView title = (TextView)convertView.findViewById(R.id.title);
            if (selected.get(group.get(position), -1) == position)title.setBackgroundColor(group.get(position));
            else title.setBackgroundColor(0x00000000);
            title.setText(this.title.get(position));
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
           setSelected(position);
           notifyDataSetChanged();
           setFragment(fragments.get(position));
           mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
}
