package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * God-класс: валидация, расчёт цены, персистентность и "уведомление
 * клиента" смешаны в одном методе. Цель для рефакторинга по SRP в ЛР1.
 */
@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PricingCalculator pricingCalculator = new PricingCalculator();
    private final OrderValidator orderValidator = new OrderValidator();
    private final OrderNotifier orderNotifier = new OrderNotifier();

    public OrderService(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        orderValidator.validateCustomerName(request);
        orderValidator.validateCustomerAddress(request);
        orderValidator.validateItems(request);

        List<Product> products = new ArrayList<>();
        List<PricingCalculator.LineItem> lineItems = new ArrayList<>();

        for (CreateOrderRequest.Item raw : request.items()) {
            Product product = productRepository.findById(raw.productId())
                    .orElseThrow(() -> new IllegalArgumentException("product " + raw.productId() + " not found"));
            orderValidator.validateQuantity(raw);
            int quantity = raw.quantity() == null ? 1 : raw.quantity();
            products.add(product);
            lineItems.add(new PricingCalculator.LineItem(product.getPrice(), quantity));
        }

        BigDecimal total = pricingCalculator.calculateOrderTotal(
                lineItems, request.customerType() == null ? "regular" : request.customerType(), request.couponCode());

        Order order = new Order(
                request.customerFullName(), request.customerAddress(), request.customerPhone(), "new");
        order = orderRepository.save(order);

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int quantity = lineItems.get(i).quantity();
            orderItemRepository.save(new OrderItem(order.getId(), product.getName(), product.getPrice(), quantity));
        }

        orderNotifier.sendConfirmationEmail(request.customerFullName(), order.getId(), total);

        return order;
    }
}
