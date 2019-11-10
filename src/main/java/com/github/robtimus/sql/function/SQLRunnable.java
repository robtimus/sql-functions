/*
 * SQLRunnable.java
 * Copyright 2019 Rob Spoor
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

/**
 * Represents an action that accepts no input and returns no result.
 * This is the {@link SQLException} throwing equivalent of {@link Runnable}.
 *
 * @author Rob Spoor
 * @since 1.1
 */
public interface SQLRunnable {

    /**
     * Performs this action.
     *
     * @throws SQLException If an SQL error occurs.
     */
    void run() throws SQLException;

    /**
     * Returns a {@code Runnable} that performs the {@code action} action, and wraps any {@link SQLException} that is thrown in an
     * {@link UncheckedSQLException}.
     *
     * @param action The action to perform when the returned action is performed.
     * @return A {@code Runnable} that performs the {@code action} action, and wraps any {@link SQLException} that is thrown in an
     *         {@link UncheckedSQLException}.
     * @throws NullPointerException If {@code action} is {@code null}.
     */
    static Runnable unchecked(SQLRunnable action) {
        Objects.requireNonNull(action);
        return () -> {
            try {
                action.run();
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        };
    }

    /**
     * Returns a {@code Runnable} that performs the {@code action} action, and unwraps any {@link UncheckedSQLException} that is thrown by throwing
     * its {@link UncheckedSQLException#getCause() cause}.
     *
     * @param action The action to perform when the returned action is performed.
     * @return A {@code Runnable} that performs the {@code action} action, and unwraps any {@link SQLException} that is thrown by throwing its
     *        {@link UncheckedSQLException#getCause() cause}..
     * @throws NullPointerException If {@code action} is {@code null}.
     */
    static SQLRunnable checked(Runnable action) {
        Objects.requireNonNull(action);
        return () -> {
            try {
                action.run();
            } catch (UncheckedSQLException e) {
                throw e.getCause();
            }
        };
    }
}
