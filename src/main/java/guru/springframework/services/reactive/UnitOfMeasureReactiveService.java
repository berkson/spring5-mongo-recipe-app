package guru.springframework.services.reactive;

import guru.springframework.commands.UnitOfMeasureCommand;
import guru.springframework.domain.UnitOfMeasure;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Created by jt on 6/28/17.
 */
public interface UnitOfMeasureReactiveService {

    Flux<UnitOfMeasureCommand> listAllUoms();

    Mono<UnitOfMeasure> findById(String id);
}
