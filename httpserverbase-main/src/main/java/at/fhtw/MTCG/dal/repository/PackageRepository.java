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
                // Prüfen, ob die Karte bereits existiert
                boolean cardExists = false;
                try (PreparedStatement checkStmt = unitOfWork.prepareStatement(
                        "SELECT id FROM cards WHERE id = ?")) {
                    checkStmt.setObject(1, card.getId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        cardExists = true;
                    }
                }

                if (cardExists) {
                    System.out.println("DEBUG: Karte mit ID " + card.getId() + " existiert bereits und wird nicht erneut eingefügt.");
                    continue;
                }

                try (PreparedStatement cardStmt = unitOfWork.prepareStatement(
                        "INSERT INTO cards (id, name, damage, element_type, package_id) VALUES (?, ?, ?, ?, ?)")) {

                    cardStmt.setObject(1, card.getId());
                    cardStmt.setString(2, card.getName());
                    cardStmt.setDouble(3, card.getDamage());

                    String elementType = card.getElementType() != null ? card.getElementType() : "normal";
                    cardStmt.setString(4, elementType);
                    cardStmt.setObject(5, packageId);

                    cardStmt.executeUpdate();
                }
            }

            unitOfWork.commitTransaction();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return false;
        }
    }

    public boolean acquirePackage(int userId) {
        try {
            UUID packageId = null;
            try (PreparedStatement findPackageStmt = unitOfWork.prepareStatement(
                    "SELECT id FROM packages ORDER BY created_at ASC LIMIT 1")) { // Änderung Reihenfolge
                ResultSet resultSet = findPackageStmt.executeQuery();
                if (resultSet.next()) {
                    packageId = (UUID) resultSet.getObject("id");
                }
            }

            if (packageId == null) {
                System.out.println("DEBUG: Kein verfügbares Package gefunden!");
                return false;
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
                            packageId,
                            resultSet.getInt("user_id")
                    );
                    cards.add(card);
                }
            }

            if (cards.isEmpty()) {
                System.out.println("DEBUG: Keine Karten in diesem Package gefunden!");
                return false;
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

            unitOfWork.commitTransaction();
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Error acquiring package", e);
        }
    }
}
