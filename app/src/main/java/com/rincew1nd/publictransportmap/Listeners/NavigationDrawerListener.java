package com.rincew1nd.publictransportmap.Listeners;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.rincew1nd.publictransportmap.Activities.MapsActivity;
import com.rincew1nd.publictransportmap.R;

/**
 * Created by Rincew1nd on 15.05.2017.
 */

public class NavigationDrawerListener implements
        ListView.OnItemClickListener {

    private MapsActivity _context;
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    public NavigationDrawerListener(MapsActivity mapsActivity) {
        _context = mapsActivity;

        mPlanetTitles = new String[]{"Солнце", "Уран", "Нептун", "Плутон", "Марс"};
        mDrawerLayout = (DrawerLayout) _context.findViewById(R.id.navigation_drawer_layout);
        mDrawerList = (ListView) _context.findViewById(R.id.navigation_left_drawer);

        DrawerLayout mDrawerLayout =
                (DrawerLayout) _context.findViewById(R.id.navigation_drawer_layout);
        mDrawerList = (ListView) _context.findViewById(R.id.navigation_left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(
                new ArrayAdapter<>(_context, R.layout.navigation_drawer_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(_context, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                _context.getActionBar().setTitle("test_1");
                _context.invalidateOptionsMenu();
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                _context.getActionBar().setTitle("test_2");
                _context.invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    private void selectItem(int position) {
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        Toast.makeText(_context, "FK YOU", Toast.LENGTH_SHORT).show();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return _context.onPrepareOptionsMenu(menu);
    }
}
