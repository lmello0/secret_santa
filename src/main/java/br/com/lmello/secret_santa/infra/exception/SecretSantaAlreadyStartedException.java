package br.com.lmello.secret_santa.infra.exception;

public class SecretSantaAlreadyStartedException extends RuntimeException {
    public SecretSantaAlreadyStartedException(String code) {
        super("Draw '" + code + "' already started");
    }
}
