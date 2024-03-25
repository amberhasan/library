package com.amber.library.library;

/**
 * Represents a book in the library system.
 * This class encapsulates the properties of a book, including its identifier, title, authors,
 * ISBN, Dewey Decimal classification, and the identifier of its publisher. It provides a structured
 * way to handle book data throughout the application.
 * Written by Amber Hasan (amh130430) for CS 6360.MS1, starting on 3/1/2024.
 */
public class Book {
    // Fields are marked as 'final' to ensure the immutability of Book instances once created
    private final Number id;
    private final String title;
    private final String authors;
    private final String isbn;
    private final String dewey;
    private final Number publisherId;

    /**
     * Constructs a new Book instance with specified details.
     * This constructor initializes a book with all its essential information, making the instance immutable.
     *
     * @param id The unique identifier of the book.
     * @param title The title of the book.
     * @param authors The authors of the book, represented as a single String.
     * @param isbn The ISBN of the book.
     * @param dewey The Dewey Decimal classification of the book.
     * @param publisherId The identifier of the book's publisher.
     */
    public Book(Number id, String title, String authors, String isbn, String dewey, Number publisherId) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.isbn = isbn;
        this.dewey = dewey;
        this.publisherId = publisherId;
    }

    // Getter methods provide read-only access to the book's properties, supporting the immutability
    public Number getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthors() {
        return authors;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getDewey() {
        return dewey;
    }

    public Number getPublisherId() {
        return publisherId;
    }
}
