package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Trading;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradingRepository {
    private final UnitOfWork unitOfWork;

    public TradingRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public List<Trading> getAllTradings() {
        List<Trading> tradings = new ArrayList<>();
        try (PreparedStatement stmt = unitOfWork.prepareStatement("SELECT * FROM tradings")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tradings.add(createTradingFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving trading deals", e);
        }
        return tradings;
    }

    public boolean createTrading(Trading trading) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "INSERT INTO tradings (id, user_id, card_id, type, min_damage) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setObject(1, trading.getId());
            stmt.setInt(2, trading.getUserId());
            stmt.setObject(3, trading.getCardId());
            stmt.setString(4, trading.getType());
            stmt.setDouble(5, trading.getMinDamage());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error creating trading deal", e);
        }
    }

    public boolean deleteTrading(UUID tradingId, int userId) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "DELETE FROM tradings WHERE id = ? AND user_id = ?")) {
            stmt.setObject(1, tradingId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                unitOfWork.commitTransaction();
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting trading deal", e);
        }
    }

    private Trading createTradingFromResultSet(ResultSet rs) throws SQLException {
        return new Trading(
                (UUID) rs.getObject("id"),
                rs.getInt("user_id"),
                (UUID) rs.getObject("card_id"),
                rs.getString("type"),
                rs.getDouble("min_damage")
        );
    }

    public Trading getTradingById(UUID tradingId) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "SELECT * FROM tradings WHERE id = ?")) {
            stmt.setObject(1, tradingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return createTradingFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving trading deal", e);
        }
        return null;
    }

    public boolean validateTrade(Trading trade, UUID offeredCardId, int userId) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "SELECT id, damage, element_type FROM cards WHERE id = ? AND user_id = ?")) {
            stmt.setObject(1, offeredCardId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double damage = rs.getDouble("damage");
                String elementType = rs.getString("element_type");
                boolean isMonster = !elementType.contains("spell");

                return damage >= trade.getMinDamage() && trade.getType().equalsIgnoreCase(isMonster ? "monster" : "spell");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error validating trade", e);
        }
        return false;
    }

    public void executeTrade(Trading trade, UUID offeredCardId, int newOwnerId) {
        try {
            // Setze die gehandelte Karte zum neuen Besitzer
            try (PreparedStatement stmt = unitOfWork.prepareStatement(
                    "UPDATE cards SET user_id = ? WHERE id = ?")) {
                stmt.setInt(1, newOwnerId);
                stmt.setObject(2, trade.getCardId());
                stmt.executeUpdate();
            }

            // Setze die angebotene Karte zum vorherigen Besitzer des Trades
            try (PreparedStatement stmt = unitOfWork.prepareStatement(
                    "UPDATE cards SET user_id = ? WHERE id = ?")) {
                stmt.setInt(1, trade.getUserId());
                stmt.setObject(2, offeredCardId);
                stmt.executeUpdate();
            }

            // Entferne das Trading-Angebot
            try (PreparedStatement stmt = unitOfWork.prepareStatement(
                    "DELETE FROM tradings WHERE id = ?")) {
                stmt.setObject(1, trade.getId());
                stmt.executeUpdate();
            }

            unitOfWork.commitTransaction();
        } catch (SQLException e) {
            unitOfWork.rollbackTransaction();
            throw new DataAccessException("Error executing trade", e);
        }
    }
}
