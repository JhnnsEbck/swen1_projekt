package at.fhtw.mtcgapp.persistence.repository;

import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.exception.DataAccessException;
import at.fhtw.mtcgapp.exception.NoDataException;
import at.fhtw.mtcgapp.exception.NotFoundException;
import at.fhtw.mtcgapp.model.BattleLogs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BattleLogs_rep {

    private UnitOfWork unitOfWork;

    public BattleLogs_rep(UnitOfWork unitOfWork)
    {
        this.unitOfWork = unitOfWork;
    }

    public UnitOfWork getUnitOfWork ()
    {
        return this.unitOfWork;
    }

    public ArrayList<BattleLogs> getBattleLogs(Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                     SELECT Battle_Log.battle_log_id, Battle.user1_id, Battle.user2_id, Battle_Log.log 
                     FROM Battle_Log
                         JOIN Battle
                         ON Battle_log.battle_id = Battle.battle_id
                         WHERE Battle.user1_id = ? OR Battle.user2_id = ?
                         AND log IS NOT NULL;
                             """))
        {
            preparedStatement.setInt(1, user_id);
            preparedStatement.setInt(2, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<BattleLogs> battle_logs = new ArrayList<>();

            while(resultSet.next())
            {
                BattleLogs battle_log = new BattleLogs(
                        resultSet.getInt("battle_log_id"),
                        resultSet.getInt("user1_id"),
                        resultSet.getInt("user2_id"),
                        resultSet.getString("log"));
                battle_logs.add(battle_log);
            }

            if(battle_logs.isEmpty())
            {
                throw new NoDataException("No log-files for user found");
            }

            return battle_logs;

        } catch (SQLException e) {
            throw new DataAccessException("Get log-files of user could not be executed", e);
        }
    }

    public String getDetailedBattleLog(Integer battle_log_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                     SELECT Battle_Log.log FROM Battle_Log
                     WHERE battle_log_id = ?;
                             """))
        {
            preparedStatement.setInt(1, battle_log_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new NotFoundException("No log-files with id: " + battle_log_id +  " found");
            }

            return resultSet.getString(1);

        } catch (SQLException e) {
            throw new DataAccessException("Get log-files of user could not be executed", e);
        }
    }
}
