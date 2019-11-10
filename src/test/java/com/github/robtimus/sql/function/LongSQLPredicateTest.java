/*
 * LongSQLPredicateTest.java
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

import static com.github.robtimus.sql.function.LongSQLPredicate.checked;
import static com.github.robtimus.sql.function.LongSQLPredicate.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.SQLException;
import java.util.function.LongPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class LongSQLPredicateTest {

    private static final long TEST_VALUE = System.currentTimeMillis();

    @Nested
    @DisplayName("and(LongSQLPredicate)")
    public class And {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            LongSQLPredicate predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.and(null));
        }

        @Test
        @DisplayName("true and true")
        public void testTrueAndTrue() throws SQLException {
            LongSQLPredicate predicate = t -> true;
            LongSQLPredicate other = t -> true;
            LongSQLPredicate combined = predicate.and(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and false")
        public void testTrueAndFalse() throws SQLException {
            LongSQLPredicate predicate = t -> true;
            LongSQLPredicate other = t -> false;
            LongSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and throws")
        public void testTrueAndThrows() {
            LongSQLPredicate predicate = t -> true;
            LongSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            LongSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("false and true")
        public void testFalseAndTrue() throws SQLException {
            LongSQLPredicate predicate = t -> false;
            LongSQLPredicate other = t -> true;
            LongSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false and false")
        public void testFalseAndFalse() throws SQLException {
            LongSQLPredicate predicate = t -> false;
            LongSQLPredicate other = t -> false;
            LongSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false and throws")
        public void testFalseAndThrows() throws SQLException {
            LongSQLPredicate predicate = t -> false;
            LongSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            LongSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws and true")
        public void testThrowsAndTrue() {
            LongSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            LongSQLPredicate other = t -> true;
            LongSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and false")
        public void testThrowsAndFalse() {
            LongSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            LongSQLPredicate other = t -> false;
            LongSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        public void testThrowsAndThrows() {
            LongSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            LongSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            LongSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("negate()")
    public class Negate {

        @Test
        @DisplayName("true")
        public void testTrue() throws SQLException {
            LongSQLPredicate predicate = t -> true;
            LongSQLPredicate negated = predicate.negate();

            assertFalse(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() throws SQLException {
            LongSQLPredicate predicate = t -> false;
            LongSQLPredicate negated = predicate.negate();

            assertTrue(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            LongSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            LongSQLPredicate negated = predicate.negate();

            SQLException exception = assertThrows(SQLException.class, () -> negated.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("or(LongSQLPredicate)")
    public class Or {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            LongSQLPredicate predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.or(null));
        }

        @Test
        @DisplayName("true or true")
        public void testTrueOrTrue() throws SQLException {
            LongSQLPredicate predicate = t -> true;
            LongSQLPredicate other = t -> true;
            LongSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true or false")
        public void testTrueOrFalse() throws SQLException {
            LongSQLPredicate predicate = t -> true;
            LongSQLPredicate other = t -> false;
            LongSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true or throws")
        public void testTrueOrThrows() throws SQLException {
            LongSQLPredicate predicate = t -> true;
            LongSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            LongSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or true")
        public void testFalseOrTrue() throws SQLException {
            LongSQLPredicate predicate = t -> false;
            LongSQLPredicate other = t -> true;
            LongSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or false")
        public void testFalseOrFalse() throws SQLException {
            LongSQLPredicate predicate = t -> false;
            LongSQLPredicate other = t -> false;
            LongSQLPredicate combined = predicate.or(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or throws")
        public void testFalseOrThrows() {
            LongSQLPredicate predicate = t -> false;
            LongSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            LongSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("throws or true")
        public void testThrowsOrTrue() {
            LongSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            LongSQLPredicate other = t -> true;
            LongSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or false")
        public void testThrowsOrFalse() {
            LongSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            LongSQLPredicate other = t -> false;
            LongSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or throws")
        public void testThrowsOrThrows() {
            LongSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            LongSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            LongSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("unchecked(LongSQLPredicate)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("true")
        public void testTrue() {
            LongSQLPredicate sqlPredicate = t -> true;
            LongPredicate predicate = unchecked(sqlPredicate);

            assertTrue(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() {
            LongSQLPredicate sqlPredicate = t -> false;
            LongPredicate predicate = unchecked(sqlPredicate);

            assertFalse(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            LongSQLPredicate sqlPredicate = t -> {
                throw new SQLException("sqlPredicate");
            };
            LongPredicate predicate = unchecked(sqlPredicate);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> predicate.test(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlPredicate", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(LongPredicate)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("true")
        public void testTrue() throws SQLException {
            LongPredicate predicate = t -> true;
            LongSQLPredicate sqlPredicate = checked(predicate);

            assertTrue(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() throws SQLException {
            LongPredicate predicate = t -> false;
            LongSQLPredicate sqlPredicate = checked(predicate);

            assertFalse(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            LongPredicate predicate = t -> {
                throw new UncheckedSQLException(e);
            };
            LongSQLPredicate sqlPredicate = checked(predicate);

            SQLException exception = assertThrows(SQLException.class, () -> sqlPredicate.test(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            LongPredicate predicate = t -> {
                throw e;
            };
            LongSQLPredicate sqlPredicate = checked(predicate);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlPredicate.test(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
