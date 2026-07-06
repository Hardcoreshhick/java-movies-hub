package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.HttpStatus;
import ru.practicum.moviehub.api.Messages;
import ru.practicum.moviehub.exception.InvalidIdException;
import ru.practicum.moviehub.exception.NotFoundException;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MoviesGetByIdHandler extends BaseHttpHandler {
    private final MoviesStore store;


    public MoviesGetByIdHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("GET /movies/{id}");

        try {
            int id = parseId(exchange);
            Movie movie = store.findById(id)
                    .orElseThrow(() -> new NotFoundException(Messages.MOVIE_NOT_FOUND));

            String json = gson.toJson(movie);
            sendResponse(exchange, json, HttpStatus.OK);
        } catch (InvalidIdException e) {
            sendResponse(exchange, e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            sendResponse(exchange, e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}

