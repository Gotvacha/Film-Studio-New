package com.function;

import java.sql.Date;

public class Review {
    private final String title;
    private final String opinion;
    private final byte rating;
    private final Date date;
    private final String authorsName;

    public Review(String title, String opinion, byte rating, Date date, String authorsName) {
        this.title = title;
        this.opinion = opinion;
        this.rating = rating;
        this.date = date;
        this.authorsName = authorsName;
    }

    public String getTitle() {
        return title;
    }

    public String getOpinion() {
        return opinion;
    }

    public byte getRating() {
        return rating;
    }

    public Date getDate() {
        return date;
    }

    public String getAuthorsName() {
        return authorsName;
    }
}
