/*
 * DoubleSQLSupplierTest.java
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

import static com.github.robtimus.sql.function.DoubleSQLSupplier.checked;
import static com.github.robtimus.sql.function.DoubleSQLSupplier.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class DoubleSQLSupplierTest {

    private static final double TEST_VALUE = Math.PI;

    @Nested
    @DisplayName("unchecked(DoubleSQLSupplier)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("supplies")
        public void testSupplies() {
            DoubleSQLSupplier sqlSupplier = () -> TEST_VALUE;
            DoubleSupplier supplier = unchecked(sqlSupplier);

            assertEquals(TEST_VALUE, supplier.getAsDouble());
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            DoubleSQLSupplier sqlSupplier = () -> {
                throw new SQLException("sqlSupplier");
            };
            DoubleSupplier supplier = unchecked(sqlSupplier);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, supplier::getAsDouble);
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlSupplier", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(DoubleSupplier)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("supplies")
        public void testSupplies() throws SQLException {
            DoubleSupplier supplier = () -> TEST_VALUE;
            DoubleSQLSupplier sqlSupplier = checked(supplier);

            assertEquals(TEST_VALUE, sqlSupplier.getAsDouble());
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            DoubleSupplier supplier = () -> {
                throw new UncheckedSQLException(e);
            };
            DoubleSQLSupplier sqlSupplier = checked(supplier);

            SQLException exception = assertThrows(SQLException.class, sqlSupplier::getAsDouble);
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            DoubleSupplier supplier = () -> {
                throw e;
            };
            DoubleSQLSupplier sqlSupplier = checked(supplier);

            IllegalStateException exception = assertThrows(IllegalStateException.class, sqlSupplier::getAsDouble);
            assertSame(e, exception);
        }
    }
}
