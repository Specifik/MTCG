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
            int packageResult = packageStmt.executeUpdate();
            System.out.println("DEBUG: Package INSERT Result = " + packageResult);
            System.out.println("DEBUG: Created Package ID = " + packageId);

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

                    int cardResult = cardStmt.executeUpdate();
                    System.out.println("DEBUG: Card INSERT Result = " + cardResult + " for Card ID " + card.getId());
                }
            }

            unitOfWork.commitTransaction();
            System.out.println("DEBUG: Transaction committed.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return false;
        }
    }

    public List<Card> acquirePackage(int userId) {
        try {
            System.out.println("DEBUG: User " + userId + " versucht, ein Package zu kaufen...");

            UUID packageId = null;
            try (PreparedStatement findPackageStmt = unitOfWork.prepareStatement(
                    "SELECT id FROM packages ORDER BY created_at ASC LIMIT 1")) { // Ändere Reihenfolge
                ResultSet resultSet = findPackageStmt.executeQuery();
                if (resultSet.next()) {
                    packageId = (UUID) resultSet.getObject("id");
                    System.out.println("DEBUG: Gefundenes Package ID: " + packageId);
                }
            }

            if (packageId == null) {
                System.out.println("DEBUG: Kein verfügbares Package gefunden!");
                return null;
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
                return null;
            }

            System.out.println("DEBUG: " + cards.size() + " Karten gefunden. Aktualisiere Kartenbesitz...");
            try (PreparedStatement updateOwnerStmt = unitOfWork.prepareStatement(
                    "UPDATE cards SET user_id = ?, package_id = NULL WHERE package_id = ?")) {
                updateOwnerStmt.setInt(1, userId);
                updateOwnerStmt.setObject(2, packageId);
                int updatedRows = updateOwnerStmt.executeUpdate();
                System.out.println("DEBUG: Kartenbesitz für " + updatedRows + " Karten geändert.");
            }

            try (PreparedStatement deletePackageStmt = unitOfWork.prepareStatement(
                    "DELETE FROM packages WHERE id = ?")) {
                deletePackageStmt.setObject(1, packageId);
                int deletedRows = deletePackageStmt.executeUpdate();
                System.out.println("DEBUG: Paket gelöscht. Anzahl gelöschter Zeilen: " + deletedRows);
            }

            unitOfWork.commitTransaction();
            return cards;
        } catch (SQLException e) {
            throw new DataAccessException("Error acquiring package", e);
        }
    }
}
