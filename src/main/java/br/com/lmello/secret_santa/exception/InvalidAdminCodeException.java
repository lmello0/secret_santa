package br.com.lmello.secret_santa.exception;

public class InvalidAdminCodeException extends RuntimeException {
    public InvalidAdminCodeException() {
        super("The given admin code is incorrect");
    }
}
