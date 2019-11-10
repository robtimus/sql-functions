/*
 * UncheckedSQLExceptionTest.java
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "nls" })
public class UncheckedSQLExceptionTest {

    @Nested
    @DisplayName("serialization")
    public class Serialization {

        @Test
        @DisplayName("with message")
        public void testWithMessage() {
            SQLException cause = new SQLException("SQL exception");
            UncheckedSQLException exception = new UncheckedSQLException("Unchecked SQL exception", cause);

            UncheckedSQLException copy = serializeCopy(exception);
            assertEquals(exception.getMessage(), copy.getMessage());
            assertNotNull(copy.getCause());
            assertEquals(cause.getMessage(), copy.getCause().getMessage());
        }

        @Test
        @DisplayName("without message")
        public void testWithoutMessage() {
            SQLException cause = new SQLException("SQL exception");
            UncheckedSQLException exception = new UncheckedSQLException(cause);

            UncheckedSQLException copy = serializeCopy(exception);
            assertEquals(exception.getMessage(), copy.getMessage());
            assertNotNull(copy.getCause());
            assertEquals(cause.getMessage(), copy.getCause().getMessage());
        }

        private UncheckedSQLException serializeCopy(UncheckedSQLException exception) {
            return assertDoesNotThrow(() -> {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try (ObjectOutputStream objectOutput = new ObjectOutputStream(output)) {
                    objectOutput.writeObject(exception);
                }
                ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
                try (ObjectInputStream objectInput = new ObjectInputStream(input)) {
                    return (UncheckedSQLException) objectInput.readObject();
                }
            });
        }
    }
}
