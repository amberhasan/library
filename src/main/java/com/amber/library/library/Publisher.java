package com.amber.library.library;

/**
 * Represents a publisher in the library management system.
 * This class models the essential information about a publisher, including its unique identifier, name, and website.
 * Publishers are entities that produce and distribute books or other publications managed within the system.
 * Written by Amber Hasan (amh130430) for CS 6360.MS1, starting on 3/1/2024.
 */
public class Publisher {
    // Unique identifier for the publisher
    int id;

    // Name of the publisher
    String name;

    // Website URL of the publisher
    String website;

/**
 * Constructs a new Publisher instance with the specified details.
 * This constructor initializes a publisher with an ID, name, and website,
 * providing a complete representation of a publisher entity within the system.
 *
 * @param id The unique identifier for the publisher.
 * @param name The name of the publisher.
 * @param website The website URL of the publisher.
 */
    public Publisher(int id, String name, String website) {
        this.id = id;
        this.name = name;
        this.website = website;
    }

/**
 * Gets the unique identifier of this publisher.
 * The ID is used to uniquely identify each publisher in the system,
 * facilitating operations like searching, updating, or deleting publisher information.
 *
 * @return The unique identifier of the publisher.
 */
    public int getId() {
        return id;
    }

/**
 * Returns a string representation of this publisher.
 * This override of the {@code toString()} method provides a simple way to print or log
 * publisher information, primarily focusing on the publisher's name for readability.
 *
 * @return A string representation of the publisher, specifically its name.
 */
    @Override
    public String toString() {
        return this.name;
    }
}