package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Card;
import at.fhtw.MTCG.dal.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardRepository {
    private final UnitOfWork unitOfWork;

    public CardRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public List<Card> getUserCards(int userId) {
        try (PreparedStatement stmt = unitOfWork.prepareStatement(
                "SELECT id, name, damage, element_type, package_id, user_id FROM cards WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            List<Card> cards = new ArrayList<>();
            while (rs.next()) {
                cards.add(new Card(
                        (UUID) rs.getObject("id"),
                        rs.getString("name"),
                        rs.getDouble("damage"),
                        rs.getString("element_type"),
                        (UUID) rs.getObject("package_id"),
                        rs.getInt("user_id")
                ));
            }
            return cards;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving cards", e);
        }
    }
}
