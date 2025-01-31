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
}
