package com.amber.library.library;

public class Book {
    Number id;
    String title;
    String authors;
    String isbn;
    Number dewey;
    Number publisherId;
    public Number getId() {
        return id;
    }

    public void setId(Number id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Number getDewey() {
        return dewey;
    }

    public void setDewey(Number dewey) {
        this.dewey = dewey;
    }

    public Number getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Number publisherId) {
        this.publisherId = publisherId;
    }



    public Book(Number id, String title, String authors, String isbn, Number dewey, Number publisherId) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.isbn = isbn;
        this.dewey = dewey;
        this.publisherId = publisherId;
    }
}
