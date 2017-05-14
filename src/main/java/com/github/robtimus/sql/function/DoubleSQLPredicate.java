/*
 * DoubleSQLPredicate.java
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
import java.util.function.DoublePredicate;

/**
 * Represents a predicate (boolean-valued function) of one {@code double}-valued argument.
 * This is the {@link SQLException} throwing equivalent of {@link DoublePredicate}.
 */
@FunctionalInterface
public interface DoubleSQLPredicate {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param value The input argument.
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}.
     * @throws SQLException If an SQL error occurs.
     */
    boolean test(double value) throws SQLException;

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate and another.
     * When evaluating the composed predicate, if this predicate is {@code false}, then the {@code other} predicate is not evaluated.
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller;
     * if evaluation of this predicate throws an exception, the {@code other} predicate will not be evaluated.
     *
     * @param other A predicate that will be logically-ANDed with this predicate.
     * @return A composed predicate that represents the short-circuiting logical AND of this predicate and the {@code other} predicate.
     * @throws NullPointerException If {@code other} is {@code null}.
     */
    default DoubleSQLPredicate and(DoubleSQLPredicate other) {
        Objects.requireNonNull(other);
        return value -> test(value) && other.test(value);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return A predicate that represents the logical negation of this predicate.
     */
    default DoubleSQLPredicate negate() {
        return value -> !test(value);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another.
     * When evaluating the composed predicate, if this predicate is {@code true}, then the {@code other} predicate is not evaluated.
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller;
     * if evaluation of this predicate throws an exception, the {@code other} predicate will not be evaluated.
     *
     * @param other A predicate that will be logically-ORed with this predicate.
     * @return A composed predicate that represents the short-circuiting logical OR of this predicate and the {@code other} predicate.
     * @throws NullPointerException If {@code other} is {@code null}.
     */
    default DoubleSQLPredicate or(DoubleSQLPredicate other) {
        Objects.requireNonNull(other);
        return value -> test(value) || other.test(value);
    }

    /**
     * Returns a predicate that evaluates the {@code predicate} predicate, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param predicate The predicate to evaluate when the returned predicate is evaluated.
     * @return A predicate that evaluates the {@code predicate} predicate on its input, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code predicate} is {@code null}.
     */
    static DoublePredicate unchecked(DoubleSQLPredicate predicate) {
        Objects.requireNonNull(predicate);
        return value -> {
            try {
                return predicate.test(value);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a predicate that evaluates the {@code predicate} predicate, and unwraps any {@link UncheckedSQLException} that is thrown by
     * throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param predicate The predicate to evaluate when the returned predicate is evaluated.
     * @return A predicate that evaluates the {@code predicate} operation on its input, and unwraps any {@link UncheckedSQLException} that is thrown.
     * @throws NullPointerException If the given operation is {@code null}.
     */
    static DoubleSQLPredicate checked(DoublePredicate predicate) {
        Objects.requireNonNull(predicate);
        return value -> {
            try {
                return predicate.test(value);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
