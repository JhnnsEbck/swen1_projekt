package at.fhtw.mtcgapp.persistence.repository;

import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.exception.DataAccessException;
import at.fhtw.mtcgapp.exception.InvalidItemException;
import at.fhtw.mtcgapp.exception.NoDataException;
import at.fhtw.mtcgapp.model.Card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Deck_rep {
    private UnitOfWork unitOfWork;
    public Deck_rep(UnitOfWork unitOfWork)
    {
        this.unitOfWork = unitOfWork;
    }

    public UnitOfWork getUnitOfWork ()
    {
        return this.unitOfWork;
    }

    public Integer createDeck(Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
               INSERT INTO Deck (deck_id) VALUES(DEFAULT) RETURNING deck_id;
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            //return deck_id
            return resultSet.getInt(1);

        } catch (SQLException e) {
            throw new DataAccessException("Create Deck could not be executed", e);
        }
    }

    public void updateCardsForDeck(Integer user_id, Integer deck_id, String card_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                   UPDATE Cards
                        SET deck_id = ?
                        WHERE user_id = ? AND card_id = ?;
                """))
        {
            preparedStatement.setInt(1, deck_id);
            preparedStatement.setInt(2, user_id);
            preparedStatement.setString(3, card_id);
            int updatedRows = preparedStatement.executeUpdate();

            if(updatedRows < 1)
            {
                throw new InvalidItemException("At least one of the provided card with card-id: " + card_id +" does not belong to the user or is not available.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Cards for Deck could not be executed", e);
        }
    }

    public void updateOldCardDeck(Integer user_id, Integer deck_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        UPDATE Cards
                        SET deck_id = NULL
                        WHERE user_id = ? AND deck_id != ?;
                             """))
        {
            preparedStatement.setInt(1, user_id);
            preparedStatement.setInt(2, deck_id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Update Old-Cards for Deck could not be executed", e);
        }
    }

    public void removeOldDeck(Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                        UPDATE Cards
                        SET deck_id = NULL
                        WHERE user_id = ?;
                             """))
        {
            preparedStatement.setInt(1, user_id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Remove Old-Deck not be executed", e);
        }
    }

    public void deleteOldDeck(Integer deck_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                             DELETE FROM Deck
                                WHERE deck_id = ?
                             """))
        {
            preparedStatement.setInt(1, deck_id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Delete old Deck Deck could not be executed", e);
        }
    }

    public Integer getDeckIdByUserId(Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
               SELECT deck_id From Cards 
                   WHERE user_id = ? 
                   AND deck_id IS NOT NULL;
                """))
        {
            preparedStatement.setInt(1, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                return null;
            }

            return resultSet.getInt(1);

        } catch (SQLException e) {
            throw new DataAccessException("Get Deck-Id by user-id could not be executed", e);
        }
    }

    public ArrayList<Card> getDeckByUserId(Integer user_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
               SELECT card_id, card_name, damage From Cards 
                   WHERE user_id = ? 
                   AND deck_id IS NOT NULL;
                """))
        {
            preparedStatement.setInt(1, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<Card> deckCardList = new ArrayList<>();

            while(resultSet.next())
            {
                Card card = new Card(
                        resultSet.getString("card_id"),
                        resultSet.getString("card_name"),
                        resultSet.getInt("damage")
                );
                deckCardList.add(card);
            }

            if(deckCardList.isEmpty())
            {
                throw new NoDataException("User does not have any cards in his deck");
            }

            return deckCardList;

        } catch (SQLException e) {
            throw new DataAccessException("Get deck by user-id could not be executed", e);
        }
    }
}
