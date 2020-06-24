/*
 * LongBinarySQLOperatorTest.java
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

import static com.github.robtimus.sql.function.LongBinarySQLOperator.checked;
import static com.github.robtimus.sql.function.LongBinarySQLOperator.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.LongBinaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class LongBinarySQLOperatorTest {

    private static final long TEST_VALUE1 = System.currentTimeMillis();
    private static final long TEST_VALUE2 = TEST_VALUE1 * 2;
    private static final long TEST_RESULT = TEST_VALUE2 * 2;

    @Nested
    @DisplayName("unchecked(LongBinarySQLOperator)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() {
            LongBinarySQLOperator sqlOperator = (t, u) -> TEST_RESULT;
            LongBinaryOperator operator = unchecked(sqlOperator);

            assertEquals(TEST_RESULT, operator.applyAsLong(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            LongBinarySQLOperator sqlOperator = (t, u) -> {
                throw new SQLException("sqlOperator");
            };
            LongBinaryOperator operator = unchecked(sqlOperator);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> operator.applyAsLong(TEST_VALUE1, TEST_VALUE2));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlOperator", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(LongBinaryOperator<T>)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() throws SQLException {
            LongBinaryOperator operator = (t, u) -> TEST_RESULT;
            LongBinarySQLOperator sqlOperator = checked(operator);

            assertEquals(TEST_RESULT, sqlOperator.applyAsLong(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            LongBinaryOperator operator = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            LongBinarySQLOperator sqlOperator = checked(operator);

            SQLException exception = assertThrows(SQLException.class, () -> sqlOperator.applyAsLong(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            LongBinaryOperator operator = (t, u) -> {
                throw e;
            };
            LongBinarySQLOperator sqlOperator = checked(operator);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlOperator.applyAsLong(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
