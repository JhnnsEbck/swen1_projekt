package at.fhtw.mtcgapp.service.session;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class SessionService implements Service {
    private final SessionController sessionController;

    public SessionService()
    {
        this.sessionController = new SessionController();
    }
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.POST) {
            return this.sessionController.loginUser(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
