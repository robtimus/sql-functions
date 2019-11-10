/*
 * IntUnarySQLOperatorTest.java
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

import static com.github.robtimus.sql.function.IntUnarySQLOperator.checked;
import static com.github.robtimus.sql.function.IntUnarySQLOperator.identity;
import static com.github.robtimus.sql.function.IntUnarySQLOperator.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.IntUnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class IntUnarySQLOperatorTest {

    private static final int TEST_VALUE = 13;
    private static final int TEST_RESULT = 481;

    @Nested
    @DisplayName("compose(IntUnarySQLOperator)")
    public class Compose {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            IntUnarySQLOperator operator = t -> TEST_RESULT;

            assertThrows(NullPointerException.class, () -> operator.compose(null));
        }

        @Test
        @DisplayName("applies and applies")
        public void testAppliesAndApplies() throws SQLException {
            IntUnarySQLOperator operator = t -> TEST_RESULT;
            IntUnarySQLOperator before = t -> TEST_VALUE;
            IntUnarySQLOperator combined = operator.compose(before);

            assertEquals(TEST_RESULT, combined.applyAsInt(TEST_RESULT));
        }

        @Test
        @DisplayName("applies and throws")
        public void testAcceptsAndThrows() {
            IntUnarySQLOperator operator = t -> TEST_RESULT;
            IntUnarySQLOperator before = t -> {
                throw new SQLException("before");
            };
            IntUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsInt(TEST_RESULT));
            assertEquals("before", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        public void testThrowsAndAccepts() {
            IntUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            IntUnarySQLOperator before = t -> TEST_VALUE;
            IntUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsInt(TEST_RESULT));
            assertEquals("operator", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        public void testThrowsAndThrows() {
            IntUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            IntUnarySQLOperator before = t -> {
                throw new SQLException("before");
            };
            IntUnarySQLOperator combined = operator.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsInt(TEST_RESULT));
            assertEquals("before", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("andThen(IntUnarySQLOperator)")
    public class AndThen {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            IntUnarySQLOperator operator = t -> TEST_RESULT;

            assertThrows(NullPointerException.class, () -> operator.andThen(null));
        }

        @Test
        @DisplayName("applies and applies")
        public void testAppliesAndApplies() throws SQLException {
            IntUnarySQLOperator operator = t -> TEST_RESULT;
            IntUnarySQLOperator after = t -> TEST_VALUE;
            IntUnarySQLOperator combined = operator.andThen(after);

            assertEquals(TEST_VALUE, combined.applyAsInt(TEST_VALUE));
        }

        @Test
        @DisplayName("applies and throws")
        public void testAcceptsAndThrows() {
            IntUnarySQLOperator operator = t -> TEST_RESULT;
            IntUnarySQLOperator after = t -> {
                throw new SQLException("after");
            };
            IntUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsInt(TEST_VALUE));
            assertEquals("after", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        public void testThrowsAndAccepts() {
            IntUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            IntUnarySQLOperator after = t -> TEST_VALUE;
            IntUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsInt(TEST_VALUE));
            assertEquals("operator", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        public void testThrowsAndThrows() {
            IntUnarySQLOperator operator = t -> {
                throw new SQLException("operator");
            };
            IntUnarySQLOperator after = t -> {
                throw new SQLException("after");
            };
            IntUnarySQLOperator combined = operator.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.applyAsInt(TEST_VALUE));
            assertEquals("operator", exception.getMessage());
        }
    }

    @Test
    @DisplayName("identity()")
    public void testIdentity() throws SQLException {
        IntUnarySQLOperator operator = identity();

        assertEquals(TEST_VALUE, operator.applyAsInt(TEST_VALUE));
    }

    @Nested
    @DisplayName("unchecked(IntUnarySQLOperator)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            IntUnarySQLOperator sqlOperator = t -> TEST_RESULT;
            IntUnaryOperator operator = unchecked(sqlOperator);

            assertEquals(TEST_RESULT, operator.applyAsInt(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            IntUnarySQLOperator sqlOperator = t -> {
                throw new SQLException("sqlOperator");
            };
            IntUnaryOperator operator = unchecked(sqlOperator);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> operator.applyAsInt(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlOperator", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(UnaryOperator<? super T, ? extends R>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() throws SQLException {
            IntUnaryOperator operator = t -> TEST_RESULT;
            IntUnarySQLOperator sqlOperator = checked(operator);

            assertEquals(TEST_RESULT, sqlOperator.applyAsInt(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            IntUnaryOperator operator = t -> {
                throw new UncheckedSQLException(e);
            };
            IntUnarySQLOperator sqlOperator = checked(operator);

            SQLException exception = assertThrows(SQLException.class, () -> sqlOperator.applyAsInt(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            IntUnaryOperator operator = t -> {
                throw e;
            };
            IntUnarySQLOperator sqlOperator = checked(operator);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlOperator.applyAsInt(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
