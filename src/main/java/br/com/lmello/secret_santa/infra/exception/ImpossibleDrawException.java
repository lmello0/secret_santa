package br.com.lmello.secret_santa.infra.exception;

public class ImpossibleDrawException extends RuntimeException {
    public ImpossibleDrawException(String code, int size) {
        super("Secret santa '" + code + "' is impossible due to its size: " + size);
    }
}
