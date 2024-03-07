package at.fhtw;

import at.fhtw.httpserver.server.Service;
import at.fhtw.httpserver.utils.Router;
import at.fhtw.httpserver.server.Server;
import at.fhtw.mtcgapp.service.battlelogs.BattlelogsService;
import at.fhtw.mtcgapp.service.cards.CardService;
import at.fhtw.mtcgapp.service.deck.DeckService;
import at.fhtw.mtcgapp.service.game.battles.BattlesService;
import at.fhtw.mtcgapp.service.game.scoreboard.ScoreboardService;
import at.fhtw.mtcgapp.service.game.stats.StatsService;
import at.fhtw.mtcgapp.service.packages.PackageService;
import at.fhtw.mtcgapp.service.session.SessionService;
import at.fhtw.mtcgapp.service.trading.TradingService;
import at.fhtw.mtcgapp.service.user.UserService;


import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(10001, configureRouter());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Router configureRouter()
    {
        Router router = new Router();

        router.addService("/users", new UserService());
        router.addService("/sessions", new SessionService());
        router.addService("/packages", new PackageService());
        router.addService("/transactions", new PackageService());
        router.addService("/cards", new CardService());
        router.addService("/deck", new DeckService());
        router.addService("/stats", new StatsService());
        router.addService("/score", new ScoreboardService());
        router.addService("/battles", new BattlesService());
        router.addService("/tradings", new TradingService());
        router.addService("/battlelogs", new BattlelogsService());

        return router;
    }
}

