/*
 * SQLPredicate.java
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
import java.util.function.Predicate;

/**
 * Represents a predicate (boolean-valued function) of one argument.
 * This is the {@link SQLException} throwing equivalent of {@link Predicate}.
 *
 * @param <T> The type of the input to the predicate.
 */
@FunctionalInterface
public interface SQLPredicate<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t The input argument.
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}.
     * @throws SQLException If an SQL error occurs.
     */
    boolean test(T t) throws SQLException;

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
    default SQLPredicate<T> and(SQLPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) && other.test(t);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return A predicate that represents the logical negation of this predicate
     */
    default SQLPredicate<T> negate() {
        return t -> !test(t);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another.
     * When evaluating the composed predicate, if this predicate is {@code true}, then the {@code other} predicate is not evaluated.
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller;
     * if evaluation of this predicate throws an exception, the {@code other} predicate will not be evaluated.
     *
     * @param other A predicate that will be logically-ORed with this predicate
     * @return A composed predicate that represents the short-circuiting logical OR of this predicate and the {@code other} predicate.
     * @throws NullPointerException If {@code other} is {@code null}.
     */
    default SQLPredicate<T> or(SQLPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) || other.test(t);
    }

    /**
     * Returns a predicate that tests if two arguments are equal according to {@link Objects#equals(Object, Object)}.
     *
     * @param <T> The type of arguments to the predicate.
     * @param targetRef The object reference with which to compare for equality, which may be {@code null}.
     * @return A predicate that tests if two arguments are equal according to {@link Objects#equals(Object, Object)}.
     */
    static <T> SQLPredicate<T> isEqual(Object targetRef) {
        return t -> Objects.equals(targetRef, t);
    }

    /**
     * Returns a predicate that evaluates the {@code predicate} predicate, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param <T> The type of arguments to the predicate.
     * @param predicate The predicate to evaluate when the returned predicate is evaluated.
     * @return A predicate that evaluates the {@code predicate} predicate on its input, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code predicate} is {@code null}.
     */
    static <T> Predicate<T> unchecked(SQLPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return t -> {
            try {
                return predicate.test(t);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a predicate that evaluates the {@code predicate} predicate, and unwraps any {@link UncheckedSQLException} that is thrown by
     * throwing its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param <T> The type of arguments to the predicate.
     * @param predicate The predicate to evaluate when the returned predicate is evaluated.
     * @return A predicate that evaluates the {@code predicate} operation on its input, and unwraps any {@link UncheckedSQLException} that is thrown.
     * @throws NullPointerException If the given operation is {@code null}.
     */
    static <T> SQLPredicate<T> checked(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return t -> {
            try {
                return predicate.test(t);
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }

    /**
     * Returns a predicate that represents the logical negation of another predicate.
     * This is accomplished by returning the result of calling {@code target.negate()}.
     *
     * @param <T> The type of the argument to the predicate.
     * @param target The predicate to negate.
     * @return A predicate that represents the logical negation of the given predicate
     * @throws NullPointerException If the given predicate is {@code null}.
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    static <T> SQLPredicate<T> not(SQLPredicate<? super T> target) {
        return (SQLPredicate<T>) target.negate();
    }
}
