/*
 * DoubleSQLConsumer.java
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
import java.util.function.DoubleConsumer;

/**
 * Represents an operation that accepts a single {@code double}-valued argument and returns no result.
 * This is the {@link SQLException} throwing equivalent of {@link DoubleConsumer}.
 */
@FunctionalInterface
public interface DoubleSQLConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value The input argument.
     * @throws SQLException If an SQL error occurs.
     */
    void accept(double value) throws SQLException;

    /**
     * Returns a composed {@code DoubleSQLConsumer} that performs, in sequence, this operation followed by the {@code after} operation.
     * If performing either operation throws an exception, it is relayed to the caller of the composed operation.
     * If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after The operation to perform after this operation.
     * @return A composed {@code DoubleSQLConsumer} that performs in sequence this operation followed by the {@code after} operation.
     * @throws NullPointerException If {@code after} is {@code null}.
     */
    default DoubleSQLConsumer andThen(DoubleSQLConsumer after) {
        Objects.requireNonNull(after);
        return value -> {
            accept(value);
            after.accept(value);
        };
    }

    /**
     * Returns a {@code DoubleConsumer} that performs the {@code operation} operation, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param operation The operation to perform when the returned operation is performed.
     * @return A {@code DoubleConsumer} that performs the {@code operation} operation on its input, and wraps any {@link SQLException} that is thrown
     *         in an {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code operation} is {@code null}.
     */
    static DoubleConsumer unchecked(DoubleSQLConsumer operation) {
        Objects.requireNonNull(operation);
        return value -> {
            try {
                operation.accept(value);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns an {@code DoubleSQLConsumer} that performs the {@code operation} operation, and unwraps any {@link UncheckedSQLException} that is
     * thrown by throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param operation The operation to perform when the returned operation is performed.
     * @return An {@code DoubleSQLConsumer} that performs the {@code operation} operation on its input, and unwraps any {@link UncheckedSQLException}
     *         that is thrown.
     * @throws NullPointerException If the given operation is {@code null}.
     */
    static DoubleSQLConsumer checked(DoubleConsumer operation) {
        Objects.requireNonNull(operation);
        return value -> {
            try {
                operation.accept(value);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
