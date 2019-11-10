/*
 * ToIntSQLBiFunctionTest.java
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

import static com.github.robtimus.sql.function.ToIntSQLBiFunction.checked;
import static com.github.robtimus.sql.function.ToIntSQLBiFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.ToIntBiFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class ToIntSQLBiFunctionTest {

    private static final String TEST_VALUE1 = "foo";
    private static final Integer TEST_VALUE2 = 13;
    private static final int TEST_RESULT = TEST_VALUE1.length();

    @Nested
    @DisplayName("unchecked(ToIntSQLBiFunction<? super T, ? super U, ? extends R>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("applies")
        public void testApplies() {
            ToIntSQLBiFunction<String, Integer> sqlFunction = (t, u) -> TEST_RESULT;
            ToIntBiFunction<String, Integer> function = unchecked(sqlFunction);

            assertEquals(TEST_RESULT, function.applyAsInt(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            ToIntSQLBiFunction<String, Integer> sqlFunction = (t, u) -> {
                throw new SQLException("sqlFunction");
            };
            ToIntBiFunction<String, Integer> function = unchecked(sqlFunction);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> function.applyAsInt(TEST_VALUE1, TEST_VALUE2));
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
            ToIntBiFunction<String, Integer> function = (t, u) -> TEST_RESULT;
            ToIntSQLBiFunction<String, Integer> sqlFunction = checked(function);

            assertEquals(TEST_RESULT, sqlFunction.applyAsInt(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            ToIntBiFunction<String, Integer> function = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            ToIntSQLBiFunction<String, Integer> sqlFunction = checked(function);

            SQLException exception = assertThrows(SQLException.class, () -> sqlFunction.applyAsInt(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            ToIntBiFunction<String, Integer> function = (t, u) -> {
                throw e;
            };
            ToIntSQLBiFunction<String, Integer> sqlFunction = checked(function);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlFunction.applyAsInt(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
