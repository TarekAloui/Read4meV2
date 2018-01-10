package com.aloui.tarek.read4me;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aloui.tarek.read4me.Activities.NewBookActivity;
import com.aloui.tarek.read4me.Activities.ReadActivity;
import com.aloui.tarek.read4me.Models.Book;
import com.aloui.tarek.read4me.Other.Database;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class LocalLibFragment extends Fragment {

    //CONSTANTS
    private static final String LIBRARY  = "Library";
    //FIREBASE
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBooksDatabaseReference;
    FirebaseRecyclerAdapter<Book, BookHolder> mFirebaseAdapter;
    public LocalLibFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View layout = inflater.inflate(R.layout.fragment_local_lib, container, false);
        //Actual work

        //firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBooksDatabaseReference = mFirebaseDatabase.getReference().child(LIBRARY);

        RecyclerView rv = (RecyclerView)layout.findViewById(R.id.rv_local_lib);

        FirebaseRecyclerOptions<Book> options =
                new FirebaseRecyclerOptions.Builder<Book>()
                        .setQuery(mBooksDatabaseReference, Book.class)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Book, BookHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final BookHolder holder, final int position, @NonNull final Book model) {
                holder.tvTitle.setText(model.getTitle());
                holder.tvAuthor.setText(model.getAuthor());
                holder.tvLang.setText(model.getLang());
                Glide.with(getActivity()).load(model.getImage_url())
                        .placeholder(R.drawable.book)
                        .error(R.drawable.book)
                        .into(holder.imBookImage);

                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO Read book
                        String hashKey = mFirebaseAdapter.getRef(position).getKey();
                        Intent intent = new Intent(getActivity(), ReadActivity.class);
                        intent.putExtra(Database.getPassedBookKey(), hashKey);

                        startActivity(intent);
                    }
                });

                holder.imBtnOption.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Getting Book Key
                        final String hashKey = mFirebaseAdapter.getRef(position).getKey();
                        //POP UP SETUP
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getActivity(), holder.imBtnOption);
                        //inflating menu from xml resource
                        popup.inflate(R.menu.options_menu_book_list);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.item_book_read_live:
                                        //handle live read click

                                        //Saving Book hash key
                                        Database database = new Database(getActivity().getSharedPreferences(Database.getSharedPref(),
                                                Context.MODE_PRIVATE));
                                        database.saveCurBookHash(hashKey);
                                        //Changing Fragment
                                        getActivity().setTitle("Home");
                                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                        ft.replace(R.id.your_placeholder, new HomeFragment());
                                        ft.commit();

                                        break;
                                    case R.id.item_book_delete:
                                        //handle delete click
                                        mBooksDatabaseReference.child(hashKey).removeValue();

                                        break;
                                }
                                return false;
                            }
                        });
                        //displaying the popup
                        popup.show();
                    }
                });


            }

            @Override
            public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.book_list_row, parent, false);
                return new BookHolder(view);
            }
        };

        rv.setAdapter(mFirebaseAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), RecyclerView.VERTICAL));


        //FAB
        FloatingActionButton fabAddBook = (FloatingActionButton)layout.findViewById(R.id.fab_add_book);
        fabAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NewBookActivity.class));
            }
        });

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }
}

class BookHolder extends RecyclerView.ViewHolder
{
    TextView tvTitle, tvAuthor, tvLang;
    ImageView imBookImage;
    ImageButton imBtnOption;
    LinearLayout layout;
    public BookHolder(View itemView) {
        super(itemView);
        tvTitle = (TextView)itemView.findViewById(R.id.tv_book_row_title);
        tvAuthor = (TextView)itemView.findViewById(R.id.tv_book_row_author);
        tvLang = (TextView)itemView.findViewById(R.id.tv_book_row_lang);
        imBookImage = (ImageView)itemView.findViewById(R.id.im_book_row);
        layout = (LinearLayout)itemView.findViewById(R.id.item_book_rv);
        imBtnOption = (ImageButton)itemView.findViewById(R.id.im_btn_options);
    }
}
