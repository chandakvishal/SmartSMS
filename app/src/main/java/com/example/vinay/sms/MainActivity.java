package com.example.vinay.sms;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        BackHandledFragment.BackHandlerInterface {


    private final String TAG = this.getClass().getSimpleName();
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView smsListView;

    @Override
    public void onStart() {
        super.onStart();
    }

    //Contacts Data Items to extract
    static final String[] CONTACT_ROWS = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.main_fragment);
        assert frameLayout != null;
        frameLayout.getForeground().setAlpha(0);

        CoordinatorLayout mLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);

        smsListView = (ListView) findViewById(R.id.SMSList);

//        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null)
            changeFragment(new SmsDisplayFragment(), "smsDisplayFragment");
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Intent intent = new Intent(Intent.ACTION_MAIN);
    //intent.addCategory(Intent.CATEGORY_DEFAULT);
    //intent.setType("vnd.android-dir/mms-sms");
    //startActivity(intent);

    public void changeFragment(Fragment targetFragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.animation2, R.anim.animation4)
                .replace(R.id.main_fragment, targetFragment, tag)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(tag)
                .commit();
    }

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
    }

//    @Override
//    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
//
//        //Select Statement to select the contacts
//        String select = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOT NULL) AND ("
//                + ContactsContract.Contacts.DISPLAY_NAME + " != '' ) AND ("
//                + ContactsContract.Contacts.STARRED + " == 1))";
//
//        String sortOrder = ContactsContract.Contacts._ID + " ASC";
//        Log.d(TAG, "onCreateLoader1: " + select);
//        Log.d(TAG, "onCreateLoader2: " + sortOrder);
//        return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI, CONTACT_ROWS,
//                select, null, sortOrder);
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        Log.d(TAG, "onLoadLoader1: " +cursor.getCount());
//        if (cursor.moveToFirst()){
//            do{
//                String data = cursor.getString(cursor.getColumnIndex("data"));
//                Log.d(TAG, "onLoadLoader2: " +data);
//                Log.d(TAG, "onLoadFinishedLoader3: " + cursor.getColumnCount());
//            }while(cursor.moveToNext());
//        }
//     }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//
//    }

}