/*
 * SQLSupplierTest.java
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

import static com.github.robtimus.sql.function.SQLSupplier.checked;
import static com.github.robtimus.sql.function.SQLSupplier.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
class SQLSupplierTest {

    private static final String TEST_VALUE = "foo";

    @Nested
    @DisplayName("unchecked(SQLSupplier<? extends T>)")
    class Unchecked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("supplies")
        void testSupplies() {
            SQLSupplier<String> sqlSupplier = () -> TEST_VALUE;
            Supplier<String> supplier = unchecked(sqlSupplier);

            assertEquals(TEST_VALUE, supplier.get());
        }

        @Test
        @DisplayName("throws")
        void testThrows() {
            SQLSupplier<String> sqlSupplier = () -> {
                throw new SQLException("sqlSupplier");
            };
            Supplier<String> supplier = unchecked(sqlSupplier);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, supplier::get);
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlSupplier", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(Supplier<? extends T>)")
    class Checked {

        @Test
        @DisplayName("null argument")
        void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("supplies")
        void testSupplies() throws SQLException {
            Supplier<String> supplier = () -> TEST_VALUE;
            SQLSupplier<String> sqlSupplier = checked(supplier);

            assertEquals(TEST_VALUE, sqlSupplier.get());
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            Supplier<String> supplier = () -> {
                throw new UncheckedSQLException(e);
            };
            SQLSupplier<String> sqlSupplier = checked(supplier);

            SQLException exception = assertThrows(SQLException.class, sqlSupplier::get);
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            Supplier<String> supplier = () -> {
                throw e;
            };
            SQLSupplier<String> sqlSupplier = checked(supplier);

            IllegalStateException exception = assertThrows(IllegalStateException.class, sqlSupplier::get);
            assertSame(e, exception);
        }
    }
}
