/*
 * UnarySQLOperatorTest.java
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

import static com.github.robtimus.sql.function.UnarySQLOperator.checked;
import static com.github.robtimus.sql.function.UnarySQLOperator.identity;
import static com.github.robtimus.sql.function.UnarySQLOperator.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class UnarySQLOperatorTest {

    private static final String TEST_VALUE = "foo";
    private static final String TEST_RESULT = "bar";

    @Nested
    @DisplayName("identity()")
    public class Identity {

        @Test
        @DisplayName("non-null value")
        public void testNonNull() throws SQLException {
            UnarySQLOperator<String> operator = identity();

            assertEquals(TEST_VALUE, operator.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("null value")
        public void testNull() throws SQLException {
            UnarySQLOperator<String> operator = identity();

            assertNull(operator.apply(null));
        }
    }

    @Nested
    @DisplayName("unchecked(UnarySQLOperator<T>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            UnarySQLOperator<String> sqlOperator = t -> TEST_RESULT;
            UnaryOperator<String> operator = unchecked(sqlOperator);

            assertEquals(TEST_RESULT, operator.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            UnarySQLOperator<String> sqlOperator = t -> {
                throw new SQLException("sqlOperator");
            };
            UnaryOperator<String> operator = unchecked(sqlOperator);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> operator.apply(TEST_VALUE));
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
            UnaryOperator<String> operator = t -> TEST_RESULT;
            UnarySQLOperator<String> sqlOperator = checked(operator);

            assertEquals(TEST_RESULT, sqlOperator.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            UnaryOperator<String> operator = t -> {
                throw new UncheckedSQLException(e);
            };
            UnarySQLOperator<String> sqlOperator = checked(operator);

            SQLException exception = assertThrows(SQLException.class, () -> sqlOperator.apply(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            UnaryOperator<String> operator = t -> {
                throw e;
            };
            UnarySQLOperator<String> sqlOperator = checked(operator);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlOperator.apply(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
