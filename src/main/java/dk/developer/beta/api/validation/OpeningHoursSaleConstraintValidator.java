package dk.developer.beta.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OpeningHoursSaleConstraintValidator implements ConstraintValidator<OpeningHours, String> {
    @Override
    public void initialize(OpeningHours constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Validator.get().areOpeningHours(value).result();
    }
}
