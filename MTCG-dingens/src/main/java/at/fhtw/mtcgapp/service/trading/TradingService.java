package at.fhtw.mtcgapp.service.trading;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class TradingService implements Service {
    private final TradingController tradingController;

    public TradingService()
    {
        this.tradingController = new TradingController();
    }
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.POST && request.getPathParts().size() == 2) {
            return this.tradingController.carryOutTradingDeal(request);
        }
        if (request.getMethod() == Method.POST) {
            return this.tradingController.createTradingDeal(request);
        }
        else if (request.getMethod() == Method.GET) {
            return this.tradingController.getTradingDeals(request);
        }
        else if (request.getMethod() == Method.DELETE && request.getPathParts().size() > 1) {
            return this.tradingController.deleateTradingDeal(request);
        }
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }

}
