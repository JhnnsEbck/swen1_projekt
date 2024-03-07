package at.fhtw.mtcgapp.persistence.repository;

import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.exception.*;
import at.fhtw.mtcgapp.model.TradingDeal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class Trading_rep {
    private UnitOfWork unitOfWork;

    public Trading_rep(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public UnitOfWork getUnitOfWork() {
        return this.unitOfWork;
    }

    public void createTradingDeal(TradingDeal tradingDeal) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                             INSERT INTO Trading (trading_id, card_to_trade, card_type, minimum_damage) VALUES (?, ?, ?, ?)
                             """)) {
            preparedStatement.setString(1, tradingDeal.getTrading_id());
            preparedStatement.setString(2, tradingDeal.getCard_to_trade());
            preparedStatement.setString(3, tradingDeal.getCard_type());
            preparedStatement.setInt(4, tradingDeal.getMinimum_damage());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                throw new ConstraintViolationException("A deal with this deal ID already exists.");
            } else {
                throw new DataAccessException("Create Trading-Deal could not be executed", e);

            }
        }
    }

    public void updateCardForCreateTradingDeal(TradingDeal tradingDeal, Integer user_id) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                                UPDATE Cards
                                     SET trading_id = ?
                                     WHERE user_id = ? AND card_id = ? AND deck_id IS NULL AND trading_id IS NULL;
                             """)) {
            preparedStatement.setString(1, tradingDeal.getTrading_id());
            preparedStatement.setInt(2, user_id);
            preparedStatement.setString(3, tradingDeal.getCard_to_trade());
            int updatedRows = preparedStatement.executeUpdate();

            if (updatedRows < 1) {
                throw new InvalidItemException("The deal contains a card that is not owned by the user or locked in the deck.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Cards for Trading-Deal could not be executed", e);
        }
    }

    public Collection<TradingDeal> getAllTradingDeals() {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                             SELECT Cards.trading_id, Cards.card_id, Trading.card_type, Trading.minimum_damage
                                 FROM Trading JOIN Cards
                                 ON Trading.trading_id = Cards.trading_id
  
                                  """)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<TradingDeal> tradingDeals = new ArrayList<>();

            while (resultSet.next()) {
                TradingDeal tradingDeal = new TradingDeal(
                        resultSet.getString("trading_id"),
                        resultSet.getString("card_id"),
                        resultSet.getString("card_type"),
                        resultSet.getInt("minimum_damage"));
                tradingDeals.add(tradingDeal);
            }

            if (tradingDeals.isEmpty()) {
                throw new NoDataException("The request was fine, but there are no trading deals available");
            }

            return tradingDeals;

        } catch (SQLException e) {
            throw new DataAccessException("Get all Trading-Deals could not be executed", e);
        }
    }

    public TradingDeal getTradingDealByTradingId(String trading_id) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                             SELECT Trading.trading_id, Cards.card_id, Trading.card_type, Trading.minimum_damage
                                 FROM Trading JOIN Cards
                                     ON Trading.trading_id = Cards.trading_id 
                                 WHERE Trading.trading_id = ?
                                      """)) {
            preparedStatement.setString(1, trading_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("No Trading-Deal with Trading-Id: " + trading_id + " found");
            }

            TradingDeal tradingDeal = new TradingDeal(
                    resultSet.getString("trading_id"),
                    resultSet.getString("card_id"),
                    resultSet.getString("card_type"),
                    resultSet.getInt("minimum_damage"));

            return tradingDeal;

        } catch (SQLException e) {
            throw new DataAccessException("Get Trading-Deals by id could not be executed", e);
        }
    }

    public void deleteTradingDeal(String trading_id) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                             DELETE FROM Trading
                                WHERE trading_id = ?;
                                 """)) {
            preparedStatement.setString(1, trading_id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Delete Trading-Deal could not be executed", e);
        }
    }

    public void updateTradingDealCardForDelete(Integer user_id, String trading_id) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                             UPDATE Cards
                             SET trading_id = NULL
                             WHERE user_id = ? AND trading_id = ?;
                                  """)) {
            preparedStatement.setInt(1, user_id);
            preparedStatement.setString(2, trading_id);
            int updatedRows = preparedStatement.executeUpdate();

            if (updatedRows < 1) {
                throw new DataUpdateException("Update could not be executed");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Trading-Deal Card for Delete could not be executed", e);
        }
    }

    public void checkOfferedCardForTrading(TradingDeal tradingDeal, Integer buyerUser_id, Integer sellerUser_id, String card_id) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                                    SELECT card_id, card_name, damage From Cards 
                                         WHERE user_id = ? 
                                         AND user_id != ?
                                         AND card_id = ?
                                         And deck_id IS NULL
                                         AND card_type = ?
                                         AND damage >= ?;
                             """)) {
            preparedStatement.setInt(1, buyerUser_id);
            preparedStatement.setInt(2, sellerUser_id);
            preparedStatement.setString(3, card_id);
            preparedStatement.setString(4, tradingDeal.getCard_type());
            preparedStatement.setInt(5, tradingDeal.getMinimum_damage());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new InvalidItemException("The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Carry out Trading-Deal could not be executed", e);
        }
    }

    public void updateCardForCarryOutTradingDeal(Integer newOwnerUser_id, Integer oldOwnerUser_id, String card_id)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                   UPDATE Cards
                        SET trading_id = NULL,
                            user_id = ?
                        WHERE user_id = ? AND card_id = ?;
                """))
        {
            preparedStatement.setInt(1, newOwnerUser_id);
            preparedStatement.setInt(2, oldOwnerUser_id);
            preparedStatement.setString(3, card_id);
            int updatedRows = preparedStatement.executeUpdate();

            if(updatedRows < 1)
            {
                throw new InvalidItemException("Cards for carry out Trading-Deal was not found");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Cards for carry out Trading-Deal could not be executed", e);
        }
    }
}

