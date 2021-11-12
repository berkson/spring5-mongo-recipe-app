package guru.springframework.services.reactive;

import guru.springframework.domain.Recipe;
import guru.springframework.repositories.RecipeRepository;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by jt on 7/3/17.
 */
@Slf4j
@Service
@Profile("reactive")
public class ImageReactiveServiceImpl implements ImageReactiveService {


    private final RecipeReactiveRepository recipeRepository;

    public ImageReactiveServiceImpl(RecipeReactiveRepository recipeService) {

        this.recipeRepository = recipeService;
    }

    @Override
    public void saveImageFile(String recipeId, MultipartFile file) {

        recipeRepository.findById(recipeId).doOnNext(recipe -> {
            Byte[] byteObjects;
            try {
                byteObjects = new Byte[file.getBytes().length];
                int i = 0;

                for (byte b : file.getBytes()) {
                    byteObjects[i++] = b;
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
            recipe.setImage(byteObjects);

            recipeRepository.save(recipe);
        }).doOnError(throwable -> log.error("Error occurred: ", throwable.getMessage()));


//        try {
//            Recipe recipe = recipeRepository.findById(recipeId).block();
//
//            Byte[] byteObjects = new Byte[file.getBytes().length];
//
//            int i = 0;
//
//            for (byte b : file.getBytes()) {
//                byteObjects[i++] = b;
//            }
//
//            recipe.setImage(byteObjects);
//
//            recipeRepository.save(recipe).block();
//        } catch (IOException e) {
//            //todo handle better
//            log.error("Error occurred", e);
//
//            e.printStackTrace();
//        }
    }
}
