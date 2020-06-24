/*
 * IntToLongSQLFunctionTest.java
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

import static com.github.robtimus.sql.function.IntToLongSQLFunction.checked;
import static com.github.robtimus.sql.function.IntToLongSQLFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.IntToLongFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class IntToLongSQLFunctionTest {

    private static final int TEST_VALUE = 13;
    private static final long TEST_RESULT = System.currentTimeMillis();

    @Nested
    @DisplayName("unchecked(IntToLongSQLFunction)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() {
            IntToLongSQLFunction sqlFunction = t -> TEST_RESULT;
            IntToLongFunction function = unchecked(sqlFunction);

            assertEquals(TEST_RESULT, function.applyAsLong(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            IntToLongSQLFunction sqlFunction = t -> {
                throw new SQLException("sqlFunction");
            };
            IntToLongFunction function = unchecked(sqlFunction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> function.applyAsLong(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlFunction", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(IntToLongFunction<? super R, ? extends R>)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        void testApplies() throws SQLException {
            IntToLongFunction function = t -> TEST_RESULT;
            IntToLongSQLFunction sqlFunction = checked(function);

            assertEquals(TEST_RESULT, sqlFunction.applyAsLong(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            IntToLongFunction function = t -> {
                throw new UncheckedSQLException(e);
            };
            IntToLongSQLFunction sqlFunction = checked(function);

            SQLException exception = assertThrows(SQLException.class, () -> sqlFunction.applyAsLong(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            IntToLongFunction function = t -> {
                throw e;
            };
            IntToLongSQLFunction sqlFunction = checked(function);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlFunction.applyAsLong(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
