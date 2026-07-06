package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.HttpStatus;
import ru.practicum.moviehub.api.Messages;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesGetAllHandler getAllHandler;
    private final MoviesPostHandler postHandler;
    private final MoviesGetByIdHandler getByIdHandler;
    private final MoviesDeleteHandler deleteHandler;

    public MoviesHandler(MoviesStore store) {
        this.getAllHandler = new MoviesGetAllHandler(store);
        this.postHandler = new MoviesPostHandler(store);
        this.getByIdHandler = new MoviesGetByIdHandler(store);
        this.deleteHandler = new MoviesDeleteHandler(store);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                if (path.matches("/movies/\\d+")) {
                    getByIdHandler.handle(exchange);
                } else if ("/movies".equals(path)) {
                    getAllHandler.handle(exchange);
                } else {
                    sendResponse(exchange, Messages.NOT_FOUND, HttpStatus.NOT_FOUND);
                }
                break;
            case "POST":
                postHandler.handle(exchange);
                break;
            case "DELETE":
                deleteHandler.handle(exchange);
                break;
            default:
                sendResponse(exchange, Messages.METHOD_NOT_SUPPORTED, HttpStatus.METHOD_NOT_ALLOWED);
        }
    }
}
