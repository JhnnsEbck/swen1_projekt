package at.fhtw.mtcgapp.service.game.battles;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtcgapp.controller.Controller;
import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.persistence.repository.*;
import at.fhtw.mtcgapp.exception.*;
import at.fhtw.mtcgapp.model.Card;
import at.fhtw.mtcgapp.model.User;
import at.fhtw.mtcgapp.model.UserStats;
import at.fhtw.mtcgapp.service.game.battles.battleImplementations.Battle;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattlesController extends Controller {
    public Response manageBattle(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();

        try (unitOfWork) {

            new Session_rep(unitOfWork).checkIfTokenIsValid(request);

            String battle_log = "";

            // first player if Lobby is empty
            if (new Battles_rep(unitOfWork).isLobbyEmpty()) {
                int firstPlayerUser_id = new Session_rep(unitOfWork).getUserIdByToken(request);
                int battle_lobby_id = new Battles_rep(unitOfWork).createBattleLobby(firstPlayerUser_id);
                unitOfWork.commitTransaction();

                // new uow is necessary -> second player has to see the updated lobby
                UnitOfWork secondUnitOfWork = new UnitOfWork();
                boolean isBattleDone = false;
                // finish after count = timeout -> no player found
                int count = 0;
                int timeout = 28;

                do {
                    // checks every sec the battle status
                    Thread.sleep(1000);
                    isBattleDone = new Battles_rep(secondUnitOfWork).isBattleDone(battle_lobby_id);
                    ++count;

                } while (!isBattleDone && count < timeout);

                if (isBattleDone) {
                    // if battle is done -> response is battle log
                    battle_log = new Battles_rep(secondUnitOfWork).getBattleLog(battle_lobby_id);
                } else if (count >= timeout) {
                    //deletes the empty lobby if no second player was found
                    new Battles_rep(secondUnitOfWork).deleteEmptyBattleById(battle_lobby_id);
                    secondUnitOfWork.commitTransaction();
                    throw new NoPlayerFoundException("No player found");
                }

                secondUnitOfWork.commitTransaction();
            }
            else // second Player if Lobby is not empty
            {
                int maxRounds = 100;
                int secondPlayerUser_id = new Session_rep(unitOfWork).getUserIdByToken(request);
                int battle_lobby_id = new Battles_rep(unitOfWork).JoinBattleLobbyWithSecondPlayer(secondPlayerUser_id);

                List<User> usersFromBattle = new Battles_rep(unitOfWork).getUsersFromBattle(battle_lobby_id);
                List<Card> firstPlayerDeck = new Deck_rep(unitOfWork).getDeckByUserId(usersFromBattle.get(0).getId());
                List<Card> secondPlayerDeck = new Deck_rep(unitOfWork).getDeckByUserId(usersFromBattle.get(1).getId());

                // battle logic in class -> method calculateWinner(List<Card> deckCardsForRound)
                Battle battle = new Battle(battle_lobby_id, usersFromBattle.get(0), usersFromBattle.get(1));
                battle.setBattleLog("Battle: " + usersFromBattle.get(0).getUsername() + " vs " + usersFromBattle.get(1).getUsername() + "\n");
                int rounds = 0;

                for (rounds = 1; rounds < maxRounds + 1; ++rounds) {
                    if (firstPlayerDeck.isEmpty() || secondPlayerDeck.isEmpty()) {
                        break;
                    }
                    Collections.shuffle(firstPlayerDeck);
                    Collections.shuffle(secondPlayerDeck);

                    battle.setBattleLog("\nRound " + rounds + ":\n");

                    //deck cards for round
                    List<Card> deckCardsForRound = new ArrayList<>();
                    deckCardsForRound.add(firstPlayerDeck.get(0));
                    deckCardsForRound.add(secondPlayerDeck.get(0));

                    String winner = battle.calculateWinner(deckCardsForRound);

                    // handle win and change cards in List
                    if (winner.equals("playerA")) {
                        battle.setBattleLog("=> " + deckCardsForRound.get(0).getName() + " wins");
                        Card deckCard = secondPlayerDeck.get(0);
                        secondPlayerDeck.remove(deckCard);
                        firstPlayerDeck.add(deckCard);
                    } else if (winner.equals("playerB")) {
                        battle.setBattleLog("=> " + deckCardsForRound.get(1).getName() + " wins");
                        Card deckCard = firstPlayerDeck.get(0);
                        firstPlayerDeck.remove(deckCard);
                        secondPlayerDeck.add(deckCard);
                    } else if (winner.equals("draw")) {
                        battle.setBattleLog("=> Draw");
                    } else {
                        throw new NoDataException("No winner could be calculated");
                    }
                    battle.setBattleLog("\nDeck-Card Amount of " + usersFromBattle.get(0).getUsername() + ": " + firstPlayerDeck.size() +
                                        " vs " + usersFromBattle.get(1).getUsername() + ": " + secondPlayerDeck.size() +"\n");
                }

                //change gained cards and userstats if game result is not a draw
                if (rounds < (maxRounds-1)) {
                    //change card owner in db if the new owner of the deck cards should be the winner
                    if (!firstPlayerDeck.isEmpty()) {
                        for (Card card : firstPlayerDeck) {
                            new Card_rep(unitOfWork).updateCardOwner(usersFromBattle.get(0).getId(), card.getCard_id());
                        }
                    }
                    if (!secondPlayerDeck.isEmpty()) {
                        for (Card card : secondPlayerDeck) {
                            new Card_rep(unitOfWork).updateCardOwner(usersFromBattle.get(1).getId(), card.getCard_id());
                        }
                    }

                    //update Deck
                    Integer playerAoldDeck_id = new Deck_rep(unitOfWork).getDeckIdByUserId(usersFromBattle.get(0).getId());
                    Integer playerBoldDeck_id = new Deck_rep(unitOfWork).getDeckIdByUserId(usersFromBattle.get(1).getId());
                    new Deck_rep(unitOfWork).removeOldDeck(usersFromBattle.get(0).getId());
                    new Deck_rep(unitOfWork).removeOldDeck(usersFromBattle.get(1).getId());

                    if (playerAoldDeck_id != null) {
                        new Deck_rep(unitOfWork).deleteOldDeck(playerAoldDeck_id);
                    }
                    if (playerBoldDeck_id != null) {
                        new Deck_rep(unitOfWork).deleteOldDeck(playerBoldDeck_id);
                    }

                    // update userStats
                    if(firstPlayerDeck.size() > secondPlayerDeck.size())
                    {
                        //update winner
                        UserStats firstPlayerUserStats = new Stats_rep(unitOfWork).getStatsByUserId(usersFromBattle.get(0).getId());
                        firstPlayerUserStats.setEloWinner();
                        firstPlayerUserStats.increaseWins();
                        new Stats_rep(unitOfWork).updateStatsByUserId(usersFromBattle.get(0).getId(), firstPlayerUserStats);

                        //update looser
                        UserStats secondPlayerUserStats = new Stats_rep(unitOfWork).getStatsByUserId(usersFromBattle.get(1).getId());
                        secondPlayerUserStats.setEloLooser();
                        secondPlayerUserStats.increaseLooses();
                        new Stats_rep(unitOfWork).updateStatsByUserId(usersFromBattle.get(1).getId(), secondPlayerUserStats);

                        battle.setBattleLog("\n=> " + usersFromBattle.get(0).getUsername() + " wins");
                    }
                    else if(firstPlayerDeck.size() < secondPlayerDeck.size())
                    {
                        //update winner
                        UserStats secondPlayerUserStats = new Stats_rep(unitOfWork).getStatsByUserId(usersFromBattle.get(1).getId());
                        secondPlayerUserStats.setEloWinner();
                        secondPlayerUserStats.increaseWins();
                        new Stats_rep(unitOfWork).updateStatsByUserId(usersFromBattle.get(1).getId(), secondPlayerUserStats);

                        //update looser
                        UserStats firstPlayerUserStats = new Stats_rep(unitOfWork).getStatsByUserId(usersFromBattle.get(0).getId());
                        firstPlayerUserStats.setEloLooser();
                        firstPlayerUserStats.increaseLooses();
                        new Stats_rep(unitOfWork).updateStatsByUserId(usersFromBattle.get(0).getId(), firstPlayerUserStats);

                        battle.setBattleLog("\n=> " + usersFromBattle.get(1).getUsername() + " wins");
                    }
                }
                else
                {
                    battle.setBattleLog("=> Draw");
                }

                // battle log
                battle_log = battle.getBattle_log();
                new Battles_rep(unitOfWork).updateBattleStatusToFinished(battle_lobby_id);
                new Battles_rep(unitOfWork).createBattleLog(battle_log, battle_lobby_id);

                unitOfWork.commitTransaction();
            }

            return new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    battle_log

            );
        } catch (JsonProcessingException exception) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        } catch (InvalidLoginDataException e) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        } catch (NotFoundException e) {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.NO_CONTENT,
                    ContentType.PLAIN_TEXT,
                    "No Data found"
            );
        } catch (NoDataException e) {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.NO_CONTENT,
                    ContentType.PLAIN_TEXT,
                    "User does not have any cards in his deck"
            );
        } catch (NoPlayerFoundException e) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.PLAIN_TEXT,
                    "No player found"
            );
        } catch (DataAccessException e) {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        } catch (Exception e) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }
}
