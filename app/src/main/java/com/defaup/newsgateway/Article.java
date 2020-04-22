package com.defaup.newsgateway;

import java.io.Serializable;

class Article implements Serializable
{
    public String author, title, description, url, urlToImage, publishedAt;
    Article(String author, String title, String description, String url, String urlToImage, String publishedAt)
    {
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
    }
}
