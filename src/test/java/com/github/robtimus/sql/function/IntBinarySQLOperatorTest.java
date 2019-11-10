/*
 * IntBinarySQLOperatorTest.java
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

import static com.github.robtimus.sql.function.IntBinarySQLOperator.checked;
import static com.github.robtimus.sql.function.IntBinarySQLOperator.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.IntBinaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class IntBinarySQLOperatorTest {

    private static final int TEST_VALUE1 = 13;
    private static final int TEST_VALUE2 = 481;
    private static final int TEST_RESULT = TEST_VALUE1 + TEST_VALUE2;

    @Nested
    @DisplayName("unchecked(IntBinarySQLOperator)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            IntBinarySQLOperator sqlOperator = (t, u) -> TEST_RESULT;
            IntBinaryOperator operator = unchecked(sqlOperator);

            assertEquals(TEST_RESULT, operator.applyAsInt(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            IntBinarySQLOperator sqlOperator = (t, u) -> {
                throw new SQLException("sqlOperator");
            };
            IntBinaryOperator operator = unchecked(sqlOperator);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> operator.applyAsInt(TEST_VALUE1, TEST_VALUE2));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlOperator", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(IntBinaryOperator<T>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() throws SQLException {
            IntBinaryOperator operator = (t, u) -> TEST_RESULT;
            IntBinarySQLOperator sqlOperator = checked(operator);

            assertEquals(TEST_RESULT, sqlOperator.applyAsInt(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            IntBinaryOperator operator = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            IntBinarySQLOperator sqlOperator = checked(operator);

            SQLException exception = assertThrows(SQLException.class, () -> sqlOperator.applyAsInt(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            IntBinaryOperator operator = (t, u) -> {
                throw e;
            };
            IntBinarySQLOperator sqlOperator = checked(operator);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlOperator.applyAsInt(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
