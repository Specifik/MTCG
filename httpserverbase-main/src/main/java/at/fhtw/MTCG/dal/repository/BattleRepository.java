package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BattleRepository {
    private final UnitOfWork unitOfWork;

    public BattleRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public Integer findOpponent(int userId) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "SELECT user_id FROM battles_waiting WHERE user_id != ? AND EXISTS " +
                        "(SELECT 1 FROM deck WHERE deck.user_id = battles_waiting.user_id) LIMIT 1")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int opponentId = rs.getInt("user_id");
                System.out.println("DEBUG: Found opponent - " + opponentId);

                // Warten, bis der Battle wirklich gestartet wird
                try (PreparedStatement deleteStmt = unitOfWork.prepareStatement(
                        "DELETE FROM battles_waiting WHERE user_id = ?")) {
                    deleteStmt.setInt(1, opponentId);
                    deleteStmt.executeUpdate();
                    System.out.println("DEBUG: Removed opponent " + opponentId + " from queue");
                }
                return opponentId;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding opponent", e);
        }
        return null;
    }

    public void addToBattleQueue(int userId) {
        System.out.println("DEBUG: Adding user " + userId + " to battle queue");

        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "INSERT INTO battles_waiting (user_id) VALUES (?)")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            unitOfWork.commitTransaction();
            System.out.println("DEBUG: User " + userId + " added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error adding user to battle queue", e);
        }
    }

    public void saveBattle(int user1Id, int user2Id, Integer winnerId) {
        try {
            // Gewinner aus der Warteschlange entfernen (falls er noch drin ist)
            try (PreparedStatement deleteStmt = unitOfWork.prepareStatement(
                    "DELETE FROM battles_waiting WHERE user_id = ?")) {
                deleteStmt.setInt(1, user1Id);
                deleteStmt.executeUpdate();
                deleteStmt.setInt(1, user2Id);
                deleteStmt.executeUpdate();
            }

            // Battle in die Datenbank speichern
            try (PreparedStatement stmt = unitOfWork.prepareStatement(
                    "INSERT INTO battles (user1_id, user2_id, winner_id) VALUES (?, ?, ?)")) {
                stmt.setInt(1, user1Id);
                stmt.setInt(2, user2Id);
                stmt.setObject(3, winnerId); // Kann NULL sein, falls Unentschieden
                stmt.executeUpdate();
            }

            unitOfWork.commitTransaction(); // Transaktion abschließen
            System.out.println("DEBUG: Battle gespeichert - Winner: " + winnerId);
        } catch (SQLException e) {
            throw new DataAccessException("Error saving battle", e);
        }
    }

}
