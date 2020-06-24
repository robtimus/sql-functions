/*
 * SQLBiPredicateTest.java
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

import static com.github.robtimus.sql.function.SQLBiPredicate.checked;
import static com.github.robtimus.sql.function.SQLBiPredicate.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.SQLException;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class SQLBiPredicateTest {

    private static final String TEST_VALUE1 = "foo";
    private static final Integer TEST_VALUE2 = 13;

    @Nested
    @DisplayName("and(SQLBiPredicate<? super T, ? super U>)")
    class And {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;

            assertThrows(NullPointerException.class, () -> predicate.and(null));
        }

        @Test
        @DisplayName("true and true")
        void testTrueAndTrue() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;
            SQLBiPredicate<String, Integer> other = (t, u) -> true;
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            assertTrue(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("true and false")
        void testTrueAndFalse() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;
            SQLBiPredicate<String, Integer> other = (t, u) -> false;
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("true and throws")
        void testTrueAndThrows() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;
            SQLBiPredicate<String, Integer> other = (t, u) -> {
                throw new SQLException("other");
            };
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("false and true")
        void testFalseAndTrue() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> false;
            SQLBiPredicate<String, Integer> other = (t, u) -> true;
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("false and false")
        void testFalseAndFalse() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> false;
            SQLBiPredicate<String, Integer> other = (t, u) -> false;
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("false and throws")
        void testFalseAndThrows() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> false;
            SQLBiPredicate<String, Integer> other = (t, u) -> {
                throw new SQLException("other");
            };
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws and true")
        void testThrowsAndTrue() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> {
                throw new SQLException("predicate");
            };
            SQLBiPredicate<String, Integer> other = (t, u) -> true;
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and false")
        void testThrowsAndFalse() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> {
                throw new SQLException("predicate");
            };
            SQLBiPredicate<String, Integer> other = (t, u) -> false;
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws and throws")
        void testThrowsAndThrows() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> {
                throw new SQLException("predicate");
            };
            SQLBiPredicate<String, Integer> other = (t, u) -> {
                throw new SQLException("other");
            };
            SQLBiPredicate<String, Integer> combined = predicate.and(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("negate()")
    class Negate {

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;
            SQLBiPredicate<String, Integer> negated = predicate.negate();

            assertFalse(negated.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> false;
            SQLBiPredicate<String, Integer> negated = predicate.negate();

            assertTrue(negated.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> {
                throw new SQLException("predicate");
            };
            SQLBiPredicate<String, Integer> negated = predicate.negate();

            SQLException exception = assertThrows(SQLException.class, () -> negated.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("or(SQLBiPredicate<? super T, ? super U>)")
    class Or {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;

            assertThrows(NullPointerException.class, () -> predicate.or(null));
        }

        @Test
        @DisplayName("true or true")
        void testTrueOrTrue() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;
            SQLBiPredicate<String, Integer> other = (t, u) -> true;
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("true or false")
        void testTrueOrFalse() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;
            SQLBiPredicate<String, Integer> other = (t, u) -> false;
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("true or throws")
        void testTrueOrThrows() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> true;
            SQLBiPredicate<String, Integer> other = (t, u) -> {
                throw new SQLException("other");
            };
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("false or true")
        void testFalseOrTrue() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> false;
            SQLBiPredicate<String, Integer> other = (t, u) -> true;
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("false or false")
        void testFalseOrFalse() throws SQLException {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> false;
            SQLBiPredicate<String, Integer> other = (t, u) -> false;
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            assertFalse(combined.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("false or throws")
        void testFalseOrThrows() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> false;
            SQLBiPredicate<String, Integer> other = (t, u) -> {
                throw new SQLException("other");
            };
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("other", exception.getMessage());
        }

        @Test
        @DisplayName("throws or true")
        void testThrowsOrTrue() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> {
                throw new SQLException("predicate");
            };
            SQLBiPredicate<String, Integer> other = (t, u) -> true;
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or false")
        void testThrowsOrFalse() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> {
                throw new SQLException("predicate");
            };
            SQLBiPredicate<String, Integer> other = (t, u) -> false;
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("predicate", exception.getMessage());
        }

        @Test
        @DisplayName("throws or throws")
        void testThrowsOrThrows() {
            SQLBiPredicate<String, Integer> predicate = (t, u) -> {
                throw new SQLException("predicate");
            };
            SQLBiPredicate<String, Integer> other = (t, u) -> {
                throw new SQLException("other");
            };
            SQLBiPredicate<String, Integer> combined = predicate.or(other);

            SQLException exception = assertThrows(SQLException.class, () -> combined.test(TEST_VALUE1, TEST_VALUE2));
            assertEquals("predicate", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("unchecked(SQLBiPredicate<? super T, ? super U>)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() {
            SQLBiPredicate<String, Integer> sqlPredicate = (t, u) -> true;
            BiPredicate<String, Integer> predicate = unchecked(sqlPredicate);

            assertTrue(predicate.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("false")
        void testFalse() {
            SQLBiPredicate<String, Integer> sqlPredicate = (t, u) -> false;
            BiPredicate<String, Integer> predicate = unchecked(sqlPredicate);

            assertFalse(predicate.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            SQLBiPredicate<String, Integer> sqlPredicate = (t, u) -> {
                throw new SQLException("sqlPredicate");
            };
            BiPredicate<String, Integer> predicate = unchecked(sqlPredicate);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, () -> predicate.test(TEST_VALUE1, TEST_VALUE2));
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlPredicate", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(BiPredicate<? super T, ? super U>)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("true")
        void testTrue() throws SQLException {
            BiPredicate<String, Integer> predicate = (t, u) -> true;
            SQLBiPredicate<String, Integer> sqlPredicate = checked(predicate);

            assertTrue(sqlPredicate.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("false")
        void testFalse() throws SQLException {
            BiPredicate<String, Integer> predicate = (t, u) -> false;
            SQLBiPredicate<String, Integer> sqlPredicate = checked(predicate);

            assertFalse(sqlPredicate.test(TEST_VALUE1, TEST_VALUE2));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            BiPredicate<String, Integer> predicate = (t, u) -> {
                throw new UncheckedSQLException(e);
            };
            SQLBiPredicate<String, Integer> sqlPredicate = checked(predicate);

            SQLException exception = assertThrows(SQLException.class, () -> sqlPredicate.test(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            BiPredicate<String, Integer> predicate = (t, u) -> {
                throw e;
            };
            SQLBiPredicate<String, Integer> sqlPredicate = checked(predicate);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sqlPredicate.test(TEST_VALUE1, TEST_VALUE2));
            assertSame(e, exception);
        }
    }
}
