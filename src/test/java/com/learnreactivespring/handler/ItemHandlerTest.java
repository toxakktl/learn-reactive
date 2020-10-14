package com.learnreactivespring.handler;

import com.learnreactivespring.constants.ItemConstants;
import com.learnreactivespring.document.Item;
import com.learnreactivespring.repository.ItemReactiveRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static com.learnreactivespring.constants.ItemConstants.ITEM_END_POINT_V1;
import static com.learnreactivespring.constants.ItemConstants.ITEM_FUNC_END_POINT_V1;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ItemHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    public List<Item> data() {
        List<Item> items = Arrays.asList(
                new Item(null, "Samsung TV", 400.0),
                new Item(null, "LG TV", 300.0),
                new Item(null, "Apple Watch", 250.0),
                new Item(null, "MacBook Pro 16", 3000.0),
                new Item("ABC", "Bose Headphones", 320.0)
        );
        return items;
    }

    @Before
    public void setup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(item->itemReactiveRepository.save(item))
                .doOnNext(item -> System.out.println("Inserted item " + item))
                .blockLast();
    }

    @Test
    public void getAllItems() {
        webTestClient.get().uri(ITEM_FUNC_END_POINT_V1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(5);
    }

        @Test
        public void getAllItems_approach2() {
            webTestClient.get().uri(ITEM_FUNC_END_POINT_V1)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBodyList(Item.class)
                    .hasSize(5)
                    .consumeWith(response -> {
                        List<Item> items = response.getResponseBody();
                        items.forEach(i->{
                            Assert.assertTrue(i.getId() != null);
                        });
                    });
        }

    @Test
    public void getAllItems_approach3() {
        Flux<Item> itemFlux = webTestClient.get().uri(ITEM_FUNC_END_POINT_V1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(Item.class)
                .getResponseBody();

        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextCount(5)
                .verifyComplete();
    }



    @Test
    public void getOneItem() {
        webTestClient.get().uri(ITEM_FUNC_END_POINT_V1+"/{id}", "ABC")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", 320.0);
    }

    @Test
    public void getOneItemNotFound() {
        webTestClient.get().uri(ITEM_FUNC_END_POINT_V1+"/{id}", "DEF")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void createItem() {
        Item newItem = new Item(null, "Iphone Xs Max", 1000.0);
        webTestClient.post().uri(ITEM_FUNC_END_POINT_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newItem), Item.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("Iphone Xs Max");

    }

    @Test
    public void deleteItem() {
        webTestClient.delete().uri(ITEM_FUNC_END_POINT_V1+"/{id}", "ABC")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }

    @Test
    public void updateItem() {
        double newPrice = 1200.0;
        Item newItem = new Item(null, "Bose Headphones", newPrice);
        webTestClient.put().uri(ITEM_FUNC_END_POINT_V1+"/{id}", "ABC")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(newItem), Item.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", newPrice);

    }

    @Test
    public void runtimeException() {
        webTestClient.get().uri("/fun/runtimeException")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message", "Runtime Exception occured");
    }

}
