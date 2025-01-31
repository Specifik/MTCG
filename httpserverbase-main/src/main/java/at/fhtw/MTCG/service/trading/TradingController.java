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
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

    public Response createTrading(Request request) {
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

            Trading trading = objectMapper.readValue(request.getBody(), Trading.class);

            // Setze die userId im Trading-Objekt
            trading.setUserId(user.getId());

            boolean success = tradingRepository.createTrading(trading);
            if (!success) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Invalid trade request\" }");
            }

            unitOfWork.commitTransaction();
            return new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\": \"Trading deal created successfully\" }");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }


    public Response deleteTrading(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        UUID tradingId = UUID.fromString(request.getPathParts().get(1));

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            TradingRepository tradingRepository = new TradingRepository(unitOfWork);

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
            }

            boolean success = tradingRepository.deleteTrading(tradingId, user.getId());
            if (!success) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"message\": \"Trading deal not found or not owned by user\" }");
            }

            return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\": \"Trading deal deleted successfully\" }");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

    public Response tradeCard(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        UUID tradingId = UUID.fromString(request.getPathParts().get(1));

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            TradingRepository tradingRepository = new TradingRepository(unitOfWork);

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
            }

            Trading trade = tradingRepository.getTradingById(tradingId);
            if (trade == null) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"message\": \"Trading deal not found\" }");
            }

            // Stelle sicher, dass der User nicht seine eigene Karte tauscht
            if (trade.getUserId() == user.getId()) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"You cannot trade with yourself\" }");
            }

            // Extrahiere die angebotene Karte aus der Anfrage
            UUID offeredCardId = objectMapper.readValue(request.getBody(), UUID.class);

            // Überprüfe, ob die angebotene Karte die Bedingungen erfüllt
            boolean isTradeValid = tradingRepository.validateTrade(trade, offeredCardId, user.getId());
            if (!isTradeValid) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Trade failed. Requirements not met\" }");
            }

            // Führe den Tausch durch
            tradingRepository.executeTrade(trade, offeredCardId, user.getId());
            unitOfWork.commitTransaction();

            return new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\": \"Trade successful\" }");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

}
