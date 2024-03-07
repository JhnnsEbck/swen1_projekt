package at.fhtw.mtcgapp.service.battlelogs;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;
import at.fhtw.mtcgapp.service.cards.CardController;

public class BattlelogsService  implements Service {
    private final BattlelogsController battlelogsController;

    public BattlelogsService()
    {
        this.battlelogsController = new BattlelogsController();
    }
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.GET &&
                request.getPathParts().size() > 1) {
            return this.battlelogsController.getDetailedBattleLog(request);
        }
        else if (request.getMethod() == Method.GET) {
            return this.battlelogsController.getBattleLogsFromUser(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
