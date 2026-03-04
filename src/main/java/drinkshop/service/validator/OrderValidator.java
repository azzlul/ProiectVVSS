package drinkshop.service.validator;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;

public class OrderValidator implements Validator<Order> {

    private final OrderItemValidator itemValidator = new OrderItemValidator();

    @Override
    public void validate(Order order) {

        StringBuilder errors = new StringBuilder();

        if (order.getId() <= 0)
            errors.append("ID comanda invalid!\n");

        if (order.getItems() == null || order.getItems().isEmpty()) {
            errors.append("Comanda fara produse!\n");
        } else {
            for (OrderItem item : order.getItems()) {
                try {
                    itemValidator.validate(item);
                } catch (ValidationException e) {
                    errors.append(e.getMessage());
                }
            }
        }

        if (order.getTotalPrice() < 0)
            errors.append("Total invalid!\n");

        if (!errors.isEmpty())
            throw new ValidationException(errors.toString());
    }
}
