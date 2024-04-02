package com.function;

public class Movie {
    private final String title;
    private final int year;
    private final String genre;
    private final String description;
    private final String director;
    private final String actors;

    public Movie(String title, int year, String genre, String description, String director, String actors) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.description = description;
        this.director = director;
        this.actors = actors;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public String getDescription() {
        return description;
    }

    public String getDirector() {
        return director;
    }

    public String getActors() {
        return actors;
    }
}
