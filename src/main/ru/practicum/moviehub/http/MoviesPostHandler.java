package ru.practicum.moviehub.http;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.api.HttpStatus;
import ru.practicum.moviehub.api.Messages;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

import java.util.List;

public class MoviesPostHandler extends BaseHttpHandler {
    private final MoviesStore store;

    public MoviesPostHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("POST /movies");
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.startsWith("application/json")) {
            sendResponse(exchange, Messages.UNSUPPORTED_MEDIA, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        String body = parseBody(exchange);
        Movie movie;

        try {
            movie = gson.fromJson(body, Movie.class);
        } catch (JsonSyntaxException e) {
            ErrorResponse error = new ErrorResponse(Messages.VALIDATION_ERROR,
                    List.of(Messages.INVALID_JSON));
            String json = gson.toJson(error);
            sendResponse(exchange, json, HttpStatus.UNPROCESSABLE_ENTITY);
            return;
        }

        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            ErrorResponse error = new ErrorResponse(Messages.VALIDATION_ERROR,
                    List.of(Messages.VALIDATION_TITLE_EMPTY));
            String json = gson.toJson(error);
            sendResponse(exchange, json, HttpStatus.UNPROCESSABLE_ENTITY);
            return;
        }

        if (movie.getTitle().length() > 100) {
            ErrorResponse error = new ErrorResponse(Messages.VALIDATION_ERROR,
                    List.of(Messages.VALIDATION_TITLE_TOO_LONG));
            String json = gson.toJson(error);
            sendResponse(exchange, json, HttpStatus.UNPROCESSABLE_ENTITY);
            return;
        }

        if (movie.getYear() < 1888 || movie.getYear() > 2027) {
            ErrorResponse error = new ErrorResponse(Messages.VALIDATION_ERROR,
                    List.of(Messages.VALIDATION_YEAR_RANGE));
            String json = gson.toJson(error);
            sendResponse(exchange, json, HttpStatus.UNPROCESSABLE_ENTITY);
            return;
        }

        movie.setId(store.getNextId());
        store.getMovies().put(movie.getId(), movie);
        String response = gson.toJson(movie);
        sendResponse(exchange, response, HttpStatus.CREATED);
    }
}
