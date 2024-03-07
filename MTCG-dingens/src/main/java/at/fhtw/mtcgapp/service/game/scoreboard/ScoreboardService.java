package at.fhtw.mtcgapp.service.game.scoreboard;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;
import at.fhtw.mtcgapp.service.game.stats.StatsController;

public class ScoreboardService implements Service {
    private final ScoreboardController scoreboardController;

    public ScoreboardService()
    {
        this.scoreboardController = new ScoreboardController();
    }
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.GET) {
            return this.scoreboardController.getScoreboard(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
