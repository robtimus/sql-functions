/*
 * SQLBiFunctionTest.java
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

import static com.github.robtimus.sql.function.SQLBiFunction.checked;
import static com.github.robtimus.sql.function.SQLBiFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.BiFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class SQLBiFunctionTest {

    private static final String TEST_VALUE1 = "foo";
    private static final Integer TEST_VALUE2 = 13;
    private static final Integer TEST_RESULT = TEST_VALUE1.length();

    @Nested
    @DisplayName("andThen(SQLFunction<? super R, ? extends V>)")
    public class AndThen {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            SQLBiFunction<String, Integer, Integer> function = (t, u) -> TEST_RESULT;

            assertThrows(NullPointerException.class, () -> function.andThen(null));
        }

        @Test
        @DisplayName("applies and applies")
        public void testAppliesAndApplies() throws SQLException {
            SQLBiFunction<String, Integer, Integer> function = (t, u) -> TEST_RESULT;
            SQLFunction<Integer, String> after = t -> TEST_VALUE1;
            SQLBiFunction<String, Integer, String> combined = function.andThen(after);

            assertEquals(TEST_VALUE1, combined.apply(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("applies and throws")
        public void testAcceptsAndThrows() {
            SQLBiFunction<String, Integer, Integer> function = (t, u) -> TEST_RESULT;
            SQLFunction<Integer, String> after = t -> {
                throw new SQLException("after");
            };
            SQLBiFunction<String, Integer, String> combined = function.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_VALUE1, TEST_VALUE2));
            assertEquals("after", exception.getMessage());
        }

        @Test
        @DisplayName("throws and applies")
        public void testThrowsAndAccepts() {
            SQLBiFunction<String, Integer, Integer> function = (t, u) -> {
                throw new SQLException("function");
            };
            SQLFunction<Integer, String> after = t -> TEST_VALUE1;
            SQLBiFunction<String, Integer, String> combined = function.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_VALUE1, TEST_VALUE2));
            assertEquals("function", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        public void testThrowsAndThrows() {
            SQLBiFunction<String, Integer, Integer> function = (t, u) -> {
                throw new SQLException("function");
            };
            SQLFunction<Integer, String> after = t -> {
                throw new SQLException("after");
            };
            SQLBiFunction<String, Integer, String> combined = function.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.apply(TEST_VALUE1, TEST_VALUE2));
            assertEquals("function", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("unchecked(SQLBiFunction<? super T, ? super U, ? extends R>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            SQLBiFunction<String, Integer, Integer> sqlFunction = (t, u) -> TEST_RESULT;
            BiFunction<String, Integer, Integer> function = unchecked(sqlFunction);

            assertEquals(TEST_RESULT, function.apply(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            SQLBiFunction<String, Integer, Integer> sqlFunction = (t, u) -> {
                throw new SQLException("sqlFunction");
            };
            BiFunction<String, Integer, Integer> function = unchecked(sqlFunction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> function.apply(TEST_VALUE1, TEST_VALUE2));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlFunction", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(BiFunction<? super T, ? super U, ? extends R>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() throws SQLException {
            BiFunction<String, Integer, Integer> function = (t, u) -> TEST_RESULT;
            SQLBiFunction<String, Integer, Integer> sqlFunction = checked(function);

            assertEquals(TEST_RESULT, sqlFunction.apply(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            BiFunction<String, Integer, Integer> function = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            SQLBiFunction<String, Integer, Integer> sqlFunction = checked(function);

            SQLException exception = assertThrows(SQLException.class, () -> sqlFunction.apply(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            BiFunction<String, Integer, Integer> function = (t, u) -> {
                throw e;
            };
            SQLBiFunction<String, Integer, Integer> sqlFunction = checked(function);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlFunction.apply(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
