/*
 * IntBinarySQLOperator.java
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
import java.util.function.IntBinaryOperator;

/**
 * Represents an operation upon two {@code int}-valued operands and producing an {@code int}-valued result.
 * This is the {@link SQLException} throwing equivalent of {@link IntBinarySQLOperator}.
 */
@FunctionalInterface
public interface IntBinarySQLOperator {

    /**
     * Applies this operator to the given operands.
     *
     * @param left The first operand.
     * @param right The second operand.
     * @return The operator result.
     * @throws SQLException If an SQL error occurs.
     */
    int applyAsInt(int left, int right) throws SQLException;

    /**
     * Returns a binary operator that applies the {@code operator} operator to its input, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param operator The binary operator to apply when the returned binary operator is applied.
     * @return A binary operator that applies the {@code operator} operator to its input, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code operator} is {@code null}.
     */
    static IntBinaryOperator unchecked(IntBinarySQLOperator operator) {
        Objects.requireNonNull(operator);
        return (left, right) -> {
            try {
                return operator.applyAsInt(left, right);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a binary operator that applies the {@code operator} operator to its input, and unwraps any {@link UncheckedSQLException} that is thrown
     * by throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param operator The binary operator to apply when the returned binary operator is applied.
     * @return A binary operator that applies the {@code operator} operator to its input, and unwraps any {@link UncheckedSQLException} that is
     *         thrown.
     * @throws NullPointerException If {@code operator} is {@code null}.
     */
    static IntBinarySQLOperator checked(IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return (left, right) -> {
            try {
                return operator.applyAsInt(left, right);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
