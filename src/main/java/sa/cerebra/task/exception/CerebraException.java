package sa.cerebra.task.exception;

import java.util.function.Supplier;

public class CerebraException extends RuntimeException {
    public CerebraException(String message) {
        super(message);
    }
    public static CerebraException withMessage(String message) {
        return new CerebraException(message);
    }

    public static Supplier<CerebraException> supply(String message) {
        return () -> new CerebraException(message);
    }

}
