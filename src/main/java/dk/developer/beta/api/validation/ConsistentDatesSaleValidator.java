package dk.developer.beta.api.validation;

import dk.developer.beta.api.Sale;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConsistentDatesSaleValidator implements ConstraintValidator<ConsistentDates, Sale> {
    @Override
    public void initialize(ConsistentDates constraintAnnotation) {
    }

    @Override
    public boolean isValid(Sale value, ConstraintValidatorContext context) {
        return Validator.get().hasConsistentDates(value).result();
    }
}
