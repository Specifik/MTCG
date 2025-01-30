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

        // Prüfen, ob ALLE Karten dem User gehören
        if (!checkUserOwnsCards(userId, cardIds)) {
            System.out.println("DEBUG: Nicht alle Karten gehören dem User!");
            return false;
        }

        try {
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

            unitOfWork.commitTransaction();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return false;
        }
    }

    public boolean checkUserOwnsCards(int userId, List<UUID> cardIds) {
        try {
            String sql = "SELECT COUNT(*) FROM cards WHERE user_id = ? AND id IN (?, ?, ?, ?)";
            try (PreparedStatement checkStmt = unitOfWork.prepareStatement(sql)) {
                checkStmt.setInt(1, userId);
                for (int i = 0; i < cardIds.size(); i++) {
                    checkStmt.setObject(i + 2, cardIds.get(i));
                }

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int ownedCardCount = rs.getInt(1);

                    // Falls der User nicht genau 4 der übergebenen Karten besitzt, ist die Bedingung nicht erfüllt
                    return ownedCardCount == 4;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
