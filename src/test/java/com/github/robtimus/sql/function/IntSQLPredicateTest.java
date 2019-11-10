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

@SuppressWarnings({ "javadoc", "nls" })
public class IntSQLPredicateTest {

    private static final int TEST_VALUE = 13;

    @Nested
    @DisplayName("and(IntSQLPredicate)")
    public class And {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            IntSQLPredicate predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.and(null));
        }

        @Test
        @DisplayName("true and true")
        public void testTrueAndTrue() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate other = t -> true;
            IntSQLPredicate combined = predicate.and(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and false")
        public void testTrueAndFalse() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate other = t -> false;
            IntSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true and throws")
        public void testTrueAndThrows() {
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
        public void testFalseAndTrue() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate other = t -> true;
            IntSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false and false")
        public void testFalseAndFalse() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate other = t -> false;
            IntSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false and throws")
        public void testFalseAndThrows() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            IntSQLPredicate combined = predicate.and(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws and true")
        public void testThrowsAndTrue() {
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
        public void testThrowsAndFalse() {
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
        public void testThrowsAndThrows() {
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
    }

    @Nested
    @DisplayName("negate()")
    public class Negate {

        @Test
        @DisplayName("true")
        public void testTrue() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate negated = predicate.negate();

            assertFalse(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate negated = predicate.negate();

            assertTrue(negated.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
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
    public class Or {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            IntSQLPredicate predicate = t -> true;

            assertThrows(NullPointerException.class, () -> predicate.or(null));
        }

        @Test
        @DisplayName("true or true")
        public void testTrueOrTrue() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate other = t -> true;
            IntSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true or false")
        public void testTrueOrFalse() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate other = t -> false;
            IntSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("true or throws")
        public void testTrueOrThrows() throws SQLException {
            IntSQLPredicate predicate = t -> true;
            IntSQLPredicate other = t -> {
                throw new SQLException("other");
            };
            IntSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or true")
        public void testFalseOrTrue() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate other = t -> true;
            IntSQLPredicate combined = predicate.or(other);

            assertTrue(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or false")
        public void testFalseOrFalse() throws SQLException {
            IntSQLPredicate predicate = t -> false;
            IntSQLPredicate other = t -> false;
            IntSQLPredicate combined = predicate.or(other);

            assertFalse(combined.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false or throws")
        public void testFalseOrThrows() {
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
        public void testThrowsOrTrue() {
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
        public void testThrowsOrFalse() {
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
        public void testThrowsOrThrows() {
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
    }

    @Nested
    @DisplayName("unchecked(IntSQLPredicate)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("true")
        public void testTrue() {
            IntSQLPredicate sqlPredicate = t -> true;
            IntPredicate predicate = unchecked(sqlPredicate);

            assertTrue(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() {
            IntSQLPredicate sqlPredicate = t -> false;
            IntPredicate predicate = unchecked(sqlPredicate);

            assertFalse(predicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
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
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("true")
        public void testTrue() throws SQLException {
            IntPredicate predicate = t -> true;
            IntSQLPredicate sqlPredicate = checked(predicate);

            assertTrue(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("false")
        public void testFalse() throws SQLException {
            IntPredicate predicate = t -> false;
            IntSQLPredicate sqlPredicate = checked(predicate);

            assertFalse(sqlPredicate.test(TEST_VALUE));
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
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
        public void testThrowsOtherException() {
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
