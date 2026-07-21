package com.ogm.hrms.storage;

/** Thrown when the storage backend fails to persist or read content (maps to a 500 by default). */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
