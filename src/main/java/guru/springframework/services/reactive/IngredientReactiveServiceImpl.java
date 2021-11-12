package guru.springframework.services.reactive;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.domain.UnitOfMeasure;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import guru.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Created by jt on 6/28/17.
 */
@Slf4j
@Service
@Profile("reactive")
public class IngredientReactiveServiceImpl implements IngredientReactiveService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeReactiveRepository recipeRepository;
    private final UnitOfMeasureReactiveRepository unitOfMeasureRepository;

    public IngredientReactiveServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                         IngredientCommandToIngredient ingredientCommandToIngredient,
                                         RecipeReactiveRepository recipeRepository,
                                         UnitOfMeasureReactiveRepository unitOfMeasureRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeRepository = recipeRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Override
    public Mono<IngredientCommand> findByRecipeIdAndIngredientId(String recipeId, String ingredientId) {

        return recipeRepository.findById(recipeId)
                .flatMapIterable(Recipe::getIngredients)
                .filter(ingredient -> ingredient.getId().equalsIgnoreCase(ingredientId))
                .single()
                .map(ingredient -> {
                    IngredientCommand command = ingredientToIngredientCommand.convert(ingredient);
                    command.setRecipeId(recipeId);
                    return command;
                });
    }

    @Override
    public Mono<IngredientCommand> saveIngredientCommand(IngredientCommand command) {

        Mono<Recipe> monoRecipe = recipeRepository.findById(command.getRecipeId());

        return monoRecipe.flatMap(recipe -> {

            Optional<Ingredient> ingredientOptional = recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(command.getId()))
                    .findFirst();

            if (ingredientOptional.isPresent()) {
                Ingredient ingredientFound = ingredientOptional.get();
                ingredientFound.setDescription(command.getDescription());
                ingredientFound.setAmount(command.getAmount());
                unitOfMeasureRepository
                        .findById(command.getUom().getId())
                        .doOnNext(unitOfMeasure -> ingredientFound.setUom(unitOfMeasure))
                        .doOnError(throwable -> new RuntimeException("UOM NOT FOUND"));
            } else {
                //add new Ingredient
                unitOfMeasureRepository
                        .findById(command.getUom().getId())
                        .doOnNext(unitOfMeasure -> command.getUom().setDescription(unitOfMeasure.getDescription()))
                        .doOnError(throwable -> new RuntimeException("Erro ao adicionar novo ingrediente" + throwable.getMessage()));
                Ingredient ingredient = ingredientCommandToIngredient.convert(command);
                ingredient.setRecipe(null); // avoids stackoverflow infinite loop
                recipe.addIngredient(ingredient);
            }

            return recipeRepository.save(recipe)
                    .flatMap(savedRecipe -> {
                        Optional<Ingredient> savedIngredientOptional = savedRecipe.getIngredients()
                                .stream()
                                .filter(recipeIngredients -> recipeIngredients.getId().equals(command.getId()))
                                .findFirst();
                        if (!savedIngredientOptional.isPresent()) {
                            //not totally safe... But best guess
                            savedIngredientOptional = savedRecipe.getIngredients().stream()
                                    .filter(recipeIngredients -> recipeIngredients.getDescription().equals(command.getDescription()))
                                    .filter(recipeIngredients -> recipeIngredients.getAmount().equals(command.getAmount()))
                                    .filter(recipeIngredients -> recipeIngredients.getUom().getId().equals(command.getUom().getId()))
                                    .findFirst();
                        }

                        //incrementado com o valor do id
                        IngredientCommand ingredientCommandSaved = ingredientToIngredientCommand.convert(savedIngredientOptional.get());
                        ingredientCommandSaved.setRecipeId(recipe.getId());
                        return Mono.just(ingredientCommandSaved);
                    });
        });

    }

    @Override
    public Mono<Void> deleteById(String recipeId, String idToDelete) {

        log.debug("Deleting ingredient: " + recipeId + ":" + idToDelete);

        recipeRepository.findById(recipeId)
                .doOnNext(recipe -> {
                    log.debug("found recipe");

                    Optional<Ingredient> ingredientOptional = recipe
                            .getIngredients()
                            .stream()
                            .filter(ingredient -> ingredient.getId().equals(idToDelete))
                            .findFirst();

                    if (ingredientOptional.isPresent()) {
                        log.debug("found Ingredient");
                        Ingredient ingredientToDelete = ingredientOptional.get();
                        //ingredientToDelete.setRecipe(null);
                        recipe.getIngredients().remove(ingredientOptional.get());
                        recipeRepository.save(recipe);
                    }
                })
                .doOnError(throwable -> log.debug("Recipe Id Not found. Id:" + recipeId));

        return Mono.empty();
    }
}
