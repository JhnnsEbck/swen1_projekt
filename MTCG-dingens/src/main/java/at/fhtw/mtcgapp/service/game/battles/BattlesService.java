package at.fhtw.mtcgapp.service.game.battles;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class BattlesService implements Service{
    private final BattlesController battlesController;

    public BattlesService()
    {
        this.battlesController = new BattlesController();
    }
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.POST) {
            return this.battlesController.manageBattle(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
