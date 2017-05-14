/*
 * SQLSupplier.java
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
import java.util.function.Supplier;

/**
 * Represents a supplier of results.
 * This is the {@link SQLException} throwing equivalent of {@link Supplier}.
 *
 * @param <T> The type of results supplied by this supplier.
 */
@FunctionalInterface
public interface SQLSupplier<T> {

    /**
     * Gets a result.
     *
     * @return A result.
     * @throws SQLException If an SQL error occurs.
     */
    T get() throws SQLException;

    /**
     * Returns a supplier that returns the result of the {@code supplier} supplier, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param <T> The type of results supplied by the supplier.
     * @param supplier The supplier that will provide results for the returned supplier.
     * @return A supplier that returns the result of the {@code supplier} supplier, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code supplier} is {@code null}.
     */
    static <T> Supplier<T> unchecked(SQLSupplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        return () -> {
            try {
                return supplier.get();
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a supplier that returns the result of the {@code supplier} supplier, and unwraps any {@link UncheckedSQLException} that is thrown by
     * throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param <T> The type of results supplied by the supplier.
     * @param supplier The supplier that will provide results for the returned supplier.
     * @return A supplier that returns the result of the {@code supplier} supplier, and unwraps any {@link UncheckedSQLException} that is thrown.
     * @throws NullPointerException If {@code supplier} is {@code null}.
     */
    static <T> SQLSupplier<T> checked(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        return () -> {
            try {
                return supplier.get();
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
