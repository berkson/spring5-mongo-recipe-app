package guru.springframework.controllers.reactive;

import guru.springframework.config.WebReactiveConfig;
import guru.springframework.domain.Recipe;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import guru.springframework.services.reactive.RecipeReactiveServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

import static reactor.core.publisher.Mono.when;

/**
 * Created by Berkson Ximenes
 * Date: 15/11/2021
 * Time: 20:43
 */

// TODO: Verificar porque não funciona
public class RouterReactiveFunctionTest {

    WebTestClient webTestClient;

    @Mock
    RecipeReactiveServiceImpl recipeService;


    @BeforeEach
    void setUp() {
        // Inicializando o arquivo de configurações
        MockitoAnnotations.openMocks(this);
        WebReactiveConfig webReactiveConfig = new WebReactiveConfig();
        // simula o carregamento da rota e injeção ddo serviço
        RouterFunction<?> routerFunction = webReactiveConfig.routes(recipeService);
        // cliente de test reativo
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void testGetRecipes() throws Exception {
        List<Recipe> recipes = Arrays.asList(new Recipe(), new Recipe());
        when(recipeService.getRecipes()).thenMany(Flux.fromIterable(recipes));
        webTestClient.get().uri("/api/recipes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Recipe.class);
    }
}
