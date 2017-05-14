/*
 * SQLConsumer.java
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
import java.util.function.Consumer;

/**
 * Represents an operation that accepts a single input argument and returns no result.
 * This is the {@link SQLException} throwing equivalent of {@link Consumer}.
 *
 * @param <T> The type of the input to the operation.
 */
@FunctionalInterface
public interface SQLConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t The input argument.
     * @throws SQLException If an SQL error occurs.
     */
    void accept(T t) throws SQLException;

    /**
     * Returns a composed {@code SQLConsumer} that performs, in sequence, this operation followed by the {@code after} operation.
     * If performing either operation throws an exception, it is relayed to the caller of the composed operation.
     * If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after The operation to perform after this operation.
     * @return A composed {@code SQLConsumer} that performs in sequence this operation followed by the {@code after} operation.
     * @throws NullPointerException If {@code after} is {@code null}.
     */
    default SQLConsumer<T> andThen(SQLConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return t -> {
            accept(t);
            after.accept(t);
        };
    }

    /**
     * Returns a {@code Consumer} that performs the {@code operation} operation, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param <T> The type of the input to the operation.
     * @param operation The operation to perform when the returned operation is performed.
     * @return A {@code Consumer} that performs the {@code operation} operation on its input, and wraps any {@link SQLException} that is thrown in
     *         an {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code operation} is {@code null}.
     */
    static <T> Consumer<T> unchecked(SQLConsumer<? super T> operation) {
        Objects.requireNonNull(operation);
        return t -> {
            try {
                operation.accept(t);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns an {@code SQLConsumer} that performs the {@code operation} operation, and unwraps any {@link UncheckedSQLException} that is thrown by
     * throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param <T> The type of the input to the operation.
     * @param operation The operation to perform when the returned operation is performed.
     * @return An {@code SQLConsumer} that performs the {@code operation} operation on its input, and unwraps any {@link UncheckedSQLException} that
     *         is thrown.
     * @throws NullPointerException If the given operation is {@code null}.
     */
    static <T> SQLConsumer<T> checked(Consumer<? super T> operation) {
        Objects.requireNonNull(operation);
        return t -> {
            try {
                operation.accept(t);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
