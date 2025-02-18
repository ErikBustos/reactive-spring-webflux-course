# reactive-spring-webflux
This is the code generated from the course: "Build Reactive MicroServices using Spring WebFlux/SpringBoot"

On this project , I learned about Reactive Webflux. 

There is 2 programming models for building RESTFUL APIs :
- Annotated Controllers
  - The popular way of building APIs on Spring.
- Functional Web
  - Uses the functional programming aspects like Lambdas, Method References, Functional Interfaces
  - It has 2 elements: the Router (where Rest endpoints are configured) & the Handler (Code to handle the request)
    - movies-review-service

On this Repo, there is the 2 ways. As well there is Junit Test cases and Integration Test cases to easily test the project and quickly detect issues when developing more features to the APIs.

To apply Bean Validations:
- Annotated Controllers: 
  - [On the Entity](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-info-service/src/main/java/com/reactivespring/domain/MovieInfo.java#L23) with annotations like @NotBlank, @NotNull
  - [On the Controller function](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-info-service/src/main/java/com/reactivespring/controller/MoviesInfoController.java#L49), use @RequestBody and @Valid annotations.
  - [Use a Global Error handler](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-info-service/src/main/java/com/reactivespring/exceptionhandler/GlobalErrorHandler.java#L17) with the @ControllerAdvice annotation to handle the exceptions that are thrown as part of the validation 
- On Functional Web:
  - [On the Entity](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-review-service/src/main/java/com/reactivespring/domain/Review.java#L20) have validation annotations like @NotBlank, @NotNull
  - On the Handler, have a [javax validator property](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-review-service/src/main/java/com/reactivespring/handler/ReviewHandler.java#L24)
    - Use it with the [onNext on your function](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-review-service/src/main/java/com/reactivespring/handler/ReviewHandler.java#L34)
    - Have a [validate function](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-review-service/src/main/java/com/reactivespring/handler/ReviewHandler.java#L77) , extract the constraintViolations, get all the error messages and throw a custom exception.
  - Create a [GlobalErrorHandler](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-review-service/src/main/java/com/reactivespring/exceptionhandler/GlobalErrorHandler.java#L14) that implements the ErrorWebExceptionHandler.
    - [Check for a custom Exception](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/movies-review-service/src/main/java/com/reactivespring/exceptionhandler/GlobalErrorHandler.java#L22) and handle it.
![Application Diagram](https://github.com/ErikBustos/reactive-spring-webflux-course/blob/main/ApplicationDiagram.png?raw=true)
