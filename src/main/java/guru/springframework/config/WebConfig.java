package guru.springframework.config;

import guru.springframework.domain.Recipe;
import guru.springframework.services.RecipeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.GET;

/**
 * Created by Berkson Ximenes
 * Date: 15/11/2021
 * Time: 20:00
 */
@Configuration
public class WebConfig {

    @Bean
    RouterFunction<?> routes(RecipeService recipeService){
        return RouterFunctions.route(GET("api/recipes"),
                serverRequest -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(recipeService.getRecipes()));
    }
}
