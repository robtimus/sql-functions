/*
 * DoubleSQLConsumerTest.java
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

import static com.github.robtimus.sql.function.DoubleSQLConsumer.checked;
import static com.github.robtimus.sql.function.DoubleSQLConsumer.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class DoubleSQLConsumerTest {

    private static final double TEST_VALUE = Math.PI;

    @Nested
    @DisplayName("andThen(DoubleSQLConsumer)")
    class AndThen {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            DoubleSQLConsumer consumer = t -> { /* does nothing */ };

            assertThrows(NullPointerException.class, () -> consumer.andThen(null));
        }

        @Test
        @DisplayName("accepts and accepts")
        void testAcceptsAndAccepts() throws SQLException {
            List<Double> consumerList = new ArrayList<>();
            List<Double> afterList = new ArrayList<>();

            DoubleSQLConsumer consumer = consumerList::add;
            DoubleSQLConsumer after = afterList::add;
            DoubleSQLConsumer combined = consumer.andThen(after);

            combined.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), consumerList);
            assertEquals(Collections.singletonList(TEST_VALUE), afterList);
        }

        @Test
        @DisplayName("accepts and throws")
        void testAcceptsAndThrows() {
            List<Double> consumerList = new ArrayList<>();

            DoubleSQLConsumer consumer = consumerList::add;
            DoubleSQLConsumer after = t -> {
                throw new SQLException("after");
            };
            DoubleSQLConsumer combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("after", exception.getMessage());
            assertEquals(Collections.singletonList(TEST_VALUE), consumerList);
        }

        @Test
        @DisplayName("throws and accepts")
        void testThrowsAndAccepts() {
            List<Double> afterList = new ArrayList<>();

            DoubleSQLConsumer consumer = t -> {
                throw new SQLException("consumer");
            };
            DoubleSQLConsumer after = afterList::add;
            DoubleSQLConsumer combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("consumer", exception.getMessage());
            assertEquals(Collections.emptyList(), afterList);
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            DoubleSQLConsumer consumer = t -> {
                throw new SQLException("consumer");
            };
            DoubleSQLConsumer after = t -> {
                throw new SQLException("after");
            };
            DoubleSQLConsumer combined = consumer.andThen(after);

            SQLException exception = assertThrows(SQLException.class, () -> combined.accept(TEST_VALUE));
            assertEquals("consumer", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("unchecked(DoubleSQLConsumer)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("accepts")
        void testAccepts() {
            List<Double> list = new ArrayList<>();

            DoubleSQLConsumer sqlConsumer = list::add;
            DoubleConsumer consumer = unchecked(sqlConsumer);

            consumer.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), list);
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            DoubleSQLConsumer sqlConsumer = t -> {
                throw new SQLException("sqlConsumer");
            };
            DoubleConsumer consumer = unchecked(sqlConsumer);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> consumer.accept(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlConsumer", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(DoubleConsumer)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("accepts")
        void testAccepts() throws SQLException {
            List<Double> list = new ArrayList<>();

            DoubleConsumer consumer = list::add;
            DoubleSQLConsumer sqlConsumer = checked(consumer);

            sqlConsumer.accept(TEST_VALUE);
            assertEquals(Collections.singletonList(TEST_VALUE), list);
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            DoubleConsumer consumer = t -> {
                throw new UncheckedSQLException(e);
            };
            DoubleSQLConsumer sqlConsumer = checked(consumer);

            SQLException exception = assertThrows(SQLException.class, () -> sqlConsumer.accept(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            DoubleConsumer consumer = t -> {
                throw e;
            };
            DoubleSQLConsumer sqlConsumer = checked(consumer);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlConsumer.accept(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
