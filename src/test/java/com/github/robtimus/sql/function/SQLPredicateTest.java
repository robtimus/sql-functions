/*
 * SQLPredicateTest.java
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

import static com.github.robtimus.sql.function.SQLPredicate.checked;
import static com.github.robtimus.sql.function.SQLPredicate.isEqual;
import static com.github.robtimus.sql.function.SQLPredicate.not;
import static com.github.robtimus.sql.function.SQLPredicate.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.SQLException;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class SQLPredicateTest {

    private static final String TEST_VALUE = "foo";

    @Nested
    @DisplayName("and(SQLPredicate<? super T>)")
    class And {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            SQLPredicate<String> predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.and(null));
        }

        @Test
        @DisplayName("true and true")
        void testTrueAndTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> other = t -> true;
            SQLPredicate<String> combined = predicate.and(other);

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
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> other = t -> {
                throw new SQLException("other");
            };
            SQLPredicate<String> combined = predicate.and(other);

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
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> other = t -> {
                throw new SQLException("other");
            };
            SQLPredicate<String> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws and true")
        void testThrowsAndTrue() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> other = t -> true;
            SQLPredicate<String> combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and false")
        void testThrowsAndFalse() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> other = t -> false;
            SQLPredicate<String> combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> other = t -> {
                throw new SQLException("other");
            };
            SQLPredicate<String> combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        private void testFalseResult(boolean firstResult, boolean secondResult) throws SQLException {
            SQLPredicate<String> predicate = t -> firstResult;
            SQLPredicate<String> other = t -> secondResult;
            SQLPredicate<String> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }
    }

    @Nested
    @DisplayName("negate()")
    class Negate {

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> negated = predicate.negate();

            assertFalse(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> negated = predicate.negate();

            assertTrue(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> negated = predicate.negate();

            SQLException exception = assertThrows(SQLException.class, () -> negated.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("or(SQLPredicate<? super T>)")
    class Or {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            SQLPredicate<String> predicate = t -> true;

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
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> other = t -> {
                throw new SQLException("other");
            };
            SQLPredicate<String> combined = predicate.or(other);

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
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> other = t -> false;
            SQLPredicate<String> combined = predicate.or(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or throws")
        void testFalseOrThrows() {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> other = t -> {
                throw new SQLException("other");
            };
            SQLPredicate<String> combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("throws or true")
        void testThrowsOrTrue() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> other = t -> true;
            SQLPredicate<String> combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or false")
        void testThrowsOrFalse() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> other = t -> false;
            SQLPredicate<String> combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or throws")
        void testThrowsOrThrows() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> other = t -> {
                throw new SQLException("other");
            };
            SQLPredicate<String> combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }

        private void testTrueResult(boolean firstResult, boolean secondResult) throws SQLException {
            SQLPredicate<String> predicate = t -> firstResult;
            SQLPredicate<String> other = t -> secondResult;
            SQLPredicate<String> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }
    }

    @Nested
    @DisplayName("isEqual(Object)")
    class IsEqual {

        @Test
        @DisplayName("non-null value")
        void testNonNull() throws SQLException {
            SQLPredicate<String> predicate = isEqual("foo");

            assertTrue(predicate.test("foo"));
            assertFalse(predicate.test("bar"));
            assertFalse(predicate.test(null));
        }

        @Test
        @DisplayName("null value")
        void testNull() throws SQLException {
            SQLPredicate<String> predicate = isEqual(null);

            assertFalse(predicate.test("foo"));
            assertFalse(predicate.test("bar"));
            assertTrue(predicate.test(null));
        }
    }

    @Nested
    @DisplayName("unchecked(SQLPredicate<? super T>)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() {
            SQLPredicate<String> sqlPredicate = t -> true;
            Predicate<String> predicate = unchecked(sqlPredicate);

            assertTrue(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() {
            SQLPredicate<String> sqlPredicate = t -> false;
            Predicate<String> predicate = unchecked(sqlPredicate);

            assertFalse(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            SQLPredicate<String> sqlPredicate = t -> {
                throw new SQLException("sqlPredicate");
            };
            Predicate<String> predicate = unchecked(sqlPredicate);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> predicate.test(TEST_VALUE));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlPredicate", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(Predicate<? super T>)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            Predicate<String> predicate = t -> true;
            SQLPredicate<String> sqlPredicate = checked(predicate);

            assertTrue(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            Predicate<String> predicate = t -> false;
            SQLPredicate<String> sqlPredicate = checked(predicate);

            assertFalse(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            Predicate<String> predicate = t -> {
                throw new UncheckedSQLException(e);
            };
            SQLPredicate<String> sqlPredicate = checked(predicate);

            SQLException exception = assertThrows(SQLException.class, () -> sqlPredicate.test(TEST_VALUE));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            Predicate<String> predicate = t -> {
                throw e;
            };
            SQLPredicate<String> sqlPredicate = checked(predicate);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlPredicate.test(TEST_VALUE));
            assertSame(e, exception);
        }
    }

    @Nested
    @DisplayName("not(SQLPredicate<? super T>)")
    class Not {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> not(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> negated = not(predicate);

            assertFalse(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> negated = not(predicate);

            assertTrue(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> negated = not(predicate);

            SQLException exception = assertThrows(SQLException.class, () -> negated.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }
}
