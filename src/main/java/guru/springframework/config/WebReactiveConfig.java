package guru.springframework.config;

import guru.springframework.domain.Recipe;
import guru.springframework.services.reactive.RecipeReactiveService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Created by Berkson Ximenes
 * Date: 15/11/2021
 * Time: 20:32
 */
@Profile("reactive")
@Configuration
public class WebReactiveConfig {

    @Bean
    public RouterFunction<?> routes(RecipeReactiveService recipeService) {
        return RouterFunctions.route(GET("api/recipes"), request -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(recipeService.getRecipes(), Recipe.class));
    }
}
