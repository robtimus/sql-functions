/*
 * LongToIntSQLFunctionTest.java
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

import static com.github.robtimus.sql.function.LongToIntSQLFunction.checked;
import static com.github.robtimus.sql.function.LongToIntSQLFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.LongToIntFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class LongToIntSQLFunctionTest {

    private static final long TEST_VALUE = System.currentTimeMillis();
    private static final int TEST_RESULT = 13;

    @Nested
    @DisplayName("unchecked(LongToIntSQLFunction)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            LongToIntSQLFunction sqlFunction = t -> TEST_RESULT;
            LongToIntFunction function = unchecked(sqlFunction);

            assertEquals(TEST_RESULT, function.applyAsInt(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            LongToIntSQLFunction sqlFunction = t -> {
                throw new SQLException("sqlFunction");
            };
            LongToIntFunction function = unchecked(sqlFunction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> function.applyAsInt(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlFunction", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(LongToIntFunction<? super R, ? extends R>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() throws SQLException {
            LongToIntFunction function = t -> TEST_RESULT;
            LongToIntSQLFunction sqlFunction = checked(function);

            assertEquals(TEST_RESULT, sqlFunction.applyAsInt(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            LongToIntFunction function = t -> {
                throw new UncheckedSQLException(e);
            };
            LongToIntSQLFunction sqlFunction = checked(function);

            SQLException exception = assertThrows(SQLException.class, () -> sqlFunction.applyAsInt(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            LongToIntFunction function = t -> {
                throw e;
            };
            LongToIntSQLFunction sqlFunction = checked(function);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlFunction.applyAsInt(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
