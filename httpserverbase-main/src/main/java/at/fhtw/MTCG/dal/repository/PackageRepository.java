package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Card;
import at.fhtw.MTCG.model.Package;
import at.fhtw.MTCG.dal.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class PackageRepository {
    private final UnitOfWork unitOfWork;

    public PackageRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public boolean createPackage(Package cardPackage) {
        try (PreparedStatement packageStmt = unitOfWork.prepareStatement(
                "INSERT INTO packages (id) VALUES (?)")) {

            UUID packageId = UUID.randomUUID();
            packageStmt.setObject(1, packageId);
            packageStmt.executeUpdate();

            for (Card card : cardPackage.getCards()) {
                try (PreparedStatement cardStmt = unitOfWork.prepareStatement(
                        "INSERT INTO cards (id, name, damage, element_type, package_id) VALUES (?, ?, ?, ?, ?)")) {

                    cardStmt.setObject(1, card.getId());
                    cardStmt.setString(2, card.getName());
                    cardStmt.setDouble(3, card.getDamage());
                    cardStmt.setString(4, card.getElementType());
                    cardStmt.setObject(5, packageId);
                    cardStmt.executeUpdate();
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Card> acquirePackage(int userId) {
        try {
            UUID packageId = null;
            try (PreparedStatement findPackageStmt = unitOfWork.prepareStatement(
                    "SELECT id FROM packages ORDER BY RANDOM() LIMIT 1")) {
                ResultSet resultSet = findPackageStmt.executeQuery();
                if (resultSet.next()) {
                    packageId = (UUID) resultSet.getObject("id");
                }
            }

            if (packageId == null) {
                return null; // Kein Paket verfügbar
            }

            List<Card> cards = new ArrayList<>();
            try (PreparedStatement findCardsStmt = unitOfWork.prepareStatement(
                    "SELECT * FROM cards WHERE package_id = ?")) {
                findCardsStmt.setObject(1, packageId);
                ResultSet resultSet = findCardsStmt.executeQuery();
                while (resultSet.next()) {
                    Card card = new Card(
                            (UUID) resultSet.getObject("id"),
                            resultSet.getString("name"),
                            resultSet.getDouble("damage"),
                            resultSet.getString("element_type"),
                            packageId
                    );
                    cards.add(card);
                }
            }

            try (PreparedStatement updateOwnerStmt = unitOfWork.prepareStatement(
                    "UPDATE cards SET user_id = ?, package_id = NULL WHERE package_id = ?")) {
                updateOwnerStmt.setInt(1, userId);
                updateOwnerStmt.setObject(2, packageId);
                updateOwnerStmt.executeUpdate();
            }

            try (PreparedStatement deletePackageStmt = unitOfWork.prepareStatement(
                    "DELETE FROM packages WHERE id = ?")) {
                deletePackageStmt.setObject(1, packageId);
                deletePackageStmt.executeUpdate();
            }

            return cards;
        } catch (SQLException e) {
            throw new DataAccessException("Error acquiring package", e);
        }
    }
}
