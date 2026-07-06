package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.HttpStatus;
import ru.practicum.moviehub.api.Messages;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.List;

public class MoviesGetAllHandler extends BaseHttpHandler {
    private final MoviesStore store;

    public MoviesGetAllHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("GET /movies");
        String query = exchange.getRequestURI().getQuery();

        if (query == null) {
            List<Movie> allMovies = store.findAll();
            String json = gson.toJson(allMovies);
            sendResponse(exchange, json, HttpStatus.OK);
            return;
        }

        String[] parts = query.split("=");
        if (parts.length != 2 || !"year".equals(parts[0])) {
            sendResponse(exchange, Messages.INVALID_QUERY_PARAM, HttpStatus.BAD_REQUEST);
            return;
        }

        try {
            int year = Integer.parseInt(parts[1]);
            List<Movie> filtered = store.findByYear(year);

            String json = gson.toJson(filtered);
            sendResponse(exchange, json, HttpStatus.OK);
        } catch (NumberFormatException e) {
            sendResponse(exchange, Messages.INVALID_QUERY_YEAR, HttpStatus.BAD_REQUEST);
        }
    }
}
