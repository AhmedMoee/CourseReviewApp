package edu.virginia.sde.reviews;

import java.util.ArrayList;

public class Course {
    private int courseID;
    private String subject;      // 4 characters e.g. "CS", "STS"
    private int courseNumber;           // 4 digits e.g. 2150
    private String title;               // 50 characters e.g. "Software Development"
    private Double averageRating;      // Average rating of the course

    // Constructor
    public Course(int id, String subject, int courseNumber, String title) {
        this.courseID = id;
        this.subject = subject;
        this.courseNumber = courseNumber;
        this.title = title;
        this.averageRating = null; // Initially, there may be no reviews
    }

    public Course() {
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(int courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    @Override
    public String toString() {
        String ratingStr = averageRating != null ? String.format(" - Avg Rating: %.2f", averageRating) : "";
        return subject + " " + courseNumber + ": " + title + ratingStr;
    }
}
