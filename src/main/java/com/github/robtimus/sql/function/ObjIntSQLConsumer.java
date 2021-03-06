/*
 * ObjIntSQLConsumer.java
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

import java.sql.SQLException;
import java.util.Objects;
import java.util.function.ObjIntConsumer;

/**
 * Represents an operation that accepts an object-valued and a {@code int}-valued argument, and returns no result.
 * This is the {@link SQLException} throwing equivalent of {@link ObjIntConsumer}.
 *
 * @param <T> The type of the object argument to the operation.
 */
@FunctionalInterface
public interface ObjIntSQLConsumer<T> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t The first input argument.
     * @param value The second input argument.
     * @throws SQLException If an SQL error occurs.
     */
    void accept(T t, int value) throws SQLException;

    /**
     * Returns an {@code ObjIntConsumer} that performs the {@code operation} operation, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param <T> The type of the object argument to the operation.
     * @param operation The operation to perform when the returned operation is performed.
     * @return An {@code ObjIntConsumer} that performs the {@code operation} operation on its input, and wraps any {@link SQLException} that is thrown
     *         in an {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code operation} is {@code null}.
     */
    static <T> ObjIntConsumer<T> unchecked(ObjIntSQLConsumer<? super T> operation) {
        Objects.requireNonNull(operation);
        return (t, value) -> {
            try {
                operation.accept(t, value);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns an {@code ObjIntSQLConsumer} that performs the {@code operation} operation, and unwraps any {@link UncheckedSQLException} that is
     * thrown by throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param <T> The type of the input to the operation.
     * @param operation The operation to perform when the returned operation is performed.
     * @return An {@code ObjIntSQLConsumer} that performs the {@code operation} operation on its input, and unwraps any {@link UncheckedSQLException}
     *         that is thrown.
     * @throws NullPointerException If the given operation is {@code null}.
     */
    static <T> ObjIntSQLConsumer<T> checked(ObjIntConsumer<? super T> operation) {
        Objects.requireNonNull(operation);
        return (t, value) -> {
            try {
                operation.accept(t, value);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
