package com.example.vinay.sms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.vinay.sms.Utilities.BackHandledFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        BackHandledFragment.BackHandlerInterface {

    private final String TAG = this.getClass().getSimpleName();
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView smsListView;
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

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.main_fragment);
        assert frameLayout != null;
        frameLayout.getForeground().setAlpha(0);

        smsListView = (ListView) findViewById(R.id.SMSList);

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
}