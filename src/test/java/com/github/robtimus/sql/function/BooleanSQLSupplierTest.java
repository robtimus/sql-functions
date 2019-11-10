/*
 * BooleanSQLSupplierTest.java
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

import static com.github.robtimus.sql.function.BooleanSQLSupplier.checked;
import static com.github.robtimus.sql.function.BooleanSQLSupplier.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class BooleanSQLSupplierTest {

    private static final boolean TEST_VALUE = true;

    @Nested
    @DisplayName("unchecked(BooleanSQLSupplier)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("supplies")
        public void testSupplies() {
            BooleanSQLSupplier sqlSupplier = () -> TEST_VALUE;
            BooleanSupplier supplier = unchecked(sqlSupplier);

            assertEquals(TEST_VALUE, supplier.getAsBoolean());
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            BooleanSQLSupplier sqlSupplier = () -> {
                throw new SQLException("sqlSupplier");
            };
            BooleanSupplier supplier = unchecked(sqlSupplier);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, supplier::getAsBoolean);
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlSupplier", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(BooleanSupplier)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("supplies")
        public void testSupplies() throws SQLException {
            BooleanSupplier supplier = () -> TEST_VALUE;
            BooleanSQLSupplier sqlSupplier = checked(supplier);

            assertEquals(TEST_VALUE, sqlSupplier.getAsBoolean());
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            BooleanSupplier supplier = () -> {
                throw new UncheckedSQLException(e);
            };
            BooleanSQLSupplier sqlSupplier = checked(supplier);

            SQLException exception = assertThrows(SQLException.class, sqlSupplier::getAsBoolean);
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            BooleanSupplier supplier = () -> {
                throw e;
            };
            BooleanSQLSupplier sqlSupplier = checked(supplier);

            IllegalStateException exception = assertThrows(IllegalStateException.class, sqlSupplier::getAsBoolean);
            assertSame(e, exception);
        }
    }
}
