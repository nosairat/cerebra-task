package sa.cerebra.task.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class SafePathValidator implements ConstraintValidator<SafePath, String> {


    private static final Pattern PATH_TRAVERSAL_PATTERN =
            Pattern.compile("(\\.\\.|[<>\\^\\%\\$\\#\\t\\n])", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (path == null || path.trim().isEmpty()) {
            return true; // Use @NotBlank or @NotNull separately for empty checks
        }

        // 1. Check for absolute paths: User input should always be relative
        if (path.startsWith("/") || path.startsWith("\\") || path.contains(":")) {
            return false;
        }
        if (path.contains("\\")) {
            return false;
        }

        // 2. Check for explicit path traversal patterns
        if (PATH_TRAVERSAL_PATTERN.matcher(path).find()) {
            return false;
        }

        // 3. Robust check: Normalize the path and ensure it doesn't escape the root.
        // We use an arbitrary root (like "/") for normalization.
        try {
            Path normalizedPath = Paths.get("/").resolve(path).normalize();

            // If the normalized path is equivalent to the root path itself (i.e., the path
            // completely escaped or refers to the root), or if it contains ".." components
            // after normalization (which shouldn't happen if the input was relative), reject it.
            if (normalizedPath.toString().equals("/") || normalizedPath.toString().contains("..")
            || normalizedPath.toString().contains("<")
            || normalizedPath.toString().contains(">"))
            {
                return false;
            }

        } catch (Exception e) {
            // Path conversion failed due to invalid characters or format
            return false;
        }

        return true;
    }
}