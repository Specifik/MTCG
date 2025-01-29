package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Deck;
import at.fhtw.MTCG.dal.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeckRepository {
    private final UnitOfWork unitOfWork;

    public DeckRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public Deck getUserDeck(int userId) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "SELECT card_id FROM deck WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<UUID> cardIds = new ArrayList<>();
            while (rs.next()) {
                cardIds.add((UUID) rs.getObject("card_id"));
            }
            return new Deck(userId, cardIds);
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving deck", e);
        }
    }

    public boolean updateDeck(int userId, List<UUID> cardIds) {
        if (cardIds.size() != 4) {
            throw new IllegalArgumentException("Deck must contain exactly 4 cards.");
        }

        try {
            String sql = "SELECT id FROM cards WHERE user_id = ? AND id IN (?, ?, ?, ?)";
            try (PreparedStatement checkStmt = unitOfWork.prepareStatement(sql)) {
                checkStmt.setInt(1, userId);
                for (int i = 0; i < cardIds.size(); i++) {
                    checkStmt.setObject(i + 2, cardIds.get(i)); // Startet bei Index 2, weil Index 1 `user_id` ist
                }

                ResultSet rs = checkStmt.executeQuery();

                List<UUID> validCards = new ArrayList<>();
                while (rs.next()) {
                    validCards.add((UUID) rs.getObject("id"));
                }

                if (validCards.size() != 4) {
                    throw new DataAccessException("Not all selected cards belong to the user or exist in the database. Found: "
                            + validCards.size() + " of 4 expected.");
                }
            }

            try (PreparedStatement deleteStmt = unitOfWork.prepareStatement("DELETE FROM deck WHERE user_id = ?")) {
                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = unitOfWork.prepareStatement(
                    "INSERT INTO deck (user_id, card_id) VALUES (?, ?)")) {
                for (UUID cardId : cardIds) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setObject(2, cardId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Error updating deck", e);
        }
    }

}
