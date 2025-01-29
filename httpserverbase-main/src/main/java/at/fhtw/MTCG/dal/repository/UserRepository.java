package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PSQLException;

public class UserRepository {
    private final UnitOfWork unitOfWork;

    public UserRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    // Method to register a new user
    public boolean registerUser(String username, String password) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "INSERT INTO users (username, password) VALUES (?, ?)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (PSQLException e) {
            if (e.getSQLState().equals("23505")) { // 23505 unique constraint violations in PostgreSQL
                return false; // Username already exists
            } else {
                throw new DataAccessException("Error registering new user", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error registering new user", e);
        }
    }

    // Method to find a user by username
    public User findUserByUsername(String username) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "SELECT * FROM users WHERE username = ?")) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getInt("coins"),
                        resultSet.getString("token"),
                        resultSet.getBoolean("logged_in")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user by username", e);
        }
        return null;
    }

    // Method to find a user by token
    public User findUserByToken(String token) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "SELECT * FROM users WHERE token = ?")) {
            preparedStatement.setString(1, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getInt("coins"),
                        resultSet.getString("token"),
                        resultSet.getBoolean("logged_in")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user by token", e);
        }
        return null;
    }


    public boolean updateUserToken(String token, String username) {
        if (username == null) {
            throw new IllegalArgumentException("username cannot be null");
        }

        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "UPDATE users SET token = ? WHERE username = ?")) {
            if(token == null) {
                preparedStatement.setNull(1, Types.VARCHAR); // set Token to null on logout
            } else {
                preparedStatement.setString(1, token);
            }
            preparedStatement.setString(2, username);
            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error updating user token by username", e);
        }
    }

    public boolean checkUser(String token) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "SELECT * FROM users WHERE token = ?")) {
            preparedStatement.setString(1, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new DataAccessException("No user with token found ", e);
        }
        return false;
    }

    // Method to get all users
    public List<User> findAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement("SELECT * FROM users")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getInt("coins")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all users", e);
        }
        return users;
    }

    // Method to find a user by username and password
    public User findUserByUsernameAndPassword(String username, String hashedPassword) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hashedPassword);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getInt("coins")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user by username and password", e);
        }
        return null;
    }

    // Update user logged-in state
    public void updateUserLoggedInState(String username, boolean loggedIn) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "UPDATE users SET logged_in = ? WHERE username = ?")) {
            preparedStatement.setBoolean(1, loggedIn);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating user logged-in state", e);
        }
    }
}
