package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new HashMap<>();
    private int nextId = 1;

    public Movie save(Movie movie) {
        movie.setId(nextId++);
        movies.put(movie.getId(), movie);
        return movie;
    }

    public Optional<Movie> findById(int id) {
        return Optional.ofNullable(movies.get(id));
    }

    public List<Movie> findAll() {
        return List.copyOf(movies.values());
    }

    public Optional<Movie> deleteById(int id) {
        return Optional.ofNullable(movies.remove(id));
    }

    public List<Movie> findByYear(int year) {
        return movies.values().stream()
                .filter(m -> m.getYear() == year)
                .collect(Collectors.toUnmodifiableList());
    }
}