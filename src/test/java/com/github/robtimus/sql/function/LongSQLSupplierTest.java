/*
 * LongSQLSupplierTest.java
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

import static com.github.robtimus.sql.function.LongSQLSupplier.checked;
import static com.github.robtimus.sql.function.LongSQLSupplier.unchecked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class LongSQLSupplierTest {

    private static final long TEST_VALUE = System.currentTimeMillis();

    @Nested
    @DisplayName("unchecked(LongSQLSupplier)")
    public class Unchecked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> unchecked(null));
        }

        @Test
        @DisplayName("supplies")
        public void testSupplies() {
            LongSQLSupplier sqlSupplier = () -> TEST_VALUE;
            LongSupplier supplier = unchecked(sqlSupplier);

            assertEquals(TEST_VALUE, supplier.getAsLong());
        }

        @Test
        @DisplayName("throws")
        public void testThrows() {
            LongSQLSupplier sqlSupplier = () -> {
                throw new SQLException("sqlSupplier");
            };
            LongSupplier supplier = unchecked(sqlSupplier);

            UncheckedSQLException exception = assertThrows(UncheckedSQLException.class, supplier::getAsLong);
            SQLException cause = exception.getCause();
            assertNotNull(cause);
            assertEquals("sqlSupplier", cause.getMessage());
        }
    }

    @Nested
    @DisplayName("checked(LongSupplier)")
    public class Checked {

        @Test
        @DisplayName("null argument")
        public void testNullArgument() {
            assertThrows(NullPointerException.class, () -> checked(null));
        }

        @Test
        @DisplayName("supplies")
        public void testSupplies() throws SQLException {
            LongSupplier supplier = () -> TEST_VALUE;
            LongSQLSupplier sqlSupplier = checked(supplier);

            assertEquals(TEST_VALUE, sqlSupplier.getAsLong());
        }

        @Test
        @DisplayName("throws UncheckedSQLException")
        public void testThrowsUncheckedSQLException() {
            SQLException e = new SQLException("original");
            LongSupplier supplier = () -> {
                throw new UncheckedSQLException(e);
            };
            LongSQLSupplier sqlSupplier = checked(supplier);

            SQLException exception = assertThrows(SQLException.class, sqlSupplier::getAsLong);
            assertSame(e, exception);
        }

        @Test
        @DisplayName("throws other exception")
        public void testThrowsOtherException() {
            IllegalStateException e = new IllegalStateException("error");
            LongSupplier supplier = () -> {
                throw e;
            };
            LongSQLSupplier sqlSupplier = checked(supplier);

            IllegalStateException exception = assertThrows(IllegalStateException.class, sqlSupplier::getAsLong);
            assertSame(e, exception);
        }
    }
}
