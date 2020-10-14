package com.learnreactivespring.router;

import com.learnreactivespring.handler.ItemsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.learnreactivespring.constants.ItemConstants.ITEM_FUNC_END_POINT_V1;
import static com.learnreactivespring.constants.ItemConstants.ITEM_FUNC_STREAM_END_POINT_V1;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ItemsRouter {

    @Bean
    public RouterFunction<ServerResponse> itemsRoute(ItemsHandler itemsHandler) {
        return RouterFunctions.route(GET(ITEM_FUNC_END_POINT_V1).and(accept(MediaType.APPLICATION_JSON)),
                        itemsHandler::getAllItems)
                .andRoute(GET(ITEM_FUNC_END_POINT_V1+"/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        itemsHandler::getOneItem)
                .andRoute(POST(ITEM_FUNC_END_POINT_V1).and(accept(MediaType.APPLICATION_JSON)),
                        itemsHandler::createItem)
                .andRoute(DELETE(ITEM_FUNC_END_POINT_V1+"/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        itemsHandler::deleteItem)
                .andRoute(PUT(ITEM_FUNC_END_POINT_V1+"/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        itemsHandler::updateItem);
    }

    @Bean
    public RouterFunction<ServerResponse> errorRoute(ItemsHandler itemsHandler) {
        return RouterFunctions.route(GET("/fun/runtimeException").and(accept(MediaType.APPLICATION_JSON)),
                itemsHandler::itemsException);
    }

    @Bean
    public RouterFunction<ServerResponse> itemStreamRouter(ItemsHandler itemsHandler) {
        return RouterFunctions.route(GET(ITEM_FUNC_STREAM_END_POINT_V1).and(accept(MediaType.APPLICATION_JSON)),
                itemsHandler::itemStream);
    }
}
