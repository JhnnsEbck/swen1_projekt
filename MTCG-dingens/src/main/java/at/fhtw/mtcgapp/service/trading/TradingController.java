package at.fhtw.mtcgapp.service.trading;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtcgapp.controller.Controller;
import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.persistence.repository.Session_rep;
import at.fhtw.mtcgapp.persistence.repository.Trading_rep;
import at.fhtw.mtcgapp.persistence.repository.User_rep;
import at.fhtw.mtcgapp.exception.*;
import at.fhtw.mtcgapp.model.TradingDeal;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Collection;

public class TradingController extends Controller {

    public Response createTradingDeal(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();

        try (unitOfWork) {

            new Session_rep(unitOfWork).checkIfTokenIsValid(request);
            int user_id = new Session_rep(unitOfWork).getUserIdByToken(request);
            TradingDeal tradingDeal = this.getObjectMapper().readValue(request.getBody(), TradingDeal.class);

            new Trading_rep(unitOfWork).createTradingDeal(tradingDeal);
            new Trading_rep(unitOfWork).updateCardForCreateTradingDeal(tradingDeal, user_id);
            unitOfWork.commitTransaction();

            return  new Response(
                    HttpStatus.CREATED,
                    ContentType.PLAIN_TEXT,
                    "Trading deal successfully created"
            );
        }
        catch (JsonProcessingException exception) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (InvalidLoginDataException e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        }
        catch (ConstraintViolationException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "A deal with this deal ID already exists."
            );
        }
        catch (InvalidItemException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The deal contains a card that is not owned by the user or locked in the deck."
            );
        }
        catch (DataAccessException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        }
        catch (Exception e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }

    public Response getTradingDeals(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();

        try (unitOfWork) {

            new Session_rep(unitOfWork).checkIfTokenIsValid(request);
            int user_id = new Session_rep(unitOfWork).getUserIdByToken(request);
            Collection<TradingDeal> tradingDeals = new Trading_rep(unitOfWork).getAllTradingDeals();

            unitOfWork.commitTransaction();

            String tradingDealsJSON = this.getObjectMapper().writeValueAsString(tradingDeals);

            return  new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    tradingDealsJSON
            );
        }
        catch (JsonProcessingException exception) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (InvalidLoginDataException e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        }
        catch (NoDataException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    //Mit Status 204 wird die Nachricht nicht angezeigt
                    HttpStatus.BAD_REQUEST,
                    ContentType.PLAIN_TEXT,
                    "The request was fine, but there are no trading deals available"
            );
        }
        catch (DataAccessException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        }
        catch (Exception e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }

    public Response deleateTradingDeal(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();

        try (unitOfWork) {

            new Session_rep(unitOfWork).checkIfTokenIsValid(request);
            int user_id = new Session_rep(unitOfWork).getUserIdByToken(request);
            String trading_id = request.getPathParts().get(1);

            new Trading_rep(unitOfWork).updateTradingDealCardForDelete(user_id, trading_id);
            new Trading_rep(unitOfWork).deleteTradingDeal(trading_id);

            unitOfWork.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "Trading deal successfully deleted"
            );
        }
        catch (JsonProcessingException exception) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (InvalidLoginDataException e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        }
        catch (DataUpdateException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The deal contains a card that is not owned by the user."
            );
        }
        catch (InvalidItemException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The deal contains a card that is not owned by the user or locked in the deck."
            );
        }
        catch (DataAccessException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        }
        catch (Exception e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }

    public Response carryOutTradingDeal(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();

        try (unitOfWork) {
            System.out.println("1");

            new Session_rep(unitOfWork).checkIfTokenIsValid(request);
            int buyerUser_id = new Session_rep(unitOfWork).getUserIdByToken(request);


            String trading_id = request.getPathParts().get(1);
            String offeredCardId = this.getObjectMapper().readValue(request.getBody(), String.class);
            TradingDeal tradingDeal = new Trading_rep(unitOfWork).getTradingDealByTradingId(trading_id);
            Integer sellerUser_id = new User_rep(unitOfWork).getUserIdByCardId(tradingDeal.getCard_to_trade());

            //check if trade is valid
            new Trading_rep(unitOfWork).checkOfferedCardForTrading(tradingDeal, buyerUser_id, sellerUser_id, offeredCardId);

            // changing cardOwner
            new Trading_rep(unitOfWork).updateCardForCarryOutTradingDeal(buyerUser_id, sellerUser_id, tradingDeal.getCard_to_trade());
            new Trading_rep(unitOfWork).updateCardForCarryOutTradingDeal(sellerUser_id, buyerUser_id, offeredCardId);

            new Trading_rep(unitOfWork).deleteTradingDeal(tradingDeal.getTrading_id());

            unitOfWork.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "Trading deal successfully executed."
            );
        }
        catch (JsonProcessingException exception) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.PLAIN_TEXT,
                    "Internal Server Error"
            );
        }
        catch (InvalidLoginDataException e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.PLAIN_TEXT,
                    "Authentication information is missing or invalid"
            );
        }
        catch (NotFoundException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.PLAIN_TEXT,
                    "The provided deal ID was not found."
            );
        }
        catch (InvalidItemException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck."
            );
        }
        catch (DataAccessException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Database Server Error"
            );
        }
        catch (Exception e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "Internal Server Error"
            );
        }
    }
}
