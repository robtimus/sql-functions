/*
 * LongUnarySQLOperatorTest.java
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

import static com.github.robtimus.sql.function.LongUnarySQLOperator.checked;
import static com.github.robtimus.sql.function.LongUnarySQLOperator.identity;
import static com.github.robtimus.sql.function.LongUnarySQLOperator.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.LongUnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class LongUnarySQLOperatorTest {

    private static final long TEST_VALUE = System.currentTimeMillis();
    private static final long TEST_RESULT = TEST_VALUE * 2;

    @Nested
    @DisplayName("compose(LongUnarySQLOperator)")
    class Compose {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            LongUnarySQLOperator operator = t -> TEST_RESULT;

            assertThrows(NullPointerException.class, () -> operator.compose(null));
        }

        @Test
        @DisplayName("applies and applies")
        void testAppliesAndApplies() throws SQLException {
            LongUnarySQLOperator operator = t -> TEST_RESULT;
            LongUnarySQLOperator before = t -> TEST_VALUE;
            LongUnarySQLOperator combined = operator.compose(before);

            assertEquals(TEST_RESULT, combined.applyAsLong(TEST_RESULT));
        }

        @Test
        @DisplayName("applies and throws")
        void testAcceptsAndThrows() {
            LongUnarySQLOperator operator = t -> TEST_RESULT;
            LongUnarySQLOperator before = t -> {
                throw new SQLException("before");
            };
            LongUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsLong(TEST_RESULT));
            assertEquals("before", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        void testThrowsAndAccepts() {
            LongUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            LongUnarySQLOperator before = t -> TEST_VALUE;
            LongUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsLong(TEST_RESULT));
            assertEquals("operator", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            LongUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            LongUnarySQLOperator before = t -> {
                throw new SQLException("before");
            };
            LongUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsLong(TEST_RESULT));
            assertEquals("before", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("andThen(LongUnarySQLOperator)")
    class AndThen {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            LongUnarySQLOperator operator = t -> TEST_RESULT;

            assertThrows(NullPointerException.class, () -> operator.andThen(null));
        }

        @Test
        @DisplayName("applies and applies")
        void testAppliesAndApplies() throws SQLException {
            LongUnarySQLOperator operator = t -> TEST_RESULT;
            LongUnarySQLOperator after = t -> TEST_VALUE;
            LongUnarySQLOperator combined = operator.andThen(after);

            assertEquals(TEST_VALUE, combined.applyAsLong(TEST_VALUE));
        }

        @Test
        @DisplayName("applies and throws")
        void testAcceptsAndThrows() {
            LongUnarySQLOperator operator = t -> TEST_RESULT;
            LongUnarySQLOperator after = t -> {
                throw new SQLException("after");
            };
            LongUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsLong(TEST_VALUE));
            assertEquals("after", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        void testThrowsAndAccepts() {
            LongUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            LongUnarySQLOperator after = t -> TEST_VALUE;
            LongUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsLong(TEST_VALUE));
            assertEquals("operator", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            LongUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            LongUnarySQLOperator after = t -> {
                throw new SQLException("after");
            };
            LongUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsLong(TEST_VALUE));
            assertEquals("operator", exception.getMessage());
        }
    }

    @Test
    @DisplayName("identity()")
    void testIdentity() throws SQLException {
        LongUnarySQLOperator operator = identity();

        assertEquals(TEST_VALUE, operator.applyAsLong(TEST_VALUE));
    }

    @Nested
    @DisplayName("unchecked(LongUnarySQLOperator)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() {
            LongUnarySQLOperator sqlOperator = t -> TEST_RESULT;
            LongUnaryOperator operator = unchecked(sqlOperator);

            assertEquals(TEST_RESULT, operator.applyAsLong(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            LongUnarySQLOperator sqlOperator = t -> {
                throw new SQLException("sqlOperator");
            };
            LongUnaryOperator operator = unchecked(sqlOperator);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> operator.applyAsLong(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlOperator", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(UnaryOperator<? super T, ? extends R>)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() throws SQLException {
            LongUnaryOperator operator = t -> TEST_RESULT;
            LongUnarySQLOperator sqlOperator = checked(operator);

            assertEquals(TEST_RESULT, sqlOperator.applyAsLong(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            LongUnaryOperator operator = t -> {
                throw new UncheckedSQLException(e);
            };
            LongUnarySQLOperator sqlOperator = checked(operator);

            SQLException exception = assertThrows(SQLException.class, () -> sqlOperator.applyAsLong(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            LongUnaryOperator operator = t -> {
                throw e;
            };
            LongUnarySQLOperator sqlOperator = checked(operator);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlOperator.applyAsLong(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
