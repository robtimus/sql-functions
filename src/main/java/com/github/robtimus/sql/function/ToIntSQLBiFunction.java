/*
 * ToIntSQLBiFunction.java
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
import java.util.function.ToIntBiFunction;

/**
 * Represents a function that accepts two arguments and produces an int-valued result.
 * This is the {@link SQLException} throwing equivalent of {@link ToIntBiFunction}.
 *
 * @param <T> The type of the first argument to the function.
 * @param <U> The type of the second argument to the function.
 */
@FunctionalInterface
public interface ToIntSQLBiFunction<T, U> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t The first function argument.
     * @param u The second function argument.
     * @return The function result.
     * @throws SQLException If an SQL error occurs.
     */
    int applyAsInt(T t, U u) throws SQLException;

    /**
     * Returns a function that applies the {@code function} function to its input, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param <T> The type of the first argument to the function.
     * @param <U> The type of the second argument to the function.
     * @param function The function to apply when the returned function is applied.
     * @return A function that applies the {@code function} function to its input, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <T, U> ToIntBiFunction<T, U> unchecked(ToIntSQLBiFunction<? super T, ? super U> function) {
        Objects.requireNonNull(function);
        return (t, u) -> {
            try {
                return function.applyAsInt(t, u);
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
     * @param <U> The type of the second argument to the function.
     * @param function The function to apply when the returned function is applied.
     * @return A function that applies the {@code function} function to its input, and unwraps any {@link UncheckedSQLException} that is thrown.
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <T, U> ToIntSQLBiFunction<T, U> checked(ToIntBiFunction<? super T, ? super U> function) {
        Objects.requireNonNull(function);
        return (t, u) -> {
            try {
                return function.applyAsInt(t, u);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
