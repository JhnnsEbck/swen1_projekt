package at.fhtw.mtcgapp.service.game.battles.battleImplementations;

import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtcgapp.service.game.battles.BattlesService;

//help class to test battle service for Unit-Tests
public class MyThread implements Runnable{
    private Response response;
    private Request request;

    public MyThread(Request request)
    {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public void run() {
        this.response = null;
        BattlesService battlesService = new BattlesService();
        this.response = battlesService.handleRequest(this.request);
    }
}
