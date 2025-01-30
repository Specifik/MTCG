package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Trading;
import at.fhtw.MTCG.dal.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TradingRepository {
    private final UnitOfWork unitOfWork;

    public TradingRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public List<Trading> getAllTradings() {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "SELECT * FROM tradings")) {
            ResultSet rs = stmt.executeQuery();
            List<Trading> tradings = new ArrayList<>();
            while (rs.next()) {
                tradings.add(new Trading(
                        (UUID) rs.getObject("id"),
                        rs.getInt("user_id"),
                        (UUID) rs.getObject("card_id"),
                        rs.getString("type"),
                        rs.getDouble("min_damage")
                ));
            }
            return tradings;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving trading deals", e);
        }
    }

    public boolean createTrading(Trading trading) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "INSERT INTO tradings (id, user_id, card_id, type, min_damage) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setObject(1, trading.getId());
            stmt.setInt(2, trading.getUserId());
            stmt.setObject(3, trading.getCardId());
            stmt.setString(4, trading.getType());
            stmt.setDouble(5, trading.getMinDamage());
            stmt.executeUpdate();
            unitOfWork.commitTransaction();
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Error creating trading deal", e);
        }
    }

    public boolean deleteTrading(UUID tradingId, int userId) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "DELETE FROM tradings WHERE id = ? AND user_id = ?")) {
            stmt.setObject(1, tradingId);
            stmt.setInt(2, userId);
            int affectedRows = stmt.executeUpdate();
            unitOfWork.commitTransaction();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting trading deal", e);
        }
    }

}
