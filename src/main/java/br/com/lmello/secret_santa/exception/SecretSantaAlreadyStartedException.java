package br.com.lmello.secret_santa.exception;

public class SecretSantaAlreadyStartedException extends RuntimeException {
    public SecretSantaAlreadyStartedException(String code) {
        super("Draw '" + code + "' already started");
    }
}
