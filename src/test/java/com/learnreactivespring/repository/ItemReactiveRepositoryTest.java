package com.learnreactivespring.repository;

import com.learnreactivespring.document.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
@DirtiesContext
@Profile("test")
public class ItemReactiveRepositoryTest {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;
    List<Item> items = Arrays.asList(
            new Item(null, "Samsung TV", 400.0),
            new Item(null, "LG TV", 300.0),
            new Item(null, "Apple Watch", 250.0),
            new Item(null, "MacBook Pro 16", 3000.0),
            new Item("ABC", "Bose Headphones", 320.0)
            );

    @Before
    public void setup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(items))
                .flatMap(i -> itemReactiveRepository.save(i))
                .doOnNext(System.out::println)
                .blockLast();
    }

    @Test
    public void getAllItems() {
        Flux<Item> itemFlux = itemReactiveRepository.findAll();
        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void getItemByID() {

        StepVerifier.create(itemReactiveRepository.findById("ABC"))
                .expectSubscription()
                .expectNextMatches((item -> item.getDescription().equals("Bose Headphones")))
                .verifyComplete();
    }

    @Test
    public void findItemByDescription() {

        StepVerifier.create(itemReactiveRepository.findByDescription("Bose Headphones").log("findByDescription: "))
                .expectSubscription()
                .expectNextMatches((item -> item.getId().equals("ABC")))
                .verifyComplete();
    }

    @Test
    public void saveItem() {
        Item item = new Item(null, "Iphone Xs", 1000.0);
        Mono<Item> savedItem = itemReactiveRepository.save(item);
        StepVerifier.create(savedItem)
                .expectSubscription()
                .expectNextMatches(i -> i.getId() != null && i.getDescription().equalsIgnoreCase("Iphone Xs"))
                .verifyComplete();
    }

    @Test
    public void udpateItem() {
        double newPrice = 520.0;
        Mono<Item> updatedItem = itemReactiveRepository.findByDescription("LG TV")
                .map(item -> {
                    item.setPrice(newPrice);
                    return item;
                })
                .flatMap(i -> itemReactiveRepository.save(i));
        StepVerifier.create(updatedItem)
                .expectSubscription()
                .expectNextMatches(i -> i.getPrice() == 520.0)
                .verifyComplete();
    }

    @Test
    public void deleteItem() {
        Mono<Void> deletedItem = itemReactiveRepository.findById("ABC") //Mono item
                .map(Item::getId) //getId - Transform item to ID
                .flatMap((i) -> itemReactiveRepository.deleteById(i));
        StepVerifier.create(deletedItem)
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll())
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }
    @Test
    public void deleteItemByDescription() {
        Mono<Void> deletedItem = itemReactiveRepository.findByDescription("Bose Headphones") //Mono item
                .flatMap((i) -> itemReactiveRepository.delete(i));
        StepVerifier.create(deletedItem)
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll())
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }
}
