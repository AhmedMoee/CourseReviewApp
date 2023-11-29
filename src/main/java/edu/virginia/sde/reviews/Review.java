package edu.virginia.sde.reviews;

import java.sql.Timestamp;

public class Review {


    private int id;
    private int userID;
    private int courseID;
    private int rating;
    private Timestamp entryTime;
    private String comment;

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    // Constructor
    public Review(int id, int userID, int courseID, int rating, Timestamp entryTime, String comment) {
        this.id = id;
        this.userID = userID;
        this.courseID = courseID;
        this.rating = rating;
        this.entryTime = entryTime;
        this.comment = comment;
    }

    public Review() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Rating: " + rating + "/5" + "\n" +
                "Date posted: " + entryTime + "\n" +
                comment + "\n" +
                "Last Updated: " + timestamp;
    }
}
