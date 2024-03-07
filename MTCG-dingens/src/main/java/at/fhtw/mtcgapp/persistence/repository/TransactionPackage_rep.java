package at.fhtw.mtcgapp.persistence.repository;

import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.exception.DataAccessException;
import at.fhtw.mtcgapp.exception.DataUpdateException;
import at.fhtw.mtcgapp.exception.NoDataException;
import at.fhtw.mtcgapp.exception.InvalidItemException;
import at.fhtw.mtcgapp.model.Card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class TransactionPackage_rep {
    private UnitOfWork unitOfWork;
    public TransactionPackage_rep(UnitOfWork unitOfWork)
    {
        this.unitOfWork = unitOfWork;
    }

    public UnitOfWork getUnitOfWork ()
    {
        return this.unitOfWork;
    }

    public Integer choosePackage()
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                SELECT * FROM Cards WHERE user_id IS NULL LIMIT 1;
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new NoDataException("No card package available for buying");
            }

            return resultSet.getInt("package_id");

        } catch (SQLException e) {
            throw new DataAccessException("Choose Package could not be executed", e);
        }
    }

    public Collection<Card> getCardsFromPackage(Integer package_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                         SELECT Cards.card_id, Cards.card_name, Cards.damage FROM Cards
                               JOIN Package
                               ON Cards.package_id = Package.package_id
                               WHERE Cards.package_id = ?;
                             """))
        {
            preparedStatement.setInt(1, package_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            Collection<Card> cardsInPackage = new ArrayList<>();
            while(resultSet.next())
            {
                Card card = new Card(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getInt(3));
                cardsInPackage.add(card);
            }

            if(cardsInPackage.isEmpty())
            {
                throw new NoDataException("No card package available for buying");
            }

            return cardsInPackage;

        } catch (SQLException e) {
            throw new DataAccessException("Choose Package could not be executed", e);
        }
    }

    public void acquireCardPackage(Integer package_id, Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                          UPDATE Cards
                          SET user_id = ?
                          WHERE package_id = ?;
                             """))
        {
            preparedStatement.setInt(1, user_id);
            preparedStatement.setInt(2, package_id);
            int updatedRow = preparedStatement.executeUpdate();

            if(updatedRow < 1)
            {
                throw new DataUpdateException("Package could not be updated");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Acquire Package could not be executed", e);
        }
    }

    public void updateCoinsByUserId(Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    UPDATE Users
                    SET coins = ?
                    WHERE user_id = ?;
                """))
        {
            int coins = GetCoinsByUserId(user_id);

            if(coins < 1)
            {
                throw new InvalidItemException("Not enough money for buying a card package");
            }

            preparedStatement.setInt(1, (coins - 5));
            preparedStatement.setInt(2, user_id);
            int updatedRow = preparedStatement.executeUpdate();

            if(updatedRow < 1)
            {
                throw new DataUpdateException("Coins could not be updated");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Coins could not be executed", e);
        }
    }

    public Integer GetCoinsByUserId(Integer user_id)
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
                throw new NoDataException("No user with id " + user_id + " found!");
            }

            return resultSet.getInt("coins");

        } catch (SQLException e) {
            throw new DataAccessException("Get Coins from User could not be executed", e);
        }
    }
}
