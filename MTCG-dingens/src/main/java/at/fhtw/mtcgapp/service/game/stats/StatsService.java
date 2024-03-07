package at.fhtw.mtcgapp.service.game.stats;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class StatsService implements Service {
    private final StatsController statsController;

    public StatsService()
    {
        this.statsController = new StatsController();
    }
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.GET) {
            return this.statsController.getStatsByUserId(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
