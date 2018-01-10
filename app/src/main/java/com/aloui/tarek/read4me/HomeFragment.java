package com.aloui.tarek.read4me;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aloui.tarek.read4me.Other.Database;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.io.SerializablePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class HomeFragment extends Fragment {

    //CONSTANTS
    private static final String LIBRARY  = "Library";
    private static final String BOOK_TITLE  = "title";
    private static final String BOOK_AUTHOR  = "author";
    private static final String BOOK_LANG  = "lang";
    private static final String BOOK_CURRENT_INDEX  = "current_index";
    private static final String BOOK_ORIGINAL_CONTENT  = "original_content";
    private static final String BOOK_TRANSLATED_CONTENT  = "translated_content";
    private static final String APP_SAVE_PATH  = Environment.getExternalStorageDirectory().getPath();
    //Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBook;
    private String bookKey;
    private String lang="en-US";
    private String title;

    //UI Components
    private TextView tvBuffer, tvTitle;

    //Offline Database
    private Database database;
    private ArrayList<String> pages;
    boolean startReading = false;
    private int readInd = 100;
    private String ch_readInd = "0";
    private int synthInd = -1;
    //MULTITHREADING
    Handler handler;
    //TEXT TO SPEECH
    TextToSpeech tts;
    MediaPlayer mediaPlayer;

    //Other vars
    boolean firstTime = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View layout = inflater.inflate(R.layout.fragment_home, container, false);
        //setting book
        database = new Database(getActivity().getSharedPreferences(Database.getSharedPref(), Context.MODE_PRIVATE));
        bookKey = database.getCurrentBookKey();

        Log.w("DEBUG BOOK KEY", bookKey);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBook = mFirebaseDatabase.getReference().child(LIBRARY).child(bookKey);

        tvBuffer = (TextView)layout.findViewById(R.id.tv_live_buffer);
        tvTitle = (TextView)layout.findViewById(R.id.tv_live_title);
        //initializing pages arraylist
        pages = new ArrayList<>();

        // TTS INIT
        tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(new Locale(lang));
            }
        });

        if(bookKey.equals(""))
        {
            tvTitle.setText(R.string.tv_live_pick_book);
            return layout;
        }

        //SETTING TITLE
        mBook.child(BOOK_TITLE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                {
                    title = dataSnapshot.getValue().toString();
                    tvTitle.setText(title);
                }
                else
                {
                    Log.w("DEBUG ERROR", "TITLE IS NULL!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //LISTENING FOR READING INDEX
        mBook.child(BOOK_CURRENT_INDEX).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.w("DEBUG", String.format("INITIAL INDEX %s", dataSnapshot.getValue().toString()));
                if(dataSnapshot.getValue()!= null)
                {
                    ch_readInd = dataSnapshot.getValue().toString();
                }
                else
                {
                    ch_readInd = "";
                    Log.w("DEBUG ERROR!", "CURRENT INDEX NULL");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //SETTING LANG
        mBook.child(BOOK_LANG).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.w("DEBUG LANG", dataSnapshot.getValue().toString());
                if(dataSnapshot.getValue() != null) lang = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // GET PAGES
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.w("DEBUG SYNTHESIS", utteranceId+ " STARTED!");
                final String id = utteranceId;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvBuffer.setText(pages.get(Integer.parseInt(id)));
                    }
                });
            }


            @Override
            public void onDone(String utteranceId) {

                Log.w("DEBUG SYNTHESIS", utteranceId +" DONE!");
                readInd++;
                mBook.child(BOOK_CURRENT_INDEX).setValue(readInd);
            }

            @Override
            public void onError(String utteranceId) {
                Log.w("DEBUG FILE", "ERROR WRITING FILE");
            }
        });

        mBook.child(BOOK_TRANSLATED_CONTENT).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(firstTime)
                {
                    firstTime = false;
                    //convert readIn to int
                    readInd = Integer.parseInt(ch_readInd);

                }
                String p = dataSnapshot.getValue().toString();
                Log.w("DEBUG", p);
                pages.add(p);
                synthInd++;
                Log.w("DEBUG VALUES", pages.size()+" "+readInd + " " + synthInd);
                if(pages.size() >= readInd)
                {
                    String uniqueId = ""+synthInd;
                    //String destinationFileName = APP_SAVE_PATH+uniqueId+".wav";
                    Log.w("DEBUG SPEECH", "SPEECH TO BE STARTED!");
                    tts.speak(p, TextToSpeech.QUEUE_ADD, null, uniqueId);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public ArrayList<String> getPages() {
        return pages;
    }

    public int getReadInd() {
        return readInd;
    }
}


