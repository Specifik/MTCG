package at.fhtw;

import at.fhtw.MTCG.service.battle.BattleService;
import at.fhtw.MTCG.service.card.CardService;
import at.fhtw.MTCG.service.deck.DeckService;
import at.fhtw.MTCG.service.stats.StatsService;
import at.fhtw.MTCG.service.scoreboard.ScoreboardService;
import at.fhtw.MTCG.service.packages.PackageService;
import at.fhtw.MTCG.service.trading.TradingService;
import at.fhtw.httpserver.server.Server;
import at.fhtw.httpserver.utils.Router;
import at.fhtw.MTCG.service.echo.EchoService;
import at.fhtw.MTCG.service.user.UserService;

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
        router.addService("/sessions", new UserService());
        router.addService("/cards", new CardService());
        router.addService("/packages", new PackageService());
        router.addService("/transactions/packages", new PackageService());
        router.addService("/deck", new DeckService());
        router.addService("/stats", new StatsService());
        router.addService("/scoreboard", new ScoreboardService());
        router.addService("/tradings", new TradingService());
        router.addService("/echo", new EchoService());

        return router;
    }
}
