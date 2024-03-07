package at.fhtw.mtcgapp.persistence.repository;

import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.exception.*;
import at.fhtw.mtcgapp.model.UserStats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Stats_rep {
    private UnitOfWork unitOfWork;
    public Stats_rep(UnitOfWork unitOfWork)
    {
        this.unitOfWork = unitOfWork;
    }

    public UnitOfWork getUnitOfWork ()
    {
        return this.unitOfWork;
    }

    public UserStats getStatsByUserId(Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                SELECT * FROM Users WHERE user_id = ?
                """))
        {
            preparedStatement.setInt(1, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new NoDataException("Stats not found");
            }

            UserStats userStat = new UserStats(
                    resultSet.getString("name"),
                    resultSet.getInt("elo"),
                    resultSet.getInt("wins"),
                    resultSet.getInt("losses"));

            return userStat;

        } catch (SQLException e) {
            throw new DataAccessException("Get User-Stats could not be executed", e);
        }
    }

    public void updateStatsByUserId(Integer user_id, UserStats userStats)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    UPDATE Users
                    SET elo = ?,
                        wins = ?,
                        losses = ?
                    WHERE user_id = ?;
                """))
        {
            preparedStatement.setInt(1, userStats.getElo());
            preparedStatement.setInt(2, userStats.getWins());
            preparedStatement.setInt(3, userStats.getLosses());
            preparedStatement.setInt(4, user_id);
            int updatedRow = preparedStatement.executeUpdate();

            if(updatedRow < 1)
            {
                throw new DataUpdateException("User-Stats could not be updated");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update User-Stats could not be executed", e);
        }
    }
}
