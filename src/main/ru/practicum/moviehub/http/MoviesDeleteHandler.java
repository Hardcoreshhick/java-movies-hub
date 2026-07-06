package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.HttpStatus;
import ru.practicum.moviehub.api.Messages;
import ru.practicum.moviehub.exception.InvalidIdException;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.Optional;


public class MoviesDeleteHandler extends BaseHttpHandler {
    private final MoviesStore store;

    public MoviesDeleteHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("DELETE /movies/{id}");
        try {
            int id = parseId(exchange);

            Optional<Movie> movieOpt = store.findById(id);

            if (movieOpt.isEmpty()) {
                sendResponse(exchange, Messages.MOVIE_NOT_FOUND, HttpStatus.NOT_FOUND);
                return;
            }

            store.deleteById(id);
            sendResponse(exchange, Messages.EMPTY, HttpStatus.NO_CONTENT);
        } catch (InvalidIdException e) {
            sendResponse(exchange, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
