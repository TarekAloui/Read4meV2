package com.aloui.tarek.read4me.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aloui.tarek.read4me.LocalLibFragment;
import com.aloui.tarek.read4me.MainNavigationActivity;
import com.aloui.tarek.read4me.Models.Book;
import com.aloui.tarek.read4me.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewBookActivity extends AppCompatActivity {

    //CONSTANTS
    private static final String LIBRARY  = "Library";
    //FIREBASE
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBooksDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);
        // UI Components
        final EditText EtTitle = (EditText)findViewById(R.id.et_new_book_title);
        final EditText EtAuthor = (EditText)findViewById(R.id.et_new_book_author);
        final EditText EtLang = (EditText)findViewById(R.id.et_new_book_lang);
        final EditText EtImageUrl = (EditText)findViewById(R.id.et_new_book_image_url);

        // Firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBooksDatabaseReference = mFirebaseDatabase.getReference().child(LIBRARY);

        Button BAddBook = (Button)findViewById(R.id.btn_new_book);
        BAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Book newBook = new Book(EtTitle.getText().toString(), EtAuthor.getText().toString(), EtLang.getText().toString(),
                        EtImageUrl.getText().toString());
                mBooksDatabaseReference.push().setValue(newBook);
                finish();
            }
        });

    }
}
