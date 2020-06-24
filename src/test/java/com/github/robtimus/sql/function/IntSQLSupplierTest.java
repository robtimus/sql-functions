/*
 * IntSQLSupplierTest.java
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

import static com.github.robtimus.sql.function.IntSQLSupplier.checked;
import static com.github.robtimus.sql.function.IntSQLSupplier.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.IntSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class IntSQLSupplierTest {

    private static final int TEST_VALUE = 13;

    @Nested
    @DisplayName("unchecked(IntSQLSupplier)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("supplies")
        void testSupplies() {
            IntSQLSupplier sqlSupplier = () -> TEST_VALUE;
            IntSupplier supplier = unchecked(sqlSupplier);

            assertEquals(TEST_VALUE, supplier.getAsInt());
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            IntSQLSupplier sqlSupplier = () -> {
                throw new SQLException("sqlSupplier");
            };
            IntSupplier supplier = unchecked(sqlSupplier);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, supplier::getAsInt);
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlSupplier", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(IntSupplier)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("supplies")
        void testSupplies() throws SQLException {
            IntSupplier supplier = () -> TEST_VALUE;
            IntSQLSupplier sqlSupplier = checked(supplier);

            assertEquals(TEST_VALUE, sqlSupplier.getAsInt());
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            IntSupplier supplier = () -> {
                throw new UncheckedSQLException(e);
            };
            IntSQLSupplier sqlSupplier = checked(supplier);

            SQLException exception = assertThrows(SQLException.class, sqlSupplier::getAsInt);
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            IntSupplier supplier = () -> {
                throw e;
            };
            IntSQLSupplier sqlSupplier = checked(supplier);

            IllegalStateException exception = assertThrows(IllegalStateException.class, sqlSupplier::getAsInt);
            assertSame(e, exception);
        }
    }
}
