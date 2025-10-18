package sa.cerebra.task.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class SafePathValidatorTest {

    private SafePathValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new SafePathValidator();
        validator.initialize(null); // SafePath annotation doesn't have parameters
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathIsNull() {
        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathIsEmpty() {
        // When
        boolean result = validator.isValid("", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathIsWhitespace() {
        // When
        boolean result = validator.isValid("   ", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathIsValid() {
        // When
        boolean result = validator.isValid("documents/file.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathIsNestedDirectory() {
        // When
        boolean result = validator.isValid("documents/subfolder/file.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidCharacters() {
        // When
        boolean result = validator.isValid("documents/my-file_123.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathStartsWithSlash() {
        // When
        boolean result = validator.isValid("/documents/file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathStartsWithBackslash() {
        // When
        boolean result = validator.isValid("\\documents\\file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsColon() {
        // When
        boolean result = validator.isValid("C:documents/file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsDoubleDots() {
        // When
        boolean result = validator.isValid("../documents/file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsDoubleDotsInMiddle() {
        // When
        boolean result = validator.isValid("documents/../file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsEncodedDoubleDots() {
        // When
        boolean result = validator.isValid("%2e%2e/documents/file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsCaseInsensitiveEncodedDoubleDots() {
        // When
        boolean result = validator.isValid("%2E%2E/documents/file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsDotBackslash() {
        // When
        boolean result = validator.isValid(".\\documents\\file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsEncodedDotBackslash() {
        // When
        boolean result = validator.isValid("%2e%5c\\documents\\file.txt", context);

        // Then
        assertFalse(result);
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "../",
            "..\\",
            "%2e%2e/",
            "%2E%2E/",
            "%2e%2e\\",
            "%2E%2E\\",
            ".\\",
            "%2e%5c",
            "%2E%5C",
            "/.",
            "\\.",
            "C:",
            "C:\\",
            "/etc/passwd",
            "\\windows\\system32",
            "..\\..\\windows\\system32",
            "../etc/passwd",
            "....//",
            "....\\\\",
            "%2e%2e%2e%2e//",
            "%2E%2E%2E%2E//"
    })
    void isValid_ShouldReturnFalse_WhenPathContainsTraversalPatterns(String maliciousPath) {
        // When
        boolean result = validator.isValid(maliciousPath, context);

        // Then
        assertFalse(result, "Path should be rejected: " + maliciousPath);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "documents/file.txt",
            "folder/subfolder/file.txt",
            "my-file_123.txt",
            "file with spaces.txt",
            "file-name.txt",
            "file_name.txt",
            "file123.txt",
            "folder/file.txt",
            "a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z.txt",
            "folder1/folder2/folder3/file.txt",
            "documents/2023/file.txt",
            "documents/january/file.txt",
            "documents/file-2023-01-01.txt",
            "documents/file_2023_01_01.txt"
    })
    void isValid_ShouldReturnTrue_WhenPathIsSafe(String safePath) {
        // When
        boolean result = validator.isValid(safePath, context);

        // Then
        assertTrue(result, "Path should be accepted: " + safePath);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathNormalizesToRoot() {
        // When
        boolean result = validator.isValid("../../../../", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathNormalizesToContainDoubleDots() {
        // When
        boolean result = validator.isValid("documents/../../etc/passwd", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsInvalidCharacters() {
        // When
        boolean result = validator.isValid("documents/file\0.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsControlCharacters() {
        // When
        boolean result = validator.isValid("documents/file\t.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsNewlineCharacters() {
        // When
        boolean result = validator.isValid("documents/file\n.txt", context);

        // Then
        assertFalse(result);
    }



    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidSpecialCharacters() {
        // When
        boolean result = validator.isValid("documents/file-name_123.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsUnicodeCharacters() {
        // When
        boolean result = validator.isValid("documents/файл.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsNumbers() {
        // When
        boolean result = validator.isValid("documents/123/file.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsMixedCase() {
        // When
        boolean result = validator.isValid("Documents/MyFile.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathIsJustDoubleDots() {
        // When
        boolean result = validator.isValid("..", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathIsJustDot() {
        // When
        boolean result = validator.isValid(".", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsMultipleDoubleDots() {
        // When
        boolean result = validator.isValid("....", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsMixedSeparators() {
        // When
        boolean result = validator.isValid("documents\\../file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsEncodedTraversalWithMixedCase() {
        // When
        boolean result = validator.isValid("%2e%2E/documents/file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsTraversalAtEnd() {
        // When
        boolean result = validator.isValid("documents/file.txt/..", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsTraversalAtStart() {
        // When
        boolean result = validator.isValid("../documents/file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsTraversalInMiddle() {
        // When
        boolean result = validator.isValid("documents/../file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathIsSingleFile() {
        // When
        boolean result = validator.isValid("file.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathIsSingleDirectory() {
        // When
        boolean result = validator.isValid("documents", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsMultipleValidDirectories() {
        // When
        boolean result = validator.isValid("documents/2023/january/reports/file.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsWindowsDriveLetter() {
        // When
        boolean result = validator.isValid("D:documents/file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsUNC() {
        // When
        boolean result = validator.isValid("\\\\server\\share\\file.txt", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsURLEncodedTraversal() {
        // When
        boolean result = validator.isValid("%2e%2e%2f%2e%2e%2fetc%2fpasswd", context);

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsDoubleEncodedTraversal() {
        // When
        boolean result = validator.isValid("%252e%252e%252fetc%252fpasswd", context);

        // Then
        assertFalse(result);
    }


    @Test
    void isValid_ShouldReturnFalse_WhenPathContainsInvalidPathCharacters() {
        // When
        boolean result = validator.isValid("documents/file<>.txt", context);

        // Then
        assertFalse(result);
    }


    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidParentheses() {
        // When
        boolean result = validator.isValid("documents/file(1).txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidBrackets() {
        // When
        boolean result = validator.isValid("documents/file[1].txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidBraces() {
        // When
        boolean result = validator.isValid("documents/file{1}.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidQuotes() {
        // When
        boolean result = validator.isValid("documents/file'name'.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidDoubleQuotes() {
        // When
        boolean result = validator.isValid("documents/file\"name\".txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidTilde() {
        // When
        boolean result = validator.isValid("documents/file~name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidAtSymbol() {
        // When
        boolean result = validator.isValid("documents/file@name.txt", context);

        // Then
        assertTrue(result);
    }




    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidExclamation() {
        // When
        boolean result = validator.isValid("documents/file!name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidAmpersand() {
        // When
        boolean result = validator.isValid("documents/file&name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidPlus() {
        // When
        boolean result = validator.isValid("documents/file+name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidEquals() {
        // When
        boolean result = validator.isValid("documents/file=name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidSemicolon() {
        // When
        boolean result = validator.isValid("documents/file;name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidComma() {
        // When
        boolean result = validator.isValid("documents/file,name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidPeriod() {
        // When
        boolean result = validator.isValid("documents/file.name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidUnderscore() {
        // When
        boolean result = validator.isValid("documents/file_name.txt", context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPathContainsValidHyphen() {
        // When
        boolean result = validator.isValid("documents/file-name.txt", context);

        // Then
        assertTrue(result);
    }
}
