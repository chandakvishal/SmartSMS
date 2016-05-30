package com.example.vinay.sms;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.example.vinay.sms.Messaging.Display.SmsDisplayFragment;
import com.example.vinay.sms.Search.SearchableActivity;
import com.example.vinay.sms.Utilities.BackHandledFragment;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements BackHandledFragment.BackHandlerInterface, SearchView.OnQueryTextListener {

    private final String TAG = this.getClass().getSimpleName();

    private SearchView searchView;

//    ArrayList<String> smsMessagesList = new ArrayList<>();

    private BackHandledFragment selectedFragment;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(15.0f);
        }

        if (savedInstanceState == null)
            changeFragment(new SmsDisplayFragment(), "smsDisplayFragment");
    }

    public void changeFragment(Fragment targetFragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.animation2, R.anim.animation4)
                .replace(R.id.main_fragment, targetFragment, tag)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(tag)
                .commit();
    }

    public void changeFragment(Fragment targetFragment, String tag, int animation1, int animation2) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(animation1, animation2)
                .replace(R.id.main_fragment, targetFragment, tag)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(tag)
                .commit();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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
                if(cursor != null) {
                    if(cursor.moveToPosition(position)) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                        searchItem.collapseActionView();
                        String geolocation2 = cursor.getString(
                                cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                        Log.d(TAG, "onSuggestionClick:1 " + Arrays.toString(cursor.getColumnNames()));
                        Log.d(TAG, "onSuggestionClick:1 " + geolocation2);

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
}