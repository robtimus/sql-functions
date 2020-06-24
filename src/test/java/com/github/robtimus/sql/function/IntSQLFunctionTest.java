/*
 * IntSQLFunctionTest.java
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

import static com.github.robtimus.sql.function.IntSQLFunction.checked;
import static com.github.robtimus.sql.function.IntSQLFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.IntFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class IntSQLFunctionTest {

    private static final int TEST_VALUE = 13;
    private static final String TEST_RESULT = "foo";

    @Nested
    @DisplayName("unchecked(IntSQLFunction<? super R>)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() {
            IntSQLFunction<String> sqlFunction = t -> TEST_RESULT;
            IntFunction<String> function = unchecked(sqlFunction);

            assertEquals(TEST_RESULT, function.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            IntSQLFunction<String> sqlFunction = t -> {
                throw new SQLException("sqlFunction");
            };
            IntFunction<String> function = unchecked(sqlFunction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> function.apply(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlFunction", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(IntFunction<? super R, ? extends R>)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() throws SQLException {
            IntFunction<String> function = t -> TEST_RESULT;
            IntSQLFunction<String> sqlFunction = checked(function);

            assertEquals(TEST_RESULT, sqlFunction.apply(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            IntFunction<String> function = t -> {
                throw new UncheckedSQLException(e);
            };
            IntSQLFunction<String> sqlFunction = checked(function);

            SQLException exception = assertThrows(SQLException.class, () -> sqlFunction.apply(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            IntFunction<String> function = t -> {
                throw e;
            };
            IntSQLFunction<String> sqlFunction = checked(function);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlFunction.apply(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
