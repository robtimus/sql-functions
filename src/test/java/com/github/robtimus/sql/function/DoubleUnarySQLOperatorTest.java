/*
 * DoubleUnarySQLOperatorTest.java
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

import static com.github.robtimus.sql.function.DoubleUnarySQLOperator.checked;
import static com.github.robtimus.sql.function.DoubleUnarySQLOperator.identity;
import static com.github.robtimus.sql.function.DoubleUnarySQLOperator.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class DoubleUnarySQLOperatorTest {

    private static final double TEST_VALUE = Math.PI;
    private static final double TEST_RESULT = Math.E;

    @Nested
    @DisplayName("compose(DoubleUnarySQLOperator)")
    class Compose {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            DoubleUnarySQLOperator operator = t -> TEST_RESULT;

            assertThrows(NullPointerException.class, () -> operator.compose(null));
        }

        @Test
        @DisplayName("applies and applies")
        void testAppliesAndApplies() throws SQLException {
            DoubleUnarySQLOperator operator = t -> TEST_RESULT;
            DoubleUnarySQLOperator before = t -> TEST_VALUE;
            DoubleUnarySQLOperator combined = operator.compose(before);

            assertEquals(TEST_RESULT, combined.applyAsDouble(TEST_RESULT));
        }

        @Test
        @DisplayName("applies and throws")
        void testAcceptsAndThrows() {
            DoubleUnarySQLOperator operator = t -> TEST_RESULT;
            DoubleUnarySQLOperator before = t -> {
                throw new SQLException("before");
            };
            DoubleUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsDouble(TEST_RESULT));
            assertEquals("before", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        void testThrowsAndAccepts() {
            DoubleUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            DoubleUnarySQLOperator before = t -> TEST_VALUE;
            DoubleUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsDouble(TEST_RESULT));
            assertEquals("operator", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            DoubleUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            DoubleUnarySQLOperator before = t -> {
                throw new SQLException("before");
            };
            DoubleUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsDouble(TEST_RESULT));
            assertEquals("before", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("andThen(DoubleUnarySQLOperator)")
    class AndThen {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            DoubleUnarySQLOperator operator = t -> TEST_RESULT;

            assertThrows(NullPointerException.class, () -> operator.andThen(null));
        }

        @Test
        @DisplayName("applies and applies")
        void testAppliesAndApplies() throws SQLException {
            DoubleUnarySQLOperator operator = t -> TEST_RESULT;
            DoubleUnarySQLOperator after = t -> TEST_VALUE;
            DoubleUnarySQLOperator combined = operator.andThen(after);

            assertEquals(TEST_VALUE, combined.applyAsDouble(TEST_VALUE));
        }

        @Test
        @DisplayName("applies and throws")
        void testAcceptsAndThrows() {
            DoubleUnarySQLOperator operator = t -> TEST_RESULT;
            DoubleUnarySQLOperator after = t -> {
                throw new SQLException("after");
            };
            DoubleUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsDouble(TEST_VALUE));
            assertEquals("after", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        void testThrowsAndAccepts() {
            DoubleUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            DoubleUnarySQLOperator after = t -> TEST_VALUE;
            DoubleUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsDouble(TEST_VALUE));
            assertEquals("operator", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            DoubleUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            DoubleUnarySQLOperator after = t -> {
                throw new SQLException("after");
            };
            DoubleUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsDouble(TEST_VALUE));
            assertEquals("operator", exception.getMessage());
        }
    }

    @Test
    @DisplayName("identity()")
    void testIdentity() throws SQLException {
        DoubleUnarySQLOperator operator = identity();

        assertEquals(TEST_VALUE, operator.applyAsDouble(TEST_VALUE));
    }

    @Nested
    @DisplayName("unchecked(DoubleUnarySQLOperator)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() {
            DoubleUnarySQLOperator sqlOperator = t -> TEST_RESULT;
            DoubleUnaryOperator operator = unchecked(sqlOperator);

            assertEquals(TEST_RESULT, operator.applyAsDouble(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            DoubleUnarySQLOperator sqlOperator = t -> {
                throw new SQLException("sqlOperator");
            };
            DoubleUnaryOperator operator = unchecked(sqlOperator);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> operator.applyAsDouble(TEST_VALUE));
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
            DoubleUnaryOperator operator = t -> TEST_RESULT;
            DoubleUnarySQLOperator sqlOperator = checked(operator);

            assertEquals(TEST_RESULT, sqlOperator.applyAsDouble(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            DoubleUnaryOperator operator = t -> {
                throw new UncheckedSQLException(e);
            };
            DoubleUnarySQLOperator sqlOperator = checked(operator);

            SQLException exception = assertThrows(SQLException.class, () -> sqlOperator.applyAsDouble(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            DoubleUnaryOperator operator = t -> {
                throw e;
            };
            DoubleUnarySQLOperator sqlOperator = checked(operator);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlOperator.applyAsDouble(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
