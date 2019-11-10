/*
 * SQLConsumerTest.java
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

import static com.github.robtimus.sql.function.SQLConsumer.checked;
import static com.github.robtimus.sql.function.SQLConsumer.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class SQLConsumerTest {

    private static final String TEST_VALUE = "foo";

    @Nested
    @DisplayName("andThen(SQLConsumer<? super T>)")
    public class AndThen {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            SQLConsumer<String> consumer = t -> { /* does nothing */ };

            assertThrows(NullPointerException.class, () -> consumer.andThen(null));
        }

        @Test
        @DisplayName("accepts and accepts")
        public void testAcceptsAndAccepts() throws SQLException {
            List<String> consumerList = new ArrayList<>();
            List<String> afterList = new ArrayList<>();

            SQLConsumer<String> consumer = consumerList::add;
            SQLConsumer<String> after = afterList::add;
            SQLConsumer<String> combined = consumer.andThen(after);

            combined.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), consumerList);
            assertEquals(Collections.singletonList(TEST_VALUE), afterList);
        }

        @Test
        @DisplayName("accepts and throws")
        public void testAcceptsAndThrows() {
            List<String> consumerList = new ArrayList<>();

            SQLConsumer<String> consumer = consumerList::add;
            SQLConsumer<String> after = t -> {
                throw new SQLException("after");
            };
            SQLConsumer<String> combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("after", exception.getMessage());
            assertEquals(Collections.singletonList(TEST_VALUE), consumerList);
        }

        @Test
        @DisplayName("throws and accepts")
        public void testThrowsAndAccepts() {
            List<String> afterList = new ArrayList<>();

            SQLConsumer<String> consumer = t -> {
                throw new SQLException("consumer");
            };
            SQLConsumer<String> after = afterList::add;
            SQLConsumer<String> combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("consumer", exception.getMessage());
            assertEquals(Collections.emptyList(), afterList);
        }

        @Test
        @DisplayName("throws and throws")
        public void testThrowsAndThrows() {
            SQLConsumer<String> consumer = t -> {
                throw new SQLException("consumer");
            };
            SQLConsumer<String> after = t -> {
                throw new SQLException("after");
            };
            SQLConsumer<String> combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("consumer", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("unchecked(SQLConsumer<? super T>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("accepts")
        public void testAccepts() {
            List<String> list = new ArrayList<>();

            SQLConsumer<String> sqlConsumer = list::add;
            Consumer<String> consumer = unchecked(sqlConsumer);

            consumer.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), list);
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            SQLConsumer<String> sqlConsumer = t -> {
                throw new SQLException("sqlConsumer");
            };
            Consumer<String> consumer = unchecked(sqlConsumer);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> consumer.accept(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlConsumer", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(Consumer<? super T>)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("accepts")
        public void testAccepts() throws SQLException {
            List<String> list = new ArrayList<>();

            Consumer<String> consumer = list::add;
            SQLConsumer<String> sqlConsumer = checked(consumer);

            sqlConsumer.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), list);
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            Consumer<String> consumer = t -> {
                throw new UncheckedSQLException(e);
            };
            SQLConsumer<String> sqlConsumer = checked(consumer);

            SQLException exception = assertThrows(SQLException.class, () -> sqlConsumer.accept(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            Consumer<String> consumer = t -> {
                throw e;
            };
            SQLConsumer<String> sqlConsumer = checked(consumer);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlConsumer.accept(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
