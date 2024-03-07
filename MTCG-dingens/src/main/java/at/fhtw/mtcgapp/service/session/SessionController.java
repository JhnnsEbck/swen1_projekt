package at.fhtw.mtcgapp.service.session;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtcgapp.controller.Controller;
import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.persistence.repository.Session_rep;
import at.fhtw.mtcgapp.exception.DataAccessException;
import at.fhtw.mtcgapp.exception.DataUpdateException;
import at.fhtw.mtcgapp.exception.InvalidLoginDataException;
import at.fhtw.mtcgapp.model.UserCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SessionController extends Controller {

    public Response loginUser(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();

        try (unitOfWork) {


            UserCredentials user = this.getObjectMapper().readValue(request.getBody(), UserCredentials.class);
            Integer user_id = new Session_rep(unitOfWork).getUserIdByLoginData(user);
            unitOfWork.commitTransaction();

            if(user_id == null)
            {
                throw new InvalidLoginDataException("Invalid login data");
            }

            new Session_rep(unitOfWork).loginUser(user.getUsername(), user_id);
            unitOfWork.commitTransaction();

            return  new Response(
                    HttpStatus.OK,
                    ContentType.PLAIN_TEXT,
                    "User login successful"
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
                    "Invalid username/password provided"
            );
        }
        catch (DataUpdateException e)
        {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.PLAIN_TEXT,
                    "Update the token could not be executed."
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
