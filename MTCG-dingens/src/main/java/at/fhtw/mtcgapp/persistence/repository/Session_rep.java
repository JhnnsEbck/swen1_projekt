package at.fhtw.mtcgapp.persistence.repository;

import at.fhtw.httpserver.server.Request;
import at.fhtw.mtcgapp.persistence.UnitOfWork;
import at.fhtw.mtcgapp.exception.*;
import at.fhtw.mtcgapp.model.UserCredentials;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Session_rep {
    private UnitOfWork unitOfWork;
    public Session_rep(UnitOfWork unitOfWork)
    {
        this.unitOfWork = unitOfWork;
    }

    public UnitOfWork getUnitOfWork ()
    {
        return this.unitOfWork;
    }

    public void loginUser(String username, Integer user_id) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                INSERT INTO Tokens (user_id, token) VALUES (?, ?)
                """))
        {
            preparedStatement.setInt(1, user_id);
            preparedStatement.setString(2, username + "-mtcgToken");
            int updatedRows = preparedStatement.executeUpdate();

            if(updatedRows < 1)
            {
                throw new DataUpdateException("Update Token could not be executed");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update Token could not be executed", e);
        }
    }

    public Integer getUserIdByLoginData(UserCredentials user)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                SELECT * FROM Users WHERE username = ? AND password = ?
                """))
        {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next() == false)
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }
            return resultSet.getInt("user_id");

        } catch (SQLException e) {
            throw new DataAccessException("Get User by username and password could not be executed", e);
        }
    }

    public void checkIfTokenAndUsernameIsValid(String username, Request request)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                SELECT * FROM Tokens JOIN Users ON Tokens.user_id = Users.user_id 
                WHERE users.username = ? AND Tokens.token = ?
                """))
        {
            if(request.getHeaderMap().getAuthorizationTokenHeader() == null || request.getHeaderMap().getAuthorizationTokenHeader().isEmpty())
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, request.getHeaderMap().getAuthorizationTokenHeader().substring(6));
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next() == false)
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Check User by token and username could not be executed", e);
        }
    }

    public void checkIfTokenIsValid(Request request)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                SELECT * FROM Tokens WHERE token = ?
                """))
        {
            if(request.getHeaderMap().getAuthorizationTokenHeader() == null || request.getHeaderMap().getAuthorizationTokenHeader().isEmpty())
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            preparedStatement.setString(1, request.getHeaderMap().getAuthorizationTokenHeader().substring(6));
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next())
            {
                throw new InvalidLoginDataException("Authentication information is missing or invalid");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Check User by token could not be executed", e);
        }
    }

    public void checkIfTokenIsAdmin(Request request)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                SELECT * FROM Tokens WHERE Tokens.token = ?
                """))
        {
            if(request.getHeaderMap().getAuthorizationTokenHeader() == null || request.getHeaderMap().getAuthorizationTokenHeader().isEmpty())
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            preparedStatement.setString(1, request.getHeaderMap().getAuthorizationTokenHeader().substring(6));
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!(request.getHeaderMap().getAuthorizationTokenHeader().substring(6).startsWith("admin")))
            {
                throw new AccessRightsTooLowException("Provided user is not \"admin\"");
            }
            if(resultSet.next() == false)
            {
                throw new InvalidLoginDataException("Authentication information is missing or invalid");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Check Admin by token could not be executed", e);
        }
    }

    public Integer getUserIdByToken(Request request)
    {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                SELECT * FROM Tokens WHERE token = ?
                """))
        {
            if(request.getHeaderMap().getAuthorizationTokenHeader() == null || request.getHeaderMap().getAuthorizationTokenHeader().isEmpty())
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            preparedStatement.setString(1, request.getHeaderMap().getAuthorizationTokenHeader().substring(6));
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next() == false)
            {
                throw new InvalidLoginDataException("Invalid username/password provided");
            }

            return resultSet.getInt("user_id");

        } catch (SQLException e) {
            throw new DataAccessException("Get User by username and password could not be executed", e);
        }
    }
}
