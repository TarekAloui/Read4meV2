package com.aloui.tarek.read4me;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aloui.tarek.read4me.Other.Database;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;


public class HomeFragment extends Fragment {

    //CONSTANTS
    private static final String LIBRARY  = "Library";
    private static final String BOOK_TITLE  = "title";
    private static final String BOOK_AUTHOR  = "author";
    private static final String BOOK_LANG  = "lang";
    private static final String BOOK_CURRENT_INDEX = "ind";
    private static final String BOOK_ORIGINAL_CONTENT  = "original_content";
    private static final String BOOK_TRANSLATED_CONTENT  = "translated_content";
    private static final String APP_SAVE_PATH  = Environment.getExternalStorageDirectory().getPath();
    ImageButton imBtnTtsControl;
    boolean startReading = false;
    //MULTITHREADING
    Handler handler;
    //TEXT TO SPEECH
    TextToSpeech tts;
    boolean running = true;
    //Other vars
    boolean firstTime = true;
    boolean firstTimeLoadingTTS = true;
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
    private int readInd = -1;
    private String ch_readInd = "0";
    private int synthInd = -1;

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
        imBtnTtsControl = (ImageButton) layout.findViewById(R.id.im_btn_live_play_pause);

        imBtnTtsControl.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
        imBtnTtsControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View constraintL = getActivity().findViewById(R.id.constraint_layout_main);
                Snackbar.make(constraintL, R.string.im_btn_control_no_page_loaded, Snackbar.LENGTH_LONG).show();
            }
        });
        //initializing pages arraylist
        pages = new ArrayList<>();

        // TTS INIT
        tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(new Locale(lang));
            }
        });

        if(bookKey.equals("")) {
            tvTitle.setText(R.string.tv_live_pick_book);
            return layout;
        }

        //SETTING TITLE
        mBook.child(BOOK_TITLE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    title = dataSnapshot.getValue().toString();
                    tvTitle.setText(title);
                } else {
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
                if(dataSnapshot.getValue()!= null) {
                    readInd = Integer.parseInt(dataSnapshot.getValue().toString());
                    Log.w("DEBUG", String.format("READING INDEX %s", readInd));
                    //GET PAGES
                    startListeningForPages();
                } else {
                    readInd = -1;
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


        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onDestroy() {
        Log.w("DESTROY", "HOME FRAGMENT DESTROYED");
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

    private void setInd(int new_ind) {
        readInd = new_ind;
        //Save locally
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Database.getSharedPref(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(database.getCurrentBookKey() + "ind", "" + readInd);
        //Save online
        mBook.child(BOOK_CURRENT_INDEX).setValue(readInd);
    }

    private int getIndLocal() {
        //Save locally
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Database.getSharedPref(), Context.MODE_PRIVATE);
        String s = sharedPreferences.getString(database.getCurrentBookKey() + "ind", "-1");
        return Integer.parseInt(s);
    }

    private void startListeningForPages() {
        // GET PAGES
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

                Log.w("DEBUG SYNTHESIS", utteranceId+ " STARTED!");

                running = true;
                final String id = utteranceId;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvBuffer.setText(pages.get(Integer.parseInt(id)));
                        imBtnTtsControl.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                        imBtnTtsControl.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //PAUSE
                                imBtnTtsControl.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                                imBtnTtsControl.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //Resume
                                        int len = pages.size();
                                        for (int i = readInd; i < len; i++) {
                                            String uniqueId = "" + i;
                                            tts.speak(pages.get(i), TextToSpeech.QUEUE_ADD, null, uniqueId);
                                        }
                                    }
                                });
                                running = false;
                                tts.stop();
                            }
                        });
                    }
                });
            }


            @Override
            public void onDone(String utteranceId) {
                Log.w("DEBUG SYNTHESIS", utteranceId +" DONE!");
                Log.w("DONE STATUS", String.format("readInd %d / pages %d ", readInd + 1, pages.size()));
                if (running) {
                    readInd++;
                    mBook.child(BOOK_CURRENT_INDEX).setValue(readInd);
                }
                if (readInd >= pages.size()) //TODO 3afset el done mata5demch!
                {
                    //DONE READING LOADED PAGES
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imBtnTtsControl.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                            imBtnTtsControl.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    View constraintL = getActivity().findViewById(R.id.constraint_layout_main);
                                    Snackbar.make(constraintL, R.string.im_btn_control_no_more_pages_loaded, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onError(String utteranceId) {
                Log.w("DEBUG FILE", "ERROR WRITING FILE");
            }
        });

        mBook.child(BOOK_TRANSLATED_CONTENT).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String p = dataSnapshot.getValue().toString();
                pages.add(p);
                Log.w("READ VALUES", pages.size() + " " + readInd);
                Log.w("NEW PAGE", p);
                if (readInd < pages.size() && running) {

                    firstTimeLoadingTTS = false;
                    String uniqueId = "" + (pages.size() - 1);
                    //String destinationFileName = APP_SAVE_PATH+uniqueId+".wav";
                    Log.w("SPEECH STARTED", String.format("readInd %d / pages %d", readInd, pages.size()));
                    tts.speak(p, TextToSpeech.QUEUE_ADD, null, uniqueId);
                }
                Log.w("CHILDREN COUNT", String.valueOf(dataSnapshot.getChildrenCount()));

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
    }
}


