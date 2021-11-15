package guru.springframework.services.reactive;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 7/3/17.
 */
public interface ImageReactiveService {

    void saveImageFile(String recipeId, MultipartFile file);
    void saveImageFile(String recipeId, Mono<FilePart> file);
}
