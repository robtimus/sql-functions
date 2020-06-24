/*
 * SQLRunnableTest.java
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

import static com.github.robtimus.sql.function.SQLRunnable.checked;
import static com.github.robtimus.sql.function.SQLRunnable.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class SQLRunnableTest {

    private static final String TEST_VALUE = "foo";

    @Nested
    @DisplayName("unchecked(SQLRunnable)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("accepts")
        void testAccepts() {
            List<String> list = new ArrayList<>();
            SQLRunnable sqlAction = () -> list.add(TEST_VALUE);
            Runnable action = unchecked(sqlAction);

            action.run();
            assertEquals(Collections.singletonList(TEST_VALUE), list);
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            SQLRunnable sqlAction = () -> {
                throw new SQLException("sqlAction");
            };
            Runnable action = unchecked(sqlAction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> action.run());
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlAction", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(Runnable)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("runs")
        void testAccepts() throws SQLException {
            List<String> list = new ArrayList<>();

            Runnable action = () -> list.add(TEST_VALUE);
            SQLRunnable sqlAction = checked(action);

            sqlAction.run();
            assertEquals(Collections.singletonList(TEST_VALUE), list);
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            Runnable action = () -> {
                throw new UncheckedSQLException(e);
            };
            SQLRunnable sqlAction = checked(action);

            SQLException exception = assertThrows(SQLException.class, () -> sqlAction.run());
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            Runnable action = () -> {
                throw e;
            };
            SQLRunnable sqlAction = checked(action);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlAction.run());
            assertSame(e, exception);
        }
    }
}
