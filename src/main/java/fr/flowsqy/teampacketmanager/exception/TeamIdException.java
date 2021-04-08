package fr.flowsqy.teampacketmanager.exception;

public class TeamIdException extends RuntimeException {

    public TeamIdException() {
    }

    public TeamIdException(String message) {
        super(message);
    }

    public TeamIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeamIdException(Throwable cause) {
        super(cause);
    }


}
