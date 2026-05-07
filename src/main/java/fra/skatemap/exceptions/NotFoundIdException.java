package fra.skatemap.exceptions;

public class NotFoundIdException extends RuntimeException {
    public NotFoundIdException(String entityName, Object id) {
        super("Il record con id " + id + " non è stato trovato!");
    }
}
