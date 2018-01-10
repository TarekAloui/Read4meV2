package com.aloui.tarek.read4me.Models;

import java.util.ArrayList;

/**
 * Created by Tarek on 01/01/2018.
 */

public class Book {
    private String title, author, lang, image_url;
    int current_index;
    private ArrayList<String> content_original, content_translated, tags;

    public Book()
    {

    }

    public Book(String title, String author, String lang)
    {
        this.title = title;
        this.author = author;
        this.lang = lang;
    }

    public Book(String title, String author, String lang, String image_url)
    {
        this.title = title;
        this.author = author;
        this.lang = lang;
        this.image_url = image_url;
    }

    //GETTERS

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getLang() {
        return lang;
    }

    public int getCurrent_index() {
        return current_index;
    }

    public ArrayList<String> getContent_original() {
        return content_original;
    }

    public ArrayList<String> getContent_translated() {
        return content_translated;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public String getImage_url() {
        return image_url;
    }

    //SETTERS

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setCurrent_index(int current_index) {
        this.current_index = current_index;
    }

    public void setContent_original(ArrayList<String> content_original) {
        this.content_original = content_original;
    }

    public void setContent_translated(ArrayList<String> content_translated) {
        this.content_translated = content_translated;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

}
