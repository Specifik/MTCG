package at.fhtw.MTCG.service.scoreboard;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.User;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class ScoreboardController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Response getScoreboard(Request request) {

        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing or invalid authentication token\" }");
        }

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            List<User> users = userRepository.getScoreboard();  // ⬅ Holt ALLE Nutzer sortiert nach ELO

            ArrayNode scoreboardJson = objectMapper.createArrayNode();

            for (User user : users) {
                ObjectNode userJson = objectMapper.createObjectNode();
                userJson.put("username", user.getUsername());
                userJson.put("elo", user.getElo());
                userJson.put("gamesPlayed", user.getGamesPlayed());
                userJson.put("wins", user.getWins());
                userJson.put("losses", user.getLosses());
                scoreboardJson.add(userJson);
            }

            return new Response(HttpStatus.OK, ContentType.JSON, objectMapper.writeValueAsString(scoreboardJson));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }
}
