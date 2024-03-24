package com.amber.library.library;

public class Book {
    private Number id;
    private String title;
    private String authors;
    private String isbn;
    private String dewey;
    private Number publisherId;

    private Publisher publisher;
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

    public String getDewey() {
        return dewey;
    }

    public void setDewey(String dewey) {
        this.dewey = dewey;
    }

    public Number getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Number publisherId) {
        this.publisherId = publisherId;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Book(Number id, String title, String authors, String isbn, String dewey, Number publisherId) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.isbn = isbn;
        this.dewey = dewey;
        this.publisherId = publisherId;
    }
}
