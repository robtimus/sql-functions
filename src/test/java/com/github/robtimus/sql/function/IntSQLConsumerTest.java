/*
 * IntSQLConsumerTest.java
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

import static com.github.robtimus.sql.function.IntSQLConsumer.checked;
import static com.github.robtimus.sql.function.IntSQLConsumer.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class IntSQLConsumerTest {

    private static final int TEST_VALUE = 13;

    @Nested
    @DisplayName("andThen(IntSQLConsumer)")
    class AndThen {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            IntSQLConsumer consumer = t -> { /* does nothing */ };

            assertThrows(NullPointerException.class, () -> consumer.andThen(null));
        }

        @Test
        @DisplayName("accepts and accepts")
        void testAcceptsAndAccepts() throws SQLException {
            List<Integer> consumerList = new ArrayList<>();
            List<Integer> afterList = new ArrayList<>();

            IntSQLConsumer consumer = consumerList::add;
            IntSQLConsumer after = afterList::add;
            IntSQLConsumer combined = consumer.andThen(after);

            combined.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), consumerList);
            assertEquals(Collections.singletonList(TEST_VALUE), afterList);
        }

        @Test
        @DisplayName("accepts and throws")
        void testAcceptsAndThrows() {
            List<Integer> consumerList = new ArrayList<>();

            IntSQLConsumer consumer = consumerList::add;
            IntSQLConsumer after = t -> {
                throw new SQLException("after");
            };
            IntSQLConsumer combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("after", exception.getMessage());
            assertEquals(Collections.singletonList(TEST_VALUE), consumerList);
        }

        @Test
        @DisplayName("throws and accepts")
        void testThrowsAndAccepts() {
            List<Integer> afterList = new ArrayList<>();

            IntSQLConsumer consumer = t -> {
                throw new SQLException("consumer");
            };
            IntSQLConsumer after = afterList::add;
            IntSQLConsumer combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("consumer", exception.getMessage());
            assertEquals(Collections.emptyList(), afterList);
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            IntSQLConsumer consumer = t -> {
                throw new SQLException("consumer");
            };
            IntSQLConsumer after = t -> {
                throw new SQLException("after");
            };
            IntSQLConsumer combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("consumer", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("unchecked(IntSQLConsumer)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("accepts")
        void testAccepts() {
            List<Integer> list = new ArrayList<>();

            IntSQLConsumer sqlConsumer = list::add;
            IntConsumer consumer = unchecked(sqlConsumer);

            consumer.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), list);
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            IntSQLConsumer sqlConsumer = t -> {
                throw new SQLException("sqlConsumer");
            };
            IntConsumer consumer = unchecked(sqlConsumer);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> consumer.accept(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlConsumer", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(IntConsumer)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("accepts")
        void testAccepts() throws SQLException {
            List<Integer> list = new ArrayList<>();

            IntConsumer consumer = list::add;
            IntSQLConsumer sqlConsumer = checked(consumer);

            sqlConsumer.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), list);
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            IntConsumer consumer = t -> {
                throw new UncheckedSQLException(e);
            };
            IntSQLConsumer sqlConsumer = checked(consumer);

            SQLException exception = assertThrows(SQLException.class, () -> sqlConsumer.accept(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            IntConsumer consumer = t -> {
                throw e;
            };
            IntSQLConsumer sqlConsumer = checked(consumer);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlConsumer.accept(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
