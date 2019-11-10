/*
 * SQLFunctionTest.java
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

import static com.github.robtimus.sql.function.SQLFunction.checked;
import static com.github.robtimus.sql.function.SQLFunction.identity;
import static com.github.robtimus.sql.function.SQLFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class SQLFunctionTest {

    private static final String TEST_VALUE = "foo";
    private static final Integer TEST_RESULT = TEST_VALUE.length();

    @Nested
    @DisplayName("compose(SQLFunction<? super T, ? extends R>)")
    public class Compose {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            SQLFunction<String, Integer> function = String::length;

            assertThrows(NullPointerException.class, () -> function.compose(null));
        }

        @Test
        @DisplayName("applies and applies")
        public void testAppliesAndApplies() throws SQLException {
            SQLFunction<String, Integer> function = t -> TEST_RESULT;
            SQLFunction<Integer, String> before = t -> TEST_VALUE;
            SQLFunction<Integer, Integer> combined = function.compose(before);

            assertEquals(TEST_RESULT, combined.apply(TEST_RESULT));
        }

        @Test
        @DisplayName("applies and throws")
        public void testAcceptsAndThrows() {
            SQLFunction<String, Integer> function = t -> TEST_RESULT;
            SQLFunction<Integer, String> before = t -> {
                throw new SQLException("before");
            };
            SQLFunction<Integer, Integer> combined = function.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_RESULT));
            assertEquals("before", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        public void testThrowsAndAccepts() {
            SQLFunction<String, Integer> function = t -> {
                throw new SQLException("function");
            };
            SQLFunction<Integer, String> before = t -> TEST_VALUE;
            SQLFunction<Integer, Integer> combined = function.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_RESULT));
            assertEquals("function", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        public void testThrowsAndThrows() {
            SQLFunction<String, Integer> function = t -> {
                throw new SQLException("function");
            };
            SQLFunction<Integer, String> before = t -> {
                throw new SQLException("before");
            };
            SQLFunction<Integer, Integer> combined = function.compose(before);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_RESULT));
            assertEquals("before", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("andThen(SQLFunction<? super T, ? extends R>)")
    public class AndThen {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            SQLFunction<String, Integer> function = String::length;

            assertThrows(NullPointerException.class, () -> function.andThen(null));
        }

        @Test
        @DisplayName("applies and applies")
        public void testAppliesAndApplies() throws SQLException {
            SQLFunction<String, Integer> function = t -> TEST_RESULT;
            SQLFunction<Integer, String> after = t -> TEST_VALUE;
            SQLFunction<String, String> combined = function.andThen(after);

            assertEquals(TEST_VALUE, combined.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("applies and throws")
        public void testAcceptsAndThrows() {
            SQLFunction<String, Integer> function = t -> TEST_RESULT;
            SQLFunction<Integer, String> after = t -> {
                throw new SQLException("after");
            };
            SQLFunction<String, String> combined = function.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_VALUE));
            assertEquals("after", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        public void testThrowsAndAccepts() {
            SQLFunction<String, Integer> function = t -> {
                throw new SQLException("function");
            };
            SQLFunction<Integer, String> after = t -> TEST_VALUE;
            SQLFunction<String, String> combined = function.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_VALUE));
            assertEquals("function", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        public void testThrowsAndThrows() {
            SQLFunction<String, Integer> function = t -> {
                throw new SQLException("function");
            };
            SQLFunction<Integer, String> after = t -> {
                throw new SQLException("after");
            };
            SQLFunction<String, String> combined = function.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_VALUE));
            assertEquals("function", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("identity()")
    public class Identity {

        @Test
        @DisplayName("non-null value")
        public void testNonNull() throws SQLException {
            SQLFunction<String, String> function = identity();

            assertEquals(TEST_VALUE, function.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("null value")
        public void testNull() throws SQLException {
            SQLFunction<String, String> function = identity();

            assertNull(function.apply(null));
        }
    }

    @Nested
    @DisplayName("unchecked(SQLFunction<? super T, ? extends R>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            SQLFunction<String, Integer> sqlFunction = String::length;
            Function<String, Integer> function = unchecked(sqlFunction);

            assertEquals(TEST_RESULT, function.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            SQLFunction<String, Integer> sqlFunction = t -> {
                throw new SQLException("sqlFunction");
            };
            Function<String, Integer> function = unchecked(sqlFunction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> function.apply(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlFunction", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(Function<? super T, ? extends R>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() throws SQLException {
            Function<String, Integer> function = String::length;
            SQLFunction<String, Integer> sqlFunction = checked(function);

            assertEquals(TEST_RESULT, sqlFunction.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            Function<String, Integer> function = t -> {
                throw new UncheckedSQLException(e);
            };
            SQLFunction<String, Integer> sqlFunction = checked(function);

            SQLException exception = assertThrows(SQLException.class, () -> sqlFunction.apply(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            Function<String, Integer> function = t -> {
                throw e;
            };
            SQLFunction<String, Integer> sqlFunction = checked(function);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlFunction.apply(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
