package fra.skatemap.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class BadRequestException extends RuntimeException {

    private List<String> errors;


    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message,List<String> errors) {
        super(message);
        this.errors=errors;
    }

}
