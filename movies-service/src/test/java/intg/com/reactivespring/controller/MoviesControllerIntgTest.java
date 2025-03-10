package com.reactivespring.controller;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084) //spin up a HttpServer in port 8084
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }


    @Test
    void retrieveMovieById() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos"+"/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")
                ));

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins" , movie.getMovieInfo().getName());
                });

    }

    @Test
    void retrieveMovieById_404() {
        //given
        var movieId = "abc";
        UrlPattern urlPattern= urlEqualTo("/v1/movieinfos"+"/"+movieId);

        stubFor(get(urlPattern)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")
                ));

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo Available for the passed in Id : abc");

        WireMock.verify(1, getRequestedFor(urlPattern));

    }

    @Test
    void retrieveMovieById_reviews_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos"+"/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                ));

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().isEmpty();
                    assertEquals("Batman Begins" , movie.getMovieInfo().getName());
                });

    }

    @Test
    void retrieveMovieById_5XX() {
        //given
        var movieId = "abc";
        UrlPattern urlPattern= urlEqualTo("/v1/movieinfos"+"/"+movieId);

        stubFor(get(urlPattern)
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")
                ));

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Unavailable");

        //Then
        WireMock.verify(4, getRequestedFor(urlPattern));
    }

    @Test
    void retrieveMovieById_Reviews_5XX() {
        //given
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfos"+"/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Review Service Not Available")
                ));


        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in ReviewsService Review Service Not Available");

        //Then
        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }
}
