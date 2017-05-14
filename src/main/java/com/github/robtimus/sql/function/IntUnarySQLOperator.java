/*
 * IntUnarySQLOperator.java
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
import java.util.function.IntUnaryOperator;

/**
 * Represents an operation on a single {@code int}-valued operand that produces an {@code int}-valued result.
 * This is the {@link SQLException} throwing equivalent of {@link IntUnaryOperator}.
 */
@FunctionalInterface
public interface IntUnarySQLOperator {

    /**
     * Applies this operator to the given operand.
     *
     * @param operand The operand.
     * @return The operator result.
     * @throws SQLException If an SQL error occurs.
     */
    int applyAsInt(int operand) throws SQLException;

    /**
     * Returns a composed operator that first applies the {@code before} operator to its input, and then applies this operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to the caller of the composed operator.
     *
     * @param before The operator to apply before this operator is applied.
     * @return A composed operator that first applies the {@code before} operator and then applies this operator.
     * @throws NullPointerException If {@code before} is {@code null}.
     * @see #andThen(IntUnarySQLOperator)
     */
    default IntUnarySQLOperator compose(IntUnarySQLOperator before) {
        Objects.requireNonNull(before);
        return operand -> applyAsInt(before.applyAsInt(operand));
    }

    /**
     * Returns a composed operator that first applies this operator to its input, and then applies the {@code after} operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to the caller of the composed operator.
     *
     * @param after The operator to apply after this operator is applied.
     * @return A composed operator that first applies this operator and then applies the {@code after} operator.
     * @throws NullPointerException If {@code after} is {@code null}.
     * @see #compose(IntUnarySQLOperator)
     */
    default IntUnarySQLOperator andThen(IntUnarySQLOperator after) {
        Objects.requireNonNull(after);
        return operand -> after.applyAsInt(applyAsInt(operand));
    }

    /**
     * Returns a unary operator that always returns its input argument.
     *
     * @return a unary operator that always returns its input argument
     */
    static IntUnarySQLOperator identity() {
        return operand -> operand;
    }

    /**
     * Returns a unary operator that applies the {@code operator} operator to its input, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param operator The unary operator to apply when the returned unary operator is applied.
     * @return A unary operator that applies the {@code operator} operator to its input, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code operator} is {@code null}.
     */
    static IntUnaryOperator unchecked(IntUnarySQLOperator operator) {
        Objects.requireNonNull(operator);
        return operand -> {
            try {
                return operator.applyAsInt(operand);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a unary operator that applies the {@code operator} operator to its input, and unwraps any {@link UncheckedSQLException} that is thrown
     * by throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param operator The unary operator to apply when the returned unary operator is applied.
     * @return A unary operator that applies the {@code operator} operator to its input, and unwraps any {@link UncheckedSQLException} that is thrown.
     * @throws NullPointerException If {@code operator} is {@code null}.
     */
    static IntUnarySQLOperator checked(IntUnaryOperator operator) {
        Objects.requireNonNull(operator);
        return operand -> {
            try {
                return operator.applyAsInt(operand);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
