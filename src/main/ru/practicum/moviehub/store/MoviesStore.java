package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Map;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new HashMap<>();
    private int nextId = 1;

    public Map<Integer, Movie> getMovies() {
        return movies;
    }

    public int getNextId() {
        return nextId++;
    }
}