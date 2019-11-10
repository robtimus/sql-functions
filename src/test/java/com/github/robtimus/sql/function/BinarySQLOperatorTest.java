/*
 * BinarySQLOperatorTest.java
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

import static com.github.robtimus.sql.function.BinarySQLOperator.checked;
import static com.github.robtimus.sql.function.BinarySQLOperator.maxBy;
import static com.github.robtimus.sql.function.BinarySQLOperator.minBy;
import static com.github.robtimus.sql.function.BinarySQLOperator.unchecked;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.BinaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class BinarySQLOperatorTest {

    private static final String TEST_VALUE1 = "foo";
    private static final String TEST_VALUE2 = "bar";
    private static final String TEST_RESULT = TEST_VALUE1 + TEST_VALUE2;

    @Nested
    @DisplayName("minBy(Comparator<? super T>)")
    public class MinBy {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> minBy(null));
        }

        @Test
        @DisplayName("natural order")
        public void testNaturalOrder() throws SQLException {
            BinarySQLOperator<String> operator = minBy(naturalOrder());

            assertEquals(TEST_VALUE2, operator.apply(TEST_VALUE1, TEST_VALUE2));
            assertEquals(TEST_VALUE2, operator.apply(TEST_VALUE2, TEST_VALUE1));
        }

        @Test
        @DisplayName("reverse order")
        public void testReverseOrder() throws SQLException {
            BinarySQLOperator<String> operator = minBy(reverseOrder());

            assertEquals(TEST_VALUE1, operator.apply(TEST_VALUE1, TEST_VALUE2));
            assertEquals(TEST_VALUE1, operator.apply(TEST_VALUE2, TEST_VALUE1));
        }
    }

    @Nested
    @DisplayName("maxBy(Comparator<? super T>)")
    public class MaxBy {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> maxBy(null));
        }

        @Test
        @DisplayName("natural order")
        public void testNaturalOrder() throws SQLException {
            BinarySQLOperator<String> operator = maxBy(naturalOrder());

            assertEquals(TEST_VALUE1, operator.apply(TEST_VALUE1, TEST_VALUE2));
            assertEquals(TEST_VALUE1, operator.apply(TEST_VALUE2, TEST_VALUE1));
        }

        @Test
        @DisplayName("reverse order")
        public void testReverseOrder() throws SQLException {
            BinarySQLOperator<String> operator = maxBy(reverseOrder());

            assertEquals(TEST_VALUE2, operator.apply(TEST_VALUE1, TEST_VALUE2));
            assertEquals(TEST_VALUE2, operator.apply(TEST_VALUE2, TEST_VALUE1));
        }
    }

    @Nested
    @DisplayName("unchecked(BinarySQLOperator<T>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            BinarySQLOperator<String> sqlOperator = (t, u) -> TEST_RESULT;
            BinaryOperator<String> operator = unchecked(sqlOperator);

            assertEquals(TEST_RESULT, operator.apply(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            BinarySQLOperator<String> sqlOperator = (t, u) -> {
                throw new SQLException("sqlOperator");
            };
            BinaryOperator<String> operator = unchecked(sqlOperator);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> operator.apply(TEST_VALUE1, TEST_VALUE2));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlOperator", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(BinaryOperator<T>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() throws SQLException {
            BinaryOperator<String> operator = (t, u) -> TEST_RESULT;
            BinarySQLOperator<String> sqlOperator = checked(operator);

            assertEquals(TEST_RESULT, sqlOperator.apply(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            BinaryOperator<String> operator = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            BinarySQLOperator<String> sqlOperator = checked(operator);

            SQLException exception = assertThrows(SQLException.class, () -> sqlOperator.apply(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            BinaryOperator<String> operator = (t, u) -> {
                throw e;
            };
            BinarySQLOperator<String> sqlOperator = checked(operator);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlOperator.apply(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
