/*
 * ToIntSQLFunction.java
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
import java.util.function.ToIntFunction;

/**
 * Represents a function that produces an int-valued result.
 * This is the {@link SQLException} throwing equivalent of {@link ToIntFunction}.
 *
 * @param <T> The type of the input to the function.
 */
@FunctionalInterface
public interface ToIntSQLFunction<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param value The function argument.
     * @return The function result.
     * @throws SQLException If an SQL error occurs.
     */
    int applyAsInt(T value) throws SQLException;

    /**
     * Returns a function that applies the {@code function} function to its input, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param <T> The type of the first argument to the function.
     * @param function The function to apply when the returned function is applied.
     * @return A function that applies the {@code function} function to its input, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <T> ToIntFunction<T> unchecked(ToIntSQLFunction<? super T> function) {
        Objects.requireNonNull(function);
        return value -> {
            try {
                return function.applyAsInt(value);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a function that applies the {@code function} function to its input, and unwraps any {@link UncheckedSQLException} that is thrown by
     * throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param <T> The type of the first argument to the function.
     * @param function The function to apply when the returned function is applied.
     * @return A function that applies the {@code function} function to its input, and unwraps any {@link UncheckedSQLException} that is thrown.
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <T> ToIntSQLFunction<T> checked(ToIntFunction<? super T> function) {
        Objects.requireNonNull(function);
        return value -> {
            try {
                return function.applyAsInt(value);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
