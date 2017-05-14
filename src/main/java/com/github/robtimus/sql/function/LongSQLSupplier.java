/*
 * LongSQLSupplier.java
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
import java.util.function.LongSupplier;

/**
 * Represents a supplier of {@code long}-valued results.
 * This is the {@link SQLException} throwing equivalent of {@link LongSupplier}.
 */
@FunctionalInterface
public interface LongSQLSupplier {

    /**
     * Gets a result.
     *
     * @return A result.
     * @throws SQLException If an SQL error occurs.
     */
    long getAsLong() throws SQLException;

    /**
     * Returns a supplier that returns the result of the {@code supplier} supplier, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param supplier The supplier that will provide results for the returned supplier.
     * @return A supplier that returns the result of the {@code supplier} supplier, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code supplier} is {@code null}.
     */
    static LongSupplier unchecked(LongSQLSupplier supplier) {
        Objects.requireNonNull(supplier);
        return () -> {
            try {
                return supplier.getAsLong();
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a supplier that returns the result of the {@code supplier} supplier, and unwraps any {@link UncheckedSQLException} that is thrown by
     * throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param supplier The supplier that will provide results for the returned supplier.
     * @return A supplier that returns the result of the {@code supplier} supplier, and unwraps any {@link UncheckedSQLException} that is thrown.
     * @throws NullPointerException If {@code supplier} is {@code null}.
     */
    static LongSQLSupplier checked(LongSupplier supplier) {
        Objects.requireNonNull(supplier);
        return () -> {
            try {
                return supplier.getAsLong();
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
