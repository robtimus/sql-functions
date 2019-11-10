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

@SuppressWarnings({ "javadoc", "nls" })
public class SQLPredicateTest {

    private static final String TEST_VALUE = "foo";

    @Nested
    @DisplayName("and(SQLPredicate<? super T>)")
    public class And {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            SQLPredicate<String> predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.and(null));
        }

        @Test
        @DisplayName("true and true")
        public void testTrueAndTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> other = t -> true;
            SQLPredicate<String> combined = predicate.and(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and false")
        public void testTrueAndFalse() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> other = t -> false;
            SQLPredicate<String> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and throws")
        public void testTrueAndThrows() {
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
        public void testFalseAndTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> other = t -> true;
            SQLPredicate<String> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false and false")
        public void testFalseAndFalse() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> other = t -> false;
            SQLPredicate<String> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false and throws")
        public void testFalseAndThrows() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> other = t -> {
                throw new SQLException("other");
            };
            SQLPredicate<String> combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws and true")
        public void testThrowsAndTrue() {
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
        public void testThrowsAndFalse() {
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
        public void testThrowsAndThrows() {
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
    }

    @Nested
    @DisplayName("negate()")
    public class Negate {

        @Test
        @DisplayName("true")
        public void testTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> negated = predicate.negate();

            assertFalse(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> negated = predicate.negate();

            assertTrue(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
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
    public class Or {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            SQLPredicate<String> predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.or(null));
        }

        @Test
        @DisplayName("true or true")
        public void testTrueOrTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> other = t -> true;
            SQLPredicate<String> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true or false")
        public void testTrueOrFalse() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> other = t -> false;
            SQLPredicate<String> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true or throws")
        public void testTrueOrThrows() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> other = t -> {
                throw new SQLException("other");
            };
            SQLPredicate<String> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or true")
        public void testFalseOrTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> other = t -> true;
            SQLPredicate<String> combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or false")
        public void testFalseOrFalse() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> other = t -> false;
            SQLPredicate<String> combined = predicate.or(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or throws")
        public void testFalseOrThrows() {
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
        public void testThrowsOrTrue() {
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
        public void testThrowsOrFalse() {
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
        public void testThrowsOrThrows() {
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
    }

    @Nested
    @DisplayName("isEqual(Object)")
    public class IsEqual {

        @Test
        @DisplayName("non-null value")
        public void testNonNull() throws SQLException {
            SQLPredicate<String> predicate = isEqual("foo");

            assertTrue(predicate.test("foo"));
            assertFalse(predicate.test("bar"));
            assertFalse(predicate.test(null));
        }

        @Test
        @DisplayName("null value")
        public void testNull() throws SQLException {
            SQLPredicate<String> predicate = isEqual(null);

            assertFalse(predicate.test("foo"));
            assertFalse(predicate.test("bar"));
            assertTrue(predicate.test(null));
        }
    }

    @Nested
    @DisplayName("unchecked(SQLPredicate<? super T>)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("true")
        public void testTrue() {
            SQLPredicate<String> sqlPredicate = t -> true;
            Predicate<String> predicate = unchecked(sqlPredicate);

            assertTrue(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() {
            SQLPredicate<String> sqlPredicate = t -> false;
            Predicate<String> predicate = unchecked(sqlPredicate);

            assertFalse(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
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
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("true")
        public void testTrue() throws SQLException {
            Predicate<String> predicate = t -> true;
            SQLPredicate<String> sqlPredicate = checked(predicate);

            assertTrue(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() throws SQLException {
            Predicate<String> predicate = t -> false;
            SQLPredicate<String> sqlPredicate = checked(predicate);

            assertFalse(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
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
        public void testThrowsOtherException() {
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
    public class Not {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> not(null));
        }

        @Test
        @DisplayName("true")
        public void testTrue() throws SQLException {
            SQLPredicate<String> predicate = t -> true;
            SQLPredicate<String> negated = not(predicate);

            assertFalse(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() throws SQLException {
            SQLPredicate<String> predicate = t -> false;
            SQLPredicate<String> negated = not(predicate);

            assertTrue(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            SQLPredicate<String> predicate = t -> {
                throw new SQLException("predicate");
            };
            SQLPredicate<String> negated = not(predicate);

            SQLException exception = assertThrows(SQLException.class, () -> negated.test(TEST_VALUE));
            assertEquals("predicate", exception.getMessage());
        }
    }
}
