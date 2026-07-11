package org.example.backend.specification;

import org.example.backend.model.Vehicle;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class VehicleSpecification {

    public static Specification<Vehicle> hasMake(String make) {
        return (root, query, cb) -> {
            if (make == null || make.isBlank()) return null;
            return cb.like(cb.lower(root.get("make")), "%" + make.toLowerCase() + "%");
        };
    }

    public static Specification<Vehicle> hasModel(String model) {
        return (root, query, cb) -> {
            if (model == null || model.isBlank()) return null;
            return cb.like(cb.lower(root.get("model")), "%" + model.toLowerCase() + "%");
        };
    }

    public static Specification<Vehicle> hasCategory(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isBlank()) return null;
            return cb.equal(cb.lower(root.get("category")), category.toLowerCase());
        };
    }

    public static Specification<Vehicle> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) return null;
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Vehicle> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) return null;
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
}
