package com.example.newappstage2;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class News {

    private String articleTitle;
    private String articleSection;
    private String articleDate;
    private String articleUrl;
    private Bitmap articleThumbnail;
    private ArrayList<String> articleAuthors;

    public News(String articleTitle, String articleSection, String articleDate, Bitmap articleThumbnail, String articleUrl, ArrayList<String> articleAuthors) {
        this.articleTitle = articleTitle;
        this.articleSection = articleSection;
        this.articleDate = articleDate;
        this.articleThumbnail = articleThumbnail;
        this.articleUrl = articleUrl;
        this.articleAuthors = articleAuthors;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public String getArticleSection() {
        return articleSection;
    }

    public String getArticleDate() {
        return articleDate;
    }

    public Bitmap getArticleThumbnail() {
        return articleThumbnail;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    ArrayList<String> getArticleAuthors() {
        return articleAuthors;
    }
}
