package at.fhtw.MTCG.service.battle;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.BattleRepository;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.User;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;

public class BattleController {
    public Response startBattle(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            BattleRepository battleRepository = new BattleRepository(unitOfWork);

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
            }

            Integer opponentId = battleRepository.findOpponent(user.getId());

            if (opponentId == null) {
                battleRepository.addToBattleQueue(user.getId());
                return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\": \"Waiting for opponent...\" }");
            }

            int winnerId = (Math.random() < 0.5) ? user.getId() : opponentId;
            battleRepository.saveBattle(user.getId(), opponentId, winnerId);

            return new Response(HttpStatus.OK, ContentType.JSON,
                    "{ \"message\": \"Battle finished! Winner: " + winnerId + "\" }");
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }
}
