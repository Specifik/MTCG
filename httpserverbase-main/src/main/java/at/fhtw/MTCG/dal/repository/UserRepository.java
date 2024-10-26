package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PSQLException;

public class UserRepository {
    private UnitOfWork unitOfWork;

    public UserRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    // Method to register a new user
    public boolean registerUser(String username, String password) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "INSERT INTO users (username, password) VALUES (?, ?)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password); // should be hashed
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (PSQLException e) {
            if (e.getSQLState().equals("23505")) { // 23505 is the SQL state for unique constraint violations in PostgreSQL
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
                        resultSet.getInt("coins")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user by username", e);
        }
        return null;
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

    // Method to validate user login
    public boolean loginUser(String username, String password) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new DataAccessException("Error during user login", e);
        }
    }

    // Method to find a user by username and password
    public User findUserByUsernameAndPassword(String username, String password) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
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
}
