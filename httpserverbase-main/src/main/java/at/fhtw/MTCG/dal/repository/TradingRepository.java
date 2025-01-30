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

    public boolean tradeCard(UUID tradingId, int userId, UUID offeredCardId) {
        try {
            System.out.println("DEBUG: Starte Trade-Prozess");
            System.out.println("DEBUG: Trading-ID: " + tradingId);
            System.out.println("DEBUG: User-ID: " + userId);
            System.out.println("DEBUG: Offered Card-ID: " + offeredCardId);

            // Prüfen, ob das Trading-Angebot existiert
            Trading trading;
            try (PreparedStatement stmt = unitOfWork.prepareStatement(
                    "SELECT * FROM tradings WHERE id = ?")) {
                stmt.setObject(1, tradingId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("DEBUG: Kein Trading-Angebot gefunden.");
                    return false;
                }
                trading = new Trading(
                        (UUID) rs.getObject("id"),
                        rs.getInt("user_id"),
                        (UUID) rs.getObject("card_id"),
                        rs.getString("type"),
                        rs.getDouble("min_damage")
                );
            }
            System.out.println("DEBUG: Gefundenes Trading-Angebot: " + trading.getId());

            //Sicherstellen, dass der Benutzer nicht gegen sich selbst tradet
            if (trading.getUserId() == userId) {
                System.out.println("DEBUG: User versucht gegen sich selbst zu traden!");
                return false;
            }

            // Prüfen, ob der Benutzer die angebotene Karte besitzt
            try (PreparedStatement stmt = unitOfWork.prepareStatement(
                    "SELECT id, name, damage, element_type FROM cards WHERE id = ? AND user_id = ?")) {
                stmt.setObject(1, offeredCardId);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("DEBUG: Die angebotene Karte gehört nicht dem User.");
                    return false;
                }

                // Prüfen, ob die Karte die Trading-Bedingungen erfüllt
                String elementType = rs.getString("element_type");
                double damage = rs.getDouble("damage");
                boolean isMonster = !elementType.contains("spell");

                if (!trading.getType().equals(isMonster ? "monster" : "spell") || damage < trading.getMinDamage()) {
                    System.out.println("DEBUG: Die angebotene Karte erfüllt nicht die Bedingungen!");
                    return false;
                }
            }
            System.out.println("DEBUG: Karte erfüllt alle Bedingungen, Trade wird durchgeführt...");

            // Kartenbesitzer tauschen
            try (PreparedStatement stmt = unitOfWork.prepareStatement(
                    "UPDATE cards SET user_id = ? WHERE id = ?")) {
                stmt.setInt(1, trading.getUserId());
                stmt.setObject(2, offeredCardId);
                stmt.executeUpdate();

                stmt.setInt(1, userId);
                stmt.setObject(2, trading.getCardId());
                stmt.executeUpdate();
            }

            // Trading-Angebot aus der Datenbank entfernen
            try (PreparedStatement stmt = unitOfWork.prepareStatement(
                    "DELETE FROM tradings WHERE id = ?")) {
                stmt.setObject(1, tradingId);
                stmt.executeUpdate();
            }

            unitOfWork.commitTransaction();
            System.out.println("DEBUG: Trade erfolgreich abgeschlossen!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error processing trade", e);
        }
    }
}
