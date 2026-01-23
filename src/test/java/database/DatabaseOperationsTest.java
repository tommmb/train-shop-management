package database;

import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.*;

public class DatabaseOperationsTest {
    private static final String H2_URL = "jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
    private static final String H2_USER = "test";
    private static final String H2_PASSWORD = "test";
    private Connection connection;
    private DatabaseOperations databaseOperations;

    @BeforeEach
    void setUp() throws SQLException {
      connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
      databaseOperations = new DatabaseOperations();
      connection.createStatement().execute("RUNSCRIPT FROM 'classpath:schema-h2.sql'");
      connection.createStatement().execute("RUNSCRIPT FROM 'classpath:test-data.sql'");
    }

    @AfterEach
    void tearDown() throws SQLException {
      if (connection != null) {
        connection.createStatement().execute("DROP ALL OBJECTS");
        connection.close();
      }
    }

    @Test
    void getUserByEmail_existingEmail_returnsUser() throws SQLException {
      String testEmail = "john.doe@example.com";
      User user = databaseOperations.getUserByEmail(testEmail, connection);

      assertThat(user).isNotNull();
      assertThat(user.getClass()).isEqualTo(User.class);
      assertThat(user.getEmail()).isEqualTo(testEmail);
    }

    @Test
    void getUserByEmail_nonExistentEmail_returnsNull() throws SQLException {
      String testEmail = "non_existentexample.com";
      User user = databaseOperations.getUserByEmail(testEmail, connection);

      assertThat(user).isNull();
    }

    @Test
    void getUserByEmail_nullEmail_returnsNull() throws SQLException {
      User user = databaseOperations.getUserByEmail(null, connection);
      assertThat(user).isNull();
    }
}
