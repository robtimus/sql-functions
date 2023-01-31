/*
 * IntSQLPredicateTest.java
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

import static com.github.robtimus.sql.function.IntSQLPredicate.checked;
import static com.github.robtimus.sql.function.IntSQLPredicate.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.SQLException;
import java.util.function.IntPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class IntSQLPredicateTest {

    private static final int TEST_VALUE = 13;

    @Nested
    @DisplayName("and(IntSQLPredicate)")
    class And {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            IntSQLPredicate predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.and(null));
        }

        @Test
        @DisplayName("true and true")
        void testTrueAndTrue() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate other = t -> true;
            IntSQLPredicate combined = predicate.and(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and false")
        void testTrueAndFalse() throws SQLException {
            testFalseResult(true, false);
        }

        @Test
        @DisplayName("true and throws")
        void testTrueAndThrows() {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            IntSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("false and true")
        void testFalseAndTrue() throws SQLException {
            testFalseResult(false, true);
        }

        @Test
        @DisplayName("false and false")
        void testFalseAndFalse() throws SQLException {
            testFalseResult(false, false);
        }

        @Test
        @DisplayName("false and throws")
        void testFalseAndThrows() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            IntSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws and true")
        void testThrowsAndTrue() {
            IntSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            IntSQLPredicate other = t -> true;
            IntSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and false")
        void testThrowsAndFalse() {
            IntSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            IntSQLPredicate other = t -> false;
            IntSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            IntSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            IntSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            IntSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        private void testFalseResult(boolean firstResult, boolean secondResult) throws SQLException {
            IntSQLPredicate predicate = t -> firstResult;
            IntSQLPredicate other = t -> secondResult;
            IntSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }
    }

    @Nested
    @DisplayName("negate()")
    class Negate {

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate negated = predicate.negate();

            assertFalse(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate negated = predicate.negate();

            assertTrue(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            IntSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            IntSQLPredicate negated = predicate.negate();

            SQLException exception = assertThrows(SQLException.class, () -> negated.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("or(IntSQLPredicate)")
    class Or {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            IntSQLPredicate predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.or(null));
        }

        @Test
        @DisplayName("true or true")
        void testTrueOrTrue() throws SQLException {
            testTrueResult(true, true);
        }

        @Test
        @DisplayName("true or false")
        void testTrueOrFalse() throws SQLException {
            testTrueResult(true, false);
        }

        @Test
        @DisplayName("true or throws")
        void testTrueOrThrows() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            IntSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or true")
        void testFalseOrTrue() throws SQLException {
            testTrueResult(false, true);
        }

        @Test
        @DisplayName("false or false")
        void testFalseOrFalse() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate other = t -> false;
            IntSQLPredicate combined = predicate.or(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or throws")
        void testFalseOrThrows() {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            IntSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("throws or true")
        void testThrowsOrTrue() {
            IntSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            IntSQLPredicate other = t -> true;
            IntSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or false")
        void testThrowsOrFalse() {
            IntSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            IntSQLPredicate other = t -> false;
            IntSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or throws")
        void testThrowsOrThrows() {
            IntSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            IntSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            IntSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        private void testTrueResult(boolean firstResult, boolean secondResult) throws SQLException {
            IntSQLPredicate predicate = t -> firstResult;
            IntSQLPredicate other = t -> secondResult;
            IntSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }
    }

    @Nested
    @DisplayName("unchecked(IntSQLPredicate)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() {
            IntSQLPredicate sqlPredicate = t -> true;
            IntPredicate predicate = unchecked(sqlPredicate);

            assertTrue(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() {
            IntSQLPredicate sqlPredicate = t -> false;
            IntPredicate predicate = unchecked(sqlPredicate);

            assertFalse(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            IntSQLPredicate sqlPredicate = t -> {
                throw new SQLException("sqlPredicate");
            };
            IntPredicate predicate = unchecked(sqlPredicate);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> predicate.test(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlPredicate", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(IntPredicate)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            IntPredicate predicate = t -> true;
            IntSQLPredicate sqlPredicate = checked(predicate);

            assertTrue(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            IntPredicate predicate = t -> false;
            IntSQLPredicate sqlPredicate = checked(predicate);

            assertFalse(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            IntPredicate predicate = t -> {
                throw new UncheckedSQLException(e);
            };
            IntSQLPredicate sqlPredicate = checked(predicate);

            SQLException exception = assertThrows(SQLException.class, () -> sqlPredicate.test(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            IntPredicate predicate = t -> {
                throw e;
            };
            IntSQLPredicate sqlPredicate = checked(predicate);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlPredicate.test(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
