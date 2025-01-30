package at.fhtw.MTCG.service.trading;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.TradingRepository;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.Trading;
import at.fhtw.MTCG.model.User;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

public class TradingController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Response getAllTradings(Request request) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            TradingRepository tradingRepository = new TradingRepository(unitOfWork);
            List<Trading> tradings = tradingRepository.getAllTradings();
            return new Response(HttpStatus.OK, ContentType.JSON, objectMapper.writeValueAsString(tradings));
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

    public Response createTrading(Request request) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            TradingRepository tradingRepository = new TradingRepository(unitOfWork);
            Trading trading = objectMapper.readValue(request.getBody(), Trading.class);
            boolean success = tradingRepository.createTrading(trading);
            return success
                    ? new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\": \"Trading deal created\" }")
                    : new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Could not create trading deal\" }");
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

    public Response deleteTrading(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            TradingRepository tradingRepository = new TradingRepository(unitOfWork);

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
            }

            UUID tradingId = UUID.fromString(request.getPathParts().get(1));
            boolean success = tradingRepository.deleteTrading(tradingId, user.getId());

            return success
                    ? new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\": \"Trading deal deleted\" }")
                    : new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{ \"message\": \"You can only delete your own trading deals\" }");
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

    public Response tradeCard(Request request) {
        System.out.println("DEBUG: `POST /tradings/{id}` wurde im TradingController aufgerufen!");

        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            System.out.println("DEBUG: Kein Token erhalten!");
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            TradingRepository tradingRepository = new TradingRepository(unitOfWork);

            System.out.println("DEBUG: Trade-Request erhalten!");

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                System.out.println("DEBUG: Ungültiger Token!");
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
            }
            System.out.println("DEBUG: User gefunden - ID: " + user.getId());

            UUID tradingId = UUID.fromString(request.getPathParts().get(1));
            UUID offeredCardId = new ObjectMapper().readValue(request.getBody(), UUID.class);

            boolean success = tradingRepository.tradeCard(tradingId, user.getId(), offeredCardId);

            return success
                    ? new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\": \"Trade erfolgreich!\" }")
                    : new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Trade fehlgeschlagen. Anforderungen nicht erfüllt oder ungültiger Trade.\" }");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

}
