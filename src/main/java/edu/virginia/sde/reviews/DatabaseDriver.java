package edu.virginia.sde.reviews;

import java.sql.*;
import java.util.*;

public class DatabaseDriver {

    private static DatabaseDriver instance;
    private final String sqliteFilename;
    private Connection connection;

    public DatabaseDriver (String sqlListDatabaseFilename) {
        this.sqliteFilename = sqlListDatabaseFilename;
    }

    public static DatabaseDriver getInstance(String sqliteFilename) {
        if (instance == null) {
            instance = new DatabaseDriver(sqliteFilename);
        }
        return instance;
    }

    /**
     * Opens a connection to the database
     */
    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFilename);
        //the next line enables foreign key enforcement - do not delete/comment out
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        //the next line disables auto-commit - do not delete/comment out
        connection.setAutoCommit(true);
    }

    /**
     * Commit all changes since the connection was opened OR since the last commit/rollback
     */
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

    /**
     * Ends the connection to the database
     */
    public void disconnect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new IllegalStateException("The connection is not opened");
        }
        connection.close();
    }

    /**
     * Creates the Courses, Users, and Reviews tables in the database
     */
    public void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createCoursesTable = "CREATE TABLE IF NOT EXISTS Courses (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Subject VARCHAR(255) NOT NULL, " +
                    "CourseNumber INTEGER NOT NULL, " +
                    "Title VARCHAR(255) NOT NULL)";
            statement.executeUpdate(createCoursesTable);

            String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Username VARCHAR(255) NOT NULL UNIQUE, " +
                    "Password VARCHAR(255) NOT NULL)";
            statement.executeUpdate(createUsersTable);

            String createReviewsTable = "CREATE TABLE IF NOT EXISTS Reviews (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "UserID INTEGER NOT NULL, " +
                    "CourseID INTEGER NOT NULL, " +
                    "Rating INTEGER NOT NULL, " +
                    "EntryTime TIMESTAMP NOT NULL, " +
                    "Comment VARCHAR(255) NOT NULL, " +
                    "FOREIGN KEY (UserID) REFERENCES Users(ID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (CourseID) REFERENCES Courses(ID) ON DELETE CASCADE)";
            statement.executeUpdate(createReviewsTable);
        }
    }


    /**
     * Adds a course to the database
     * @param course The course to add
     */
    public void addCourse(Course course) throws SQLException {
        try {
            if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
            String command = "INSERT INTO Courses(Subject, CourseNumber, Title) VALUES(?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(command)) {
                statement.setString(1, course.getSubject());
                statement.setInt(2, course.getCourseNumber());
                statement.setString(3, course.getTitle());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    /**
     * Checks if a course already exists in the database
     * @param subject
     * @param courseNumber
     * @param title
     * @return True if the course already exists, false otherwise
     */
    public boolean courseAlreadyExists(String subject, int courseNumber, String title) throws SQLException {
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is not open");
        }
        String query = "SELECT COUNT(*) FROM Courses WHERE Subject = ? AND CourseNumber = ? AND Title = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subject);
            statement.setInt(2, courseNumber);
            statement.setString(3, title);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Course> getAllCoursesWithRatings() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT c.ID, c.Subject, c.CourseNumber, c.Title, AVG(r.Rating) as AverageRating " +
                "FROM Courses c " +
                "LEFT JOIN Reviews r ON c.ID = r.CourseID " +
                "GROUP BY c.ID, c.Subject, c.CourseNumber, c.Title";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                int id = results.getInt("ID");
                String subject = results.getString("Subject");
                int courseNumber = results.getInt("CourseNumber");
                String title = results.getString("Title");
                Double averageRating = results.getDouble("AverageRating");
                if (results.wasNull()) {
                    averageRating = null; // Set to null if no reviews
                }
                Course newCourse = new Course(id, subject, courseNumber, title, averageRating);
                courses.add(newCourse);
            }
        }
        return courses;
    }



    /**
     * Gets a course by its ID
     * @param courseID The ID of the course
     * @return The course with the given ID, or an empty Optional if no course exists with that ID
     */
    public Optional<Course> getCourseById(int courseID) throws SQLException {
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        String query = "SELECT * FROM Courses WHERE ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, courseID);
            try (ResultSet results = preparedStatement.executeQuery()) {
                if (results.next()) {
                    int id = results.getInt("ID");
                    String subject = results.getString("Subject");
                    int courseNumber = results.getInt("CourseNumber");
                    String title = results.getString("Title");
                    Course newCourse = new Course(id, subject, courseNumber, title, null);
                    return Optional.of(newCourse);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Gets a course by its subject, course number, and title
     * @param subject
     * @param number
     * @param title
     * @return The course with the given subject, course number, and title,
     * or an empty Optional if no course exists with that subject, course number, and title
     */
    public int getCourseId(String subject, int number, String title) throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * from Courses where Subject=? AND CourseNumber=? AND Title=?");
            statement.setString(1, subject);
            statement.setInt(2, number);
            statement.setString(3, title);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                return results.getInt("ID");
            }
            statement.close();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new SQLException("No course with subject, courseNumber, and title found.");
    }

    /**
     * Adds a user to the database
     * @param user The user to add
     */
    public void addUser(User user) throws SQLException{
        try {
            if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
            String command = "INSERT INTO Users(Username, Password) VALUES(?, ?)";
            PreparedStatement statement = connection.prepareStatement(command);

            // Set parameters
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());

            // Execute the update
            statement.executeUpdate();
            statement.close();

            // Get the last inserted ID
            Statement lastIdStatement = connection.createStatement();
            ResultSet rs = lastIdStatement.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                int generatedId = rs.getInt(1); // Retrieve the generated ID
                user.setUserID(generatedId); // Update the User object with the new ID
            }
            lastIdStatement.close();
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    /**
     * Checks if a user exists in the database
     * @param username The username of the user
     * @return True if the user exists, false otherwise
     */
    public boolean userExists(String username)throws SQLException{
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is not open");
        }
        boolean exists = false;
        String query = "SELECT COUNT(*) FROM Users WHERE Username = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    exists = count > 0;
                }
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return exists;
    }

    /**
     * Gets the ID for a user
     * @param username The username of the user
     * @return The ID for the user
     */
    public int getUserId(String username) throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * from Users where Username=?");
            statement.setString(1, username);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                return results.getInt("ID");
            }
            statement.close();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new SQLException("No user with username: " + username + "found.");
    }

    /**
     * Gets a user by their username
     * @param username The username of the user
     * @return The user with the given username, or an empty Optional if no user exists with that username
     */
    public Optional<User> getUserByUsername(String username) throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * from Users where username=?");
            statement.setString(1, username);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                int id = results.getInt("ID");
                String username2 = results.getString("Username");
                String password = results.getString("Password");
                User user = new User(username2, password);
                user.setUserID(id);
                return Optional.of(user);
            }
            statement.close();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /**
     * Gets the password for a user
     * @param user The user to get the password for
     * @return The password for the user, or an empty Optional if the user does not exist
     */
    public Optional<String> getPasswordForUser(User user) throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * from Users where username=?");
            statement.setString(1, user.getUsername());
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                String password = results.getString("Password");
                return Optional.of(password);
            }
            statement.close();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /**
     * Adds a review to the database
     * @param review The review to add
     */
    public void addReview(Review review) throws SQLException{
        try {
            if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
            String command = "INSERT INTO Reviews(UserID, CourseID, Rating, EntryTime, Comment) " +
                    "VALUES(?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(command)) {
                statement.setInt(1, review.getUserID());
                statement.setInt(2, review.getCourseID());
                statement.setInt(3, review.getRating());
                statement.setTimestamp(4, review.getEntryTime());
                if (review.getComment() != null) {
                    statement.setString(5, review.getComment());
                } else {
                    statement.setNull(5, Types.VARCHAR);
                }
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error in addReview: " + e.getMessage());
            System.err.println("Review details: UserID=" + review.getUserID() + ", CourseID=" + review.getCourseID());
            rollback();
            throw e;
        }
    }

    /**
     * Removes a review to the database
     * @param course The course to remove the review from
     * @param user The user who wrote the review
     */
    public void removeReview(Course course, User user) throws SQLException{
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is not open");
        }

        try {
            String deleteQuery = "DELETE FROM Reviews WHERE CourseID = ? AND UserID = ?";
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setInt(1, getCourseId(course.getSubject(), course.getCourseNumber(), course.getTitle()));
            statement.setInt(2, getUserId(user.getUsername()));
            int rowsAffected = statement.executeUpdate();
            statement.close();

            if (rowsAffected == 0) {
                throw new SQLException("No review found for Course ID: " + course.getCourseID()
                        + " and User ID: " + user.getUserID());
            }
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    /**
     * Edits a review in the database
     * @param oldReview The review to edit
     * @param newReview The new review to replace the old review
     */
    public void editReview(Review oldReview, Review newReview) throws SQLException{
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is not open");
        }

        // Update the review
        String updateQuery = "UPDATE Reviews SET Rating = ?, EntryTime = ?, Comment = ? WHERE UserID = ? AND CourseID = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            // Set new values for the review
            updateStatement.setInt(1, newReview.getRating());
            updateStatement.setTimestamp(2, newReview.getEntryTime());
            if (newReview.getComment() != null) {
                updateStatement.setString(3, newReview.getComment());
            } else {
                updateStatement.setNull(3, Types.VARCHAR);
            }

            // Use the User ID and Course ID from the old review to identify the review to update
            updateStatement.setInt(4, oldReview.getCourseID());
            updateStatement.setInt(5, oldReview.getUserID());

            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No review found for Course ID: " + oldReview.getCourseID()
                        + " and User ID: " + oldReview.getUserID());
            }
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    /**
     * Gets the reviews for a course
     * @param course The course to get the reviews for
     * @return The reviews for the course
     */
    public List<Review> getReviewsForCourse(Course course) throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        List<Review> reviews = new ArrayList<>();
        try {
            String query = "SELECT * FROM Reviews WHERE CourseID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getCourseId(course.getSubject(),course.getCourseNumber(),course.getTitle())); // Assuming getId() returns the course ID

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                int id = results.getInt("ID");
                int userId = results.getInt("UserID");
                int courseId = results.getInt("CourseID");
                int rating = results.getInt("Rating");
                Timestamp time = results.getTimestamp("EntryTime");
                String comment = results.getString("Comment");
                if (results.wasNull()) {
                    comment = null;
                }

                Review review = new Review(id, userId, courseId, rating, time, comment);
                reviews.add(review);
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reviews;
    }

    /**
     * Gets the reviews from a user
     * @param user The user who wrote the reviews
     * @return The reviews from the user
     */
    public List<Review> getReviewsFromUser(User user) throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        List<Review> reviews = new ArrayList<>();
        try {
            String query = "SELECT * FROM Reviews WHERE UserID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserId(user.getUsername())); // Assuming getId() returns the course ID

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                int id = results.getInt("ID");
                int userId = results.getInt("UserID");
                int courseId = results.getInt("CourseID");
                int rating = results.getInt("Rating");
                Timestamp time = results.getTimestamp("EntryTime");
                String comment = results.getString("Comment");
                if (results.wasNull()) {
                    comment = null;
                }

                Review review = new Review(id, userId, courseId, rating, time, comment);
                reviews.add(review);
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reviews;
    }

    /**
     * Gets the review from a user for a course
     * @param user The user who wrote the review
     * @param course The course that was reviewed
     * @return The review from the user for the course, or an empty Optional if the user has not reviewed the course
     */
    public Optional <Review> getReviewFromUserForCourse(User user, Course course) throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        try {
            String query = "SELECT * FROM Reviews WHERE UserID = ? AND CourseID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getUserId(user.getUsername()));
            statement.setInt(2, getCourseId(course.getSubject(),course.getCourseNumber(),course.getTitle()));

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                setupCourseColumns courses = getSetupCourseColumns(results);
                return Optional.of(new Review(courses.id(), courses.courseId(), courses.userId(), courses.rating(), courses.time(), courses.comment()));
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private static setupCourseColumns getSetupCourseColumns(ResultSet results) throws SQLException {
        int id = results.getInt("ID");
        int userId = results.getInt("UserID");
        int courseId = results.getInt("CourseID");
        int rating = results.getInt("Rating");
        Timestamp time = results.getTimestamp("EntryTime");
        String comment = results.getString("Comment");
        if (results.wasNull()) {
            comment = null;
        }
        return new setupCourseColumns(id, userId, courseId, rating, time, comment);
    }

    private record setupCourseColumns(int id, int userId, int courseId, int rating, Timestamp time, String comment) {
    }

    /**
     * Calculates the average rating for a course
     * @param course The course to calculate the average rating for
     * @return The average rating for the course, or an empty OptionalDouble if there are no reviews for the course
     */
    public OptionalDouble calculateAverageReviewForCourse(Course course) throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        try {
            String query = "SELECT AVG(Rating) AS AverageRating FROM Reviews WHERE CourseID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, getCourseId(course.getSubject(),course.getCourseNumber(),course.getTitle())); // Assuming getId() returns the course ID

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                double averageRating = results.getDouble("AverageRating");
                if (results.wasNull()) {
                    return OptionalDouble.empty();
                }
                return OptionalDouble.of(Math.round(averageRating * 100.0) / 100.0); // Rounded to two decimal places
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return OptionalDouble.empty();
    }

    /**
     * Clears all data from the tables
     */
    public void clearTables() throws SQLException{
        if (connection.isClosed()) throw new IllegalStateException("Connection is not open");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM Reviews");
            statement.executeUpdate("DELETE FROM Users");
            statement.executeUpdate("DELETE FROM Courses");
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

}
