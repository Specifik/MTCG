package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Card;
import at.fhtw.MTCG.model.Package;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

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
}
