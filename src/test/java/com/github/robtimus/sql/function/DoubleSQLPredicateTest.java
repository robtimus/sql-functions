/*
 * DoubleSQLPredicateTest.java
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

import static com.github.robtimus.sql.function.DoubleSQLPredicate.checked;
import static com.github.robtimus.sql.function.DoubleSQLPredicate.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.SQLException;
import java.util.function.DoublePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class DoubleSQLPredicateTest {

    private static final double TEST_VALUE = Math.PI;

    @Nested
    @DisplayName("and(DoubleSQLPredicate)")
    class And {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            DoubleSQLPredicate predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.and(null));
        }

        @Test
        @DisplayName("true and true")
        void testTrueAndTrue() throws SQLException {
            DoubleSQLPredicate predicate = t -> true;
            DoubleSQLPredicate other = t -> true;
            DoubleSQLPredicate combined = predicate.and(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and false")
        void testTrueAndFalse() throws SQLException {
            DoubleSQLPredicate predicate = t -> true;
            DoubleSQLPredicate other = t -> false;
            DoubleSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and throws")
        void testTrueAndThrows() {
            DoubleSQLPredicate predicate = t -> true;
            DoubleSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            DoubleSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("false and true")
        void testFalseAndTrue() throws SQLException {
            DoubleSQLPredicate predicate = t -> false;
            DoubleSQLPredicate other = t -> true;
            DoubleSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false and false")
        void testFalseAndFalse() throws SQLException {
            DoubleSQLPredicate predicate = t -> false;
            DoubleSQLPredicate other = t -> false;
            DoubleSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false and throws")
        void testFalseAndThrows() throws SQLException {
            DoubleSQLPredicate predicate = t -> false;
            DoubleSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            DoubleSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws and true")
        void testThrowsAndTrue() {
            DoubleSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            DoubleSQLPredicate other = t -> true;
            DoubleSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and false")
        void testThrowsAndFalse() {
            DoubleSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            DoubleSQLPredicate other = t -> false;
            DoubleSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            DoubleSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            DoubleSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            DoubleSQLPredicate combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("negate()")
    class Negate {

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            DoubleSQLPredicate predicate = t -> true;
            DoubleSQLPredicate negated = predicate.negate();

            assertFalse(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            DoubleSQLPredicate predicate = t -> false;
            DoubleSQLPredicate negated = predicate.negate();

            assertTrue(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            DoubleSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            DoubleSQLPredicate negated = predicate.negate();

            SQLException exception = assertThrows(SQLException.class, () -> negated.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("or(DoubleSQLPredicate)")
    class Or {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            DoubleSQLPredicate predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.or(null));
        }

        @Test
        @DisplayName("true or true")
        void testTrueOrTrue() throws SQLException {
            DoubleSQLPredicate predicate = t -> true;
            DoubleSQLPredicate other = t -> true;
            DoubleSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true or false")
        void testTrueOrFalse() throws SQLException {
            DoubleSQLPredicate predicate = t -> true;
            DoubleSQLPredicate other = t -> false;
            DoubleSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true or throws")
        void testTrueOrThrows() throws SQLException {
            DoubleSQLPredicate predicate = t -> true;
            DoubleSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            DoubleSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or true")
        void testFalseOrTrue() throws SQLException {
            DoubleSQLPredicate predicate = t -> false;
            DoubleSQLPredicate other = t -> true;
            DoubleSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or false")
        void testFalseOrFalse() throws SQLException {
            DoubleSQLPredicate predicate = t -> false;
            DoubleSQLPredicate other = t -> false;
            DoubleSQLPredicate combined = predicate.or(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or throws")
        void testFalseOrThrows() {
            DoubleSQLPredicate predicate = t -> false;
            DoubleSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            DoubleSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("throws or true")
        void testThrowsOrTrue() {
            DoubleSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            DoubleSQLPredicate other = t -> true;
            DoubleSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or false")
        void testThrowsOrFalse() {
            DoubleSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            DoubleSQLPredicate other = t -> false;
            DoubleSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or throws")
        void testThrowsOrThrows() {
            DoubleSQLPredicate predicate = t -> {
                throw new SQLException("predicate");
            };
            DoubleSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            DoubleSQLPredicate combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("unchecked(DoubleSQLPredicate)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() {
            DoubleSQLPredicate sqlPredicate = t -> true;
            DoublePredicate predicate = unchecked(sqlPredicate);

            assertTrue(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() {
            DoubleSQLPredicate sqlPredicate = t -> false;
            DoublePredicate predicate = unchecked(sqlPredicate);

            assertFalse(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            DoubleSQLPredicate sqlPredicate = t -> {
                throw new SQLException("sqlPredicate");
            };
            DoublePredicate predicate = unchecked(sqlPredicate);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> predicate.test(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlPredicate", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(DoublePredicate)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            DoublePredicate predicate = t -> true;
            DoubleSQLPredicate sqlPredicate = checked(predicate);

            assertTrue(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            DoublePredicate predicate = t -> false;
            DoubleSQLPredicate sqlPredicate = checked(predicate);

            assertFalse(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            DoublePredicate predicate = t -> {
                throw new UncheckedSQLException(e);
            };
            DoubleSQLPredicate sqlPredicate = checked(predicate);

            SQLException exception = assertThrows(SQLException.class, () -> sqlPredicate.test(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            DoublePredicate predicate = t -> {
                throw e;
            };
            DoubleSQLPredicate sqlPredicate = checked(predicate);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlPredicate.test(TEST_VALUE));
            assertSame(e, exception);
        }
    }
}
