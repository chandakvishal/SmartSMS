package com.example.vinay.sms;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.vinay.sms.Adapter.ViewPagerAdapter;
import com.example.vinay.sms.Messaging.Display.SentMessageDisplay;
import com.example.vinay.sms.Messaging.Display.SmsDisplayFragment;
import com.example.vinay.sms.Messaging.Send.SendMessage;
import com.example.vinay.sms.Search.SearchableActivity;
import com.example.vinay.sms.Utilities.BackHandledFragment;

public class MainActivity extends AppCompatActivity
        implements BackHandledFragment.BackHandlerInterface, SearchView.OnQueryTextListener {

    private SearchView searchView;

    private BackHandledFragment selectedFragment;

    TabLayout tabLayout;

    public static CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: Add a splash Screen
        SharedPreferences pref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isDark = pref.getBoolean("isDark", true);
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(15.0f);
        }

        FloatingActionButton sendMessageFloatingButton = (FloatingActionButton) findViewById(R.id.sendMessageFloatingButton);

        assert sendMessageFloatingButton != null;
        sendMessageFloatingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(getApplicationContext(), SendMessage.class);
                startActivity(intent);
                overridePendingTransition(R.anim.animation1, R.anim.animation3);
            }
        });
    }

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.selectedFragment = selectedFragment;
    }

    @Override
    public void onBackPressed() {
        if (selectedFragment == null || !selectedFragment.onBackPressed()) {
            // Selected fragment did not consume the back press event.
            super.onBackPressed();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SmsDisplayFragment(), "Inbox");
        adapter.addFragment(new SentMessageDisplay(), "Sent");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Search Functionality
        final MenuItem searchItem = menu.findItem(R.id.search);

        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, SearchableActivity.class)));
        searchView.setIconifiedByDefault(false);

        final Intent intentShowLocal = new Intent(this, SearchableActivity.class);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                android.support.v4.widget.CursorAdapter adapter = searchView.getSuggestionsAdapter();
                Cursor cursor = adapter.getCursor();
                if (cursor != null) {
                    if (cursor.moveToPosition(position)) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                        searchItem.collapseActionView();
                        String geolocation2 = cursor.getString(
                                cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));

                        intentShowLocal.putExtra("query", geolocation2);
                        intentShowLocal.setAction("android.intent.action.VIEW");
                        startActivity(intentShowLocal);
                    }
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(getApplicationContext(), Settings.class);
                startActivity(settings);
                finish();
                return true;
            case R.id.assiatant:
                Intent assistant = new Intent(getApplicationContext(), ApiAi.class);
                startActivity(assistant);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static CoordinatorLayout getCoordinateLayout() {
        return coordinatorLayout;
    }
}