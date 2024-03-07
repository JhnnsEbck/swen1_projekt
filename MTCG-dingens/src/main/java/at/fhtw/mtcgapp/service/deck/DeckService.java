package at.fhtw.mtcgapp.service.deck;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class DeckService implements Service {
    private final DeckController deckController;

    public DeckService()
    {
        this.deckController = new DeckController();
    }
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.PUT) {
            return this.deckController.configureDeckCardsFromUser(request);
        }
        else if (request.getMethod() == Method.GET && (request.getParams() == null || request.getParams().equals("format=json"))) {
            return this.deckController.getDeckCardsFromUserJSON(request);
        }
        else if (request.getMethod() == Method.GET && request.getParams().equals("format=plain")) {
            return this.deckController.getDeckCardsFromUserPLAIN(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
