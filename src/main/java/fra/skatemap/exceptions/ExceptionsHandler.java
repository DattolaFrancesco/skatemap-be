package fra.skatemap.exceptions;

import fra.skatemap.payloads.ErrorsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionsHandler {

    // (404)
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorsResponse handleNotFound(NotFoundException e) {
        return new ErrorsResponse(e.getMessage(), LocalDateTime.now());
    }

    //  (400)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorsResponse handleValidationErrors(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ErrorsResponse("Errori di validazione: " + message, LocalDateTime.now());
    }

    // (500)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorsResponse handleGeneric(Exception e) {
        e.printStackTrace();
        return new ErrorsResponse("Errore interno del server. Riprova più tardi.", LocalDateTime.now());
    }
}