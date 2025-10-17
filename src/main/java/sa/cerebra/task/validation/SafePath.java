package sa.cerebra.task.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SafePathValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER }) // Can be used on fields or method parameters
@Retention(RetentionPolicy.RUNTIME)
public @interface SafePath {

    String message() default "Invalid path: Path traversal characters (../) are not allowed.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}