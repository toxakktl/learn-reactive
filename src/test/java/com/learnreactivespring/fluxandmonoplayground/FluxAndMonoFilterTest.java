package com.learnreactivespring.fluxandmonoplayground;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxAndMonoFilterTest {

    List<String> names = Arrays.asList("adam", "anna", "john", "jenny");

    @Test
    public void filterTest(){
        Flux<String> stringFlux = Flux.fromIterable(names)
                .filter(f -> f.startsWith("a"))
                .log();
        StepVerifier.create(stringFlux)
                .expectNext("adam", "anna")
                .verifyComplete();
    }
}
