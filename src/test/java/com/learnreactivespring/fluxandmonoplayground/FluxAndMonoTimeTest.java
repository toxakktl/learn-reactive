package com.learnreactivespring.fluxandmonoplayground;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class FluxAndMonoTimeTest {

    @Test
    public void infiniteSequence() throws InterruptedException {
        Flux<Long> infiniteFlux = Flux.interval(Duration.ofMillis(200)).log(); //starts with 0 -> ...
        infiniteFlux.subscribe((element) -> System.out.println("Value is " + element));

        Thread.sleep(3000);
    }
    @Test
    public void infiniteSequenceTest() throws InterruptedException {
        Flux<Long> finiteFlux = Flux.interval(Duration.ofMillis(100))
                .take(3)
                .log(); //starts with 0 -> ...

        StepVerifier.create(finiteFlux)
                .expectNext(0L, 1L, 2L)
                .verifyComplete();
    }
    @Test
    public void infiniteSequenceMap() throws InterruptedException {
        Flux<Integer> finiteFlux = Flux.interval(Duration.ofMillis(100))
                .map(l-> l.intValue())
                .take(3)
                .log(); //starts with 0 -> ...

        StepVerifier.create(finiteFlux)
                .expectNext(0, 1, 2)
                .verifyComplete();
    }
}
