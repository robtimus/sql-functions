/*
 * ToLongSQLBiFunctionTest.java
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

import static com.github.robtimus.sql.function.ToLongSQLBiFunction.checked;
import static com.github.robtimus.sql.function.ToLongSQLBiFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.ToLongBiFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class ToLongSQLBiFunctionTest {

    private static final String TEST_VALUE1 = "foo";
    private static final Integer TEST_VALUE2 = 13;
    private static final long TEST_RESULT = System.currentTimeMillis();

    @Nested
    @DisplayName("unchecked(ToLongSQLBiFunction<? super T, ? super U, ? extends R>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            ToLongSQLBiFunction<String, Integer> sqlFunction = (t, u) -> TEST_RESULT;
            ToLongBiFunction<String, Integer> function = unchecked(sqlFunction);

            assertEquals(TEST_RESULT, function.applyAsLong(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            ToLongSQLBiFunction<String, Integer> sqlFunction = (t, u) -> {
                throw new SQLException("sqlFunction");
            };
            ToLongBiFunction<String, Integer> function = unchecked(sqlFunction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> function.applyAsLong(TEST_VALUE1, TEST_VALUE2));
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
            ToLongBiFunction<String, Integer> function = (t, u) -> TEST_RESULT;
            ToLongSQLBiFunction<String, Integer> sqlFunction = checked(function);

            assertEquals(TEST_RESULT, sqlFunction.applyAsLong(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            ToLongBiFunction<String, Integer> function = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            ToLongSQLBiFunction<String, Integer> sqlFunction = checked(function);

            SQLException exception = assertThrows(SQLException.class, () -> sqlFunction.applyAsLong(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            ToLongBiFunction<String, Integer> function = (t, u) -> {
                throw e;
            };
            ToLongSQLBiFunction<String, Integer> sqlFunction = checked(function);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlFunction.applyAsLong(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
