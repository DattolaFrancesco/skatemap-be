package fra.skatemap.exceptions;

import java.util.List;

public class ValidationException extends RuntimeException {
    private List<String> errors;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(List<String> errors) {
        super("Errori di validazione");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}