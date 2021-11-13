package guru.springframework.controllers.reactive;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.exceptions.NotFoundException;
import guru.springframework.services.RecipeService;
import guru.springframework.services.reactive.RecipeReactiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.exceptions.TemplateInputException;
import reactor.core.publisher.Mono;
//import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * Created by jt on 6/19/17.
 */
@Slf4j
@Controller
@Profile("reactive")
public class RecipeReactiveController {

    private static final String RECIPE_RECIPEFORM_URL = "recipe/recipeform";
    private final RecipeReactiveService recipeService;
    private WebDataBinder webDataBinder;

    public RecipeReactiveController(RecipeReactiveService recipeService) {
        this.recipeService = recipeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        this.webDataBinder = webDataBinder;
    }

    @GetMapping("/recipe/{id}/show")
    public String showById(@PathVariable String id, Model model) {

        model.addAttribute("recipe", recipeService.findById(id));

        return "recipe/show";
    }

    @GetMapping("recipe/new")
    public String newRecipe(Model model) {
        model.addAttribute("recipe", new RecipeCommand());

        return "recipe/recipeform";
    }

    @GetMapping("recipe/{id}/update")
    public String updateRecipe(@PathVariable String id, Model model) {
        Mono<RecipeCommand> recipeCommand = recipeService.findCommandById(id);
        model.addAttribute("recipe", recipeCommand);
        return RECIPE_RECIPEFORM_URL;
    }

    @PostMapping("recipe")
    public String saveOrUpdate(@ModelAttribute("recipe") RecipeCommand command) {
        webDataBinder.validate();
        BindingResult bindingResult = webDataBinder.getBindingResult();

        if (bindingResult.hasErrors()) {

            bindingResult.getAllErrors().forEach(objectError -> {
                log.debug(objectError.toString());
            });

            return RECIPE_RECIPEFORM_URL;
        }

        Mono<RecipeCommand> savedCommand = recipeService.saveRecipeCommand(command);

        return "redirect:/recipe/" + savedCommand.map(RecipeCommand::getId).subscribe() + "/show";
    }

    @GetMapping("recipe/{id}/delete")
    public String deleteById(@PathVariable String id) {

        log.debug("Deleting id: " + id);

        recipeService.deleteById(id).block();
        return "redirect:/";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class, TemplateInputException.class})
    public String handleNotFound(Exception exception, Model model){

        log.error("Handling not found exception");
        log.error(exception.getMessage());

        model.addAttribute("exception", exception);

        return "404error";
    }

}
