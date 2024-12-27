package com.webest.store.product.application;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.webest.store.product.domain.model.Cart;
import com.webest.store.product.domain.model.Product;
import com.webest.store.product.domain.model.dto.ProductDto;
import com.webest.store.product.domain.repository.CartRepository;
import com.webest.store.product.domain.repository.ProductRepository;
import com.webest.store.product.exception.ProductErrorCode;
import com.webest.store.product.exception.ProductException;
import com.webest.store.product.presentation.dto.CartResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    private final MongoTemplate mongoTemplate;

    // 여기서 캐시를 갱신해주고
    @CachePut(cacheNames = "userCart", key = "args[1]")
    public CartResponseDto createCart(Long productId, String userId) {

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductException(
                ProductErrorCode.PRODUCT_NOT_FOUND)
            );

        Query query = new Query();
        query.addCriteria(where("userId").is(userId));

        Update update = new Update();
        update.set("storeId", product.getStore().getId());
        update.set("productDto", ProductDto.createProduct(product));

        mongoTemplate.upsert(query, update, Cart.class);

        Cart cart = mongoTemplate.query(Cart.class)
            .matching(query(where("userId").is(userId))).firstValue();

        return CartResponseDto.from(cart);
    }

    // 여기서 캐시를 갖고 온다.
    @Cacheable(cacheNames = "userCart", key = "args[0]", unless = "#result == null")
    public CartResponseDto findCartById(String userId) {

//        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        Query query = new Query();
        query.addCriteria(where("userId").is(userId));

        Cart first = mongoTemplate.query(Cart.class)
            .matching(query(where("userId").is(userId))).firstValue();

        return CartResponseDto.from(first);
    }

    public List<Cart> findAllCart() {

        return cartRepository.findAll();
    }
}
