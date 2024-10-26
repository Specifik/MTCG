package at.fhtw.MTCG.service.user;

import at.fhtw.MTCG.model.User;
import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAL {
    private UnitOfWork unitOfWork;

    public UserDAL(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    // Method to get a user by ID
    public User getUserById(int id) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            preparedStatement.setInt(1, id);
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
            throw new DataAccessException("Error retrieving user by ID", e);
        }
        return null;
    }

    // Method to get all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
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

    // Method to add a new user
    public boolean addUser(User user) {
        try (PreparedStatement preparedStatement = unitOfWork.prepareStatement("INSERT INTO users (username, password, coins) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setInt(3, user.getCoins());
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error adding new user", e);
        }
    }
}
