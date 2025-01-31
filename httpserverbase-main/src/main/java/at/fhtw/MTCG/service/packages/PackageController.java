package at.fhtw.MTCG.service.packages;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.PackageRepository;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.Package;
import at.fhtw.MTCG.model.User;
import at.fhtw.MTCG.model.Card;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class PackageController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    private final UnitOfWork unitOfWork;

    public PackageController(PackageRepository packageRepository, UserRepository userRepository, UnitOfWork unitOfWork) {
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
        this.unitOfWork = unitOfWork;
    }

    public Response createPackage(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        User user = userRepository.findUserByToken(token);
        if (user == null || !user.getUsername().equals("admin")) {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{ \"message\": \"Only admin can create packages\" }");
        }

        try {
            List<Card> cards = objectMapper.readValue(request.getBody(), new TypeReference<>() {});
            if (cards.size() != 5) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"A package must contain exactly 5 cards\" }");
            }

            boolean success = packageRepository.createPackage(new Package(cards));
            unitOfWork.commitTransaction();

            return success
                    ? new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\": \"Package created successfully\" }")
                    : new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Could not create package\" }");

        } catch (Exception e) {
            unitOfWork.rollbackTransaction();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

    public Response acquirePackage(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        User user = userRepository.findUserByToken(token);
        if (user == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
        }

        if (user.getCoins() < 5) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Not enough coins\" }");
        }

        boolean packageAcquired = packageRepository.acquirePackage(user.getId());
        if (!packageAcquired) {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"message\": \"No packages available\" }");
        }

        userRepository.updateUserCoins(user.getUsername(), user.getCoins() - 5);
        unitOfWork.commitTransaction();

        return new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\": \"Package successfully acquired\" }");
    }
}
