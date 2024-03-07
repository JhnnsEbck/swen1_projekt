package at.fhtw.mtcgapp.persistence.repository;

import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.exception.DataAccessException;
import at.fhtw.mtcgapp.exception.NoDataException;
import at.fhtw.mtcgapp.model.UserStats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class Scoreboard_rep {
    private UnitOfWork unitOfWork;
    public Scoreboard_rep(UnitOfWork unitOfWork)
    {
        this.unitOfWork = unitOfWork;
    }

    public UnitOfWork getUnitOfWork ()
    {
        return this.unitOfWork;
    }

    public Collection<UserStats> getScoreboard()
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                SELECT * FROM Users ORDER BY elo DESC;
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<UserStats> userStatsList = new ArrayList<>();

            while(resultSet.next())
            {
                UserStats userStats = new UserStats(
                        resultSet.getString("name"),
                        resultSet.getInt("elo"),
                        resultSet.getInt("wins"),
                        resultSet.getInt("losses"));
                userStatsList.add(userStats);
            }

            if(userStatsList.isEmpty())
            {
                throw new NoDataException("No Entries for Scoreboard found");
            }

            return userStatsList;

        } catch (SQLException e) {
            throw new DataAccessException("Get Scoreboard could not be executed", e);
        }
    }
}
