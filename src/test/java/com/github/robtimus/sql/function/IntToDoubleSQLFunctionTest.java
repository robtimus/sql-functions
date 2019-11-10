/*
 * IntToDoubleSQLFunctionTest.java
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

import static com.github.robtimus.sql.function.IntToDoubleSQLFunction.checked;
import static com.github.robtimus.sql.function.IntToDoubleSQLFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.IntToDoubleFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class IntToDoubleSQLFunctionTest {

    private static final int TEST_VALUE = 13;
    private static final double TEST_RESULT = Math.PI;

    @Nested
    @DisplayName("unchecked(IntToDoubleSQLFunction)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            IntToDoubleSQLFunction sqlFunction = t -> TEST_RESULT;
            IntToDoubleFunction function = unchecked(sqlFunction);

            assertEquals(TEST_RESULT, function.applyAsDouble(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            IntToDoubleSQLFunction sqlFunction = t -> {
                throw new SQLException("sqlFunction");
            };
            IntToDoubleFunction function = unchecked(sqlFunction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> function.applyAsDouble(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlFunction", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(IntToDoubleFunction<? super R, ? extends R>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() throws SQLException {
            IntToDoubleFunction function = t -> TEST_RESULT;
            IntToDoubleSQLFunction sqlFunction = checked(function);

            assertEquals(TEST_RESULT, sqlFunction.applyAsDouble(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            IntToDoubleFunction function = t -> {
                throw new UncheckedSQLException(e);
            };
            IntToDoubleSQLFunction sqlFunction = checked(function);

            SQLException exception = assertThrows(SQLException.class, () -> sqlFunction.applyAsDouble(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            IntToDoubleFunction function = t -> {
                throw e;
            };
            IntToDoubleSQLFunction sqlFunction = checked(function);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlFunction.applyAsDouble(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
