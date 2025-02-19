package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    static String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {

        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        //given
        Review review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    var savedReview = movieReviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    System.out.println(savedReview);
                    assert savedReview.getReviewId() != null;
                });

        //then
    }

    @Test
    void addReview_validation() {
        //given
        Review review = new Review(null, null, "Awesome Movie", -9.0);

        //when
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null,rating.negative : rating is negative and please pass a non-negative value");

        //then
    }

    @Test
    void getReviews() {
        //given

        //when
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    var savedReviewList = movieReviewEntityExchangeResult.getResponseBody();
                    assert savedReviewList != null;
                    assert savedReviewList.size() > 1;
                    savedReviewList.forEach((review1 -> {
                        assert review1.getReviewId() != null;
                    }));
                });

        //then
    }

    @Test
    void getReviewsWithQueryParam() {
        //given
        URI uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 1L)
                .buildAndExpand().toUri();

        //when
        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    var savedReviewList = movieReviewEntityExchangeResult.getResponseBody();
                    assert savedReviewList != null;
                    assert savedReviewList.size() == 2;
                    System.out.println(savedReviewList);
                    savedReviewList.forEach((review1 -> {
                        assert review1.getReviewId() != null;
                    }));
                });

        //then
    }

    @Test
    void updateReview() {
        //given
        String id = "123";
        var review = new Review(id, 1L, "Awesome Movie", 7.0);
        reviewReactiveRepository.save(review).block();

        review.setRating(9.5);

        //when
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}",id)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    Review updatedReview = movieReviewEntityExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    assert updatedReview.getRating() == 9.5;
                });

        //then
    }

    @Test
    void updateReview_404NotFound() {
        //given
        var review = new Review("123", 1L, "Awesome Movie", 7.0);
        reviewReactiveRepository.save(review).block();

        //when
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}","1234")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .consumeWith(exchangeResult ->  {
                    String errorMessage = exchangeResult.getResponseBody();
                    assert errorMessage != null;
                    assert errorMessage.contains("Review not found for the given ReviewId");
                });

        //then
    }

    @Test
    void deleteReview() {
        //given
        Review review = new Review(null, 1L, "Awesome Movie", 7.0);
        Review savedReview = reviewReactiveRepository.save(review).block();

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    var savedReviewList = movieReviewEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(savedReviewList).size() == 4;
                }); // check there is 4

        //when
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/" + savedReview.getReviewId())
                .exchange()
                .expectStatus()
                .isNoContent();

        //then

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    var savedReviewList = movieReviewEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(savedReviewList).size() == 3;
                }); // check there is 3 now
    }
}
