/*
 * DoubleSQLFunction.java
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
import java.util.function.DoubleFunction;

/**
 * Represents a function that accepts a double-valued argument and produces a result.
 * This is the {@link SQLException} throwing equivalent of {@link DoubleFunction}.
 *
 * @param <R> The type of the result of the function.
 */
@FunctionalInterface
public interface DoubleSQLFunction<R> {

    /**
     * Applies this function to the given argument.
     *
     * @param value The function argument.
     * @return The function result.
     * @throws SQLException If an SQL error occurs.
     */
    R apply(double value) throws SQLException;

    /**
     * Returns a function that applies the {@code function} function to its input, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param <R> The type of the result of the function.
     * @param function The function to apply when the returned function is applied.
     * @return A function that applies the {@code function} function to its input, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <R> DoubleFunction<R> unchecked(DoubleSQLFunction<? extends R> function) {
        Objects.requireNonNull(function);
        return value -> {
            try {
                return function.apply(value);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a function that applies the {@code function} function to its input, and unwraps any {@link UncheckedSQLException} that is thrown by
     * throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param <R> The type of the result of the function.
     * @param function The function to apply when the returned function is applied.
     * @return A function that applies the {@code function} function to its input, and unwraps any {@link UncheckedSQLException} that is thrown.
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <R> DoubleSQLFunction<R> checked(DoubleFunction<? extends R> function) {
        Objects.requireNonNull(function);
        return value -> {
            try {
                return function.apply(value);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
