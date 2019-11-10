/*
 * ObjIntSQLConsumerTest.java
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

import static com.github.robtimus.sql.function.ObjIntSQLConsumer.checked;
import static com.github.robtimus.sql.function.ObjIntSQLConsumer.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ObjIntConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class ObjIntSQLConsumerTest {

    private static final String TEST_VALUE1 = "foo";
    private static final int TEST_VALUE2 = 13;

    @Nested
    @DisplayName("unchecked(ObjIntSQLConsumer<? super T>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("accepts")
        public void testAccepts() {
            Map<String, Integer> map = new HashMap<>();

            ObjIntSQLConsumer<String> sqlConsumer = map::put;
            ObjIntConsumer<String> consumer = unchecked(sqlConsumer);

            consumer.accept(TEST_VALUE1, TEST_VALUE2);
            assertEquals(Collections.singletonMap(TEST_VALUE1, TEST_VALUE2), map);
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            ObjIntSQLConsumer<String> sqlConsumer = (t, u) -> {
                throw new SQLException("sqlConsumer");
            };
            ObjIntConsumer<String> consumer = unchecked(sqlConsumer);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> consumer.accept(TEST_VALUE1, TEST_VALUE2));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlConsumer", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(ObjIntConsumer<? super T>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("accepts")
        public void testAccepts() throws SQLException {
            Map<String, Integer> map = new HashMap<>();

            ObjIntConsumer<String> consumer = map::put;
            ObjIntSQLConsumer<String> sqlConsumer = checked(consumer);

            sqlConsumer.accept(TEST_VALUE1, TEST_VALUE2);
            assertEquals(Collections.singletonMap(TEST_VALUE1, TEST_VALUE2), map);
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            ObjIntConsumer<String> consumer = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            ObjIntSQLConsumer<String> sqlConsumer = checked(consumer);

            SQLException exception = assertThrows(SQLException.class, () -> sqlConsumer.accept(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            ObjIntConsumer<String> consumer = (t, u) -> {
                throw e;
            };
            ObjIntSQLConsumer<String> sqlConsumer = checked(consumer);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlConsumer.accept(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
