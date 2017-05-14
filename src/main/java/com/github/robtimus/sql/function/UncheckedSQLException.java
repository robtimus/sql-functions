/*
 * UncheckedSQLException.java
 * Copyright 2017 Rob Spoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robtimus.sql.function;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Wraps an {@link SQLException} with an unchecked exception.
 *
 * @author Rob Spoor
 */
public class UncheckedSQLException extends RuntimeException {
    /** The serial version UID. */
    private static final long serialVersionUID = -5801607293098410875L;

    /**
     * Creates a new unchecked SQL exception.
     *
     * @param message The detail message.
     * @param cause The {@code SQLException} to wrap.
     * @throws NullPointerException If the cause is {@code null}.
     */
    public UncheckedSQLException(String message, SQLException cause) {
        super(message, Objects.requireNonNull(cause));
    }

    /**
     * Creates a new unchecked SQL exception.
     *
     * @param cause The {@code SQLException} to wrap.
     * @throws NullPointerException If the cause is {@code null}.
     */
    public UncheckedSQLException(SQLException cause) {
        super(Objects.requireNonNull(cause));
    }

    /**
     * Returns the cause of this exception.
     *
     * @return The {@code SQLException} which is the cause of this exception.
     */
    @Override
    public synchronized SQLException getCause() {
        return (SQLException) super.getCause();
    }

    /**
     * Called to read the object from a stream.
     *
     * @throws InvalidObjectException If the object is invalid or has a cause that is not an {@code SQLException}.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof SQLException)) {
            throw new InvalidObjectException("Cause must be an SQLException"); //$NON-NLS-1$
        }
    }
}
