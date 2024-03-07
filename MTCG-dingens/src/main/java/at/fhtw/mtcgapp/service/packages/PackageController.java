package at.fhtw.mtcgapp.service.packages;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.controller.Controller;
import at.fhtw.mtcgapp.persistence.repository.Package_rep;
import at.fhtw.mtcgapp.persistence.repository.Session_rep;
import at.fhtw.mtcgapp.persistence.repository.TransactionPackage_rep;
import at.fhtw.mtcgapp.exception.*;
import at.fhtw.mtcgapp.model.Card;
import com.fasterxml.jackson.core.JsonProcessingException;


public class PackageController extends Controller{
    public Response createPackage(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();

        try (unitOfWork) {
            new Session_rep(unitOfWork).checkIfTokenIsAdmin(request);

            Card cards[] = this.getObjectMapper().readValue(request.getBody(), Card[].class);
            if(cards.length != 5)
            {
                throw new InvalidDataException("The provided package did not include the required amount of cards");
            }

            new Package_rep(unitOfWork).createPackage(cards);

            unitOfWork.commitTransaction();

            return  new Response(
                    HttpStatus.CREATED,
                    ContentType.PLAIN_TEXT,
                    "Package and cards successfully created"
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
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "At least one card in the packages already exists"
            );
        }
        catch (AccessRightsTooLowException e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "Provided user is not \"admin\""
            );
        }
        catch (InvalidDataException e)
        {
            unitOfWork.rollbackTransaction();
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.PLAIN_TEXT,
                    "The provided package did not include the required amount of cards"
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

    public Response acquireCardPackage(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();

        try (unitOfWork) {
            new Session_rep(unitOfWork).checkIfTokenIsValid(request);

            int package_id = new TransactionPackage_rep(unitOfWork).choosePackage();
            int user_id = new Session_rep(unitOfWork).getUserIdByToken(request);

            //acquire package
            new TransactionPackage_rep(unitOfWork).acquireCardPackage(package_id, user_id);
            new TransactionPackage_rep(unitOfWork).updateCoinsByUserId(user_id);

            unitOfWork.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "A package has been successfully bought"
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
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.PLAIN_TEXT,
                    "No card package available for buying"
            );
        }
        catch (DataUpdateException e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Update data was not successfully"
            );
        }
        catch (InvalidItemException e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.PLAIN_TEXT,
                    "Not enough money for buying a card package"
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
