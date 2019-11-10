/*
 * DoubleBinarySQLOperatorTest.java
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

import static com.github.robtimus.sql.function.DoubleBinarySQLOperator.checked;
import static com.github.robtimus.sql.function.DoubleBinarySQLOperator.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.DoubleBinaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class DoubleBinarySQLOperatorTest {

    private static final double TEST_VALUE1 = Math.PI;
    private static final double TEST_VALUE2 = Math.E;
    private static final double TEST_RESULT = Double.MIN_NORMAL;

    @Nested
    @DisplayName("unchecked(DoubleBinarySQLOperator)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            DoubleBinarySQLOperator sqlOperator = (t, u) -> TEST_RESULT;
            DoubleBinaryOperator operator = unchecked(sqlOperator);

            assertEquals(TEST_RESULT, operator.applyAsDouble(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            DoubleBinarySQLOperator sqlOperator = (t, u) -> {
                throw new SQLException("sqlOperator");
            };
            DoubleBinaryOperator operator = unchecked(sqlOperator);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> operator.applyAsDouble(TEST_VALUE1, TEST_VALUE2));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlOperator", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(DoubleBinaryOperator<T>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() throws SQLException {
            DoubleBinaryOperator operator = (t, u) -> TEST_RESULT;
            DoubleBinarySQLOperator sqlOperator = checked(operator);

            assertEquals(TEST_RESULT, sqlOperator.applyAsDouble(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            DoubleBinaryOperator operator = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            DoubleBinarySQLOperator sqlOperator = checked(operator);

            SQLException exception = assertThrows(SQLException.class, () -> sqlOperator.applyAsDouble(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            DoubleBinaryOperator operator = (t, u) -> {
                throw e;
            };
            DoubleBinarySQLOperator sqlOperator = checked(operator);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlOperator.applyAsDouble(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
