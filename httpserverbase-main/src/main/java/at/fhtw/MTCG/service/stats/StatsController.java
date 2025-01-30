package at.fhtw.MTCG.service.stats;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.User;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StatsController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Response getStats(Request request) {

        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing or invalid authentication token\" }");
        }

        String cleanToken = token.replace("Bearer ", "");
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            User user = userRepository.findUserByToken(cleanToken);

            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Unauthorized\" }");
            }

            // JSON-Objekt mit nur den Statistikwerten erstellen
            ObjectNode statsJson = objectMapper.createObjectNode();
            statsJson.put("username", user.getUsername());
            statsJson.put("elo", user.getElo());
            statsJson.put("gamesPlayed", user.getGamesPlayed());
            statsJson.put("wins", user.getWins());
            statsJson.put("losses", user.getLosses());

            return new Response(HttpStatus.OK, ContentType.JSON, objectMapper.writeValueAsString(statsJson));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }
}
