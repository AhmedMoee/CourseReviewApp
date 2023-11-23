package edu.virginia.sde.reviews;

import java.sql.*;

public class DatabaseDriver {
    private final String sqliteFilename;
    private Connection connection;

    public DatabaseDriver(String sqliteFilename) {
        this.sqliteFilename = sqliteFilename;
    }

    public void connect() throws SQLException {
        // Similar to your previous implementation
        // ...
        if (connection != null && !connection.isClosed()) {
            throw new IllegalStateException("The connection is already opened");
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFilename);
        //the next line enables foreign key enforcement - do not delete/comment out
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        //the next line disables auto-commit - do not delete/comment out
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new IllegalStateException("The connection is not opened");
        }
        connection.commit();
    }

    /**
     * Rollback to the last commit, or when the connection was opened
     */
    public void rollback() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new IllegalStateException("The connection is not opened");
        }
        connection.rollback();
    }

    public void disconnect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new IllegalStateException("The connection is not opened");
        }
        connection.close();
    }

    public void createTables() throws SQLException {
        // Similar to your previous implementation
        // Define tables for Users, Courses, and Reviews
        // ...
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "first_name TEXT NOT NULL,"
                + "last_name TEXT NOT NULL,"
                + "role TEXT NOT NULL"
                + ")");
        statement.execute("CREATE TABLE IF NOT EXISTS courses ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "description TEXT NOT NULL,"
                + "instructor TEXT NOT NULL,"
                + "credits INTEGER NOT NULL"
                + ")");
        statement.execute("CREATE TABLE IF NOT EXISTS reviews ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "course_id INTEGER NOT NULL,"
                + "user_id INTEGER NOT NULL,"
                + "rating INTEGER NOT NULL,"
                + "comment TEXT NOT NULL,"
                + "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ")");
    }



    // Methods for Users, Courses, and Reviews
    // - addUsers
    // - getAllUsers
    // - getUserByUsername
    // - addCourses
    // - getAllCourses
    // - getCourseById
    // - addReviews
    // - getAllReviews
    // - getReviewsByCourse
    // ...
}
