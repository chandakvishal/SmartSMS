package com.example.vinay.sms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.vinay.sms.Messaging.Send.SendSms;
import com.example.vinay.sms.Utilities.DatabaseHandler;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.ui.AIButton;

public class ApiAi extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    TextView responseText;

    TextToSpeech tts;

    DatabaseHandler db;

    SendSms sendSms = new SendSms();

    AIConfiguration config;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ai);

        db = new DatabaseHandler(getApplicationContext());

        config = new AIConfiguration("59f0f98ffcf0403f994ec530ada1acfa",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(15.0f);
            getSupportActionBar().setTitle("Assistant");
        }

        final AIButton aiButton = (AIButton) findViewById(R.id.micButton);
        final EditText inputText = (EditText) findViewById(R.id.inputText);
        responseText = (TextView) findViewById(R.id.responseTextView);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
                if (status == TextToSpeech.SUCCESS) {
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            Log.d(TAG, "onDone: TTS DONE");
                            assert aiButton != null;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    aiButton.callOnClick();
                                }
                            });
//                            aiButton.callOnClick();
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                }
            }
        });

        assert aiButton != null;
        aiButton.initialize(config);
        aiButton.setResultsListener(new AIButton.AIButtonListener() {
            @Override
            public void onResult(final AIResponse result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Result response = result.getResult();

                        Log.d(TAG, "onResult");
                        assert inputText != null;
                        inputText.setText(response.getResolvedQuery());
                        assert responseText != null;
                        String responseSpeech = response.getFulfillment().getSpeech();
                        responseText.setText(responseSpeech);

                        final String name = response.getStringParameter("name");
                        final String messageText = response.getStringParameter("text");
                        //TODO : Handle Contextual Responses
                        final ArrayList<String> numbers = db.getContact(name);
                        if (numbers.size() == 0) {
                            queryForNoContacts();
                        } else if (numbers.size() == 1) {
                            Log.d(TAG, "run: NUMBER FOUND TO BE: " + numbers.get(0));
                            sendSms.sendSMS(numbers.get(0), messageText, getApplicationContext());
                        } else {
                            ArrayList<String> temp = db.getContactName(name);
                            Log.d(TAG, "Temp List Size " + temp.size());

                            String responseSpeechConfused = "Hey! I am confused, can you help me out here?";
                            assert responseText != null;
                            final String[] nameToSend = new String[1];
                            responseText.setText(responseSpeechConfused);
                            tts.speak(responseSpeechConfused, TextToSpeech.QUEUE_FLUSH, null);

                            AlertDialog.Builder builderSingle = new AlertDialog.Builder(ApiAi.this);
                            builderSingle.setIcon(R.drawable.ic_assistant);
                            builderSingle.setTitle("Select a contact:-");

                            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ApiAi.this,
                                    android.R.layout.select_dialog_singlechoice);
                            arrayAdapter.addAll(temp);

                            builderSingle.setNegativeButton(
                                    "cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            builderSingle.setAdapter(
                                    arrayAdapter,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final String strName = arrayAdapter.getItem(which);
                                            AlertDialog.Builder builderInner = new AlertDialog.Builder(ApiAi.this);
                                            builderInner.setMessage(strName);
                                            builderInner.setTitle("Your Selected Item is");
                                            builderInner.setPositiveButton(
                                                    "Ok",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialog,
                                                                int which) {
                                                            nameToSend[0] = strName;
                                                            String selectedContactName = nameToSend[0];
                                                            ArrayList<String> sendToName = db.getContact(selectedContactName);
                                                            Log.d(TAG, "Numbers List Size " + numbers.size());
                                                            sendSms.sendSMS(sendToName.get(0), messageText, getApplicationContext());
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            builderInner.show();
                                        }
                                    });
                            builderSingle.show();
                        }

                        HashMap<String, String> map = new HashMap<>();
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, getRandomPhrase());
                        //noinspection deprecation
                        tts.speak(responseSpeech, TextToSpeech.QUEUE_FLUSH, map);
                    }
                });
            }

            @Override
            public void onError(final AIError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ApiAi", "onError" + error.toString());
                        if (error.toString().contains("No speech input")) {
                            String responseSpeech = "Hey! you didn't say anything thus message sending cancelled.";
                            assert responseText != null;
                            responseText.setText(responseSpeech);
                            tts.speak(responseSpeech, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });
            }

            @Override
            public void onCancelled() {

            }
        });
    }

    @Override
    protected void onPause() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public String getRandomPhrase() {
        Random random = new Random();
        return new BigInteger(130, random).toString(32);
    }

    public void queryForNoContacts() {
        final AIDataService aiDataService = new AIDataService(this, config);
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery("Contact not found in Database");

        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                try {
                    return aiDataService.request(aiRequest);
                } catch (AIServiceException e) {
                    Log.e(TAG, "doInBackground: " + e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    Result result = aiResponse.getResult();
                    String responseSpeech = result.getFulfillment().getSpeech();
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, getRandomPhrase());
                    //noinspection deprecation
                    tts.speak(responseSpeech, TextToSpeech.QUEUE_FLUSH, map);
                }
            }
        }.execute(aiRequest);
    }
}
