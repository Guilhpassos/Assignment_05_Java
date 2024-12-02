package com.example.ab005;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.sql.*;

public class HelloController {

    @FXML
    private TableView<PlayerGameData> reportTable; // Table to display data
    @FXML
    private TableColumn<PlayerGameData, Integer> playerIdColumn, gameIdColumn, scoreColumn; // Numeric columns
    @FXML
    private TableColumn<PlayerGameData, String> firstNameColumn, lastNameColumn, titleColumn, playingDateColumn; // String columns
    @FXML
    private Button insertButton, displayButton; // Buttons for actions
    @FXML
    private TextField playerIdField, firstNameField, lastNameField, addressField, postalCodeField, provinceField, phoneNumberField; // Player input fields
    @FXML
    private TextField gameIdField, titleField, scoreField, playingDateField; // Game and score input fields

    private Connection connection;

    // Connect to the database
    public void connectToDatabase() {
        try {
            // Explicitly load the Oracle JDBC driver (Optional for newer Java versions)
            Class.forName("oracle.jdbc.OracleDriver");

            String url = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
            String user = "COMP228_F24_soh_20";
            String password = "14042005";

            connection = DriverManager.getConnection(url, user, password);

            System.out.println("Database connection successful!");
        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC Driver not found.");
            e.printStackTrace();
            showAlert("Connection Error", "Oracle JDBC Driver not found.");
        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
            showAlert("Connection Error", "Unable to connect to the database.");
        }
    }

    @FXML
    public void initialize() {
        connectToDatabase();
        // Initialize column-cell mappings
        playerIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPlayerId()).asObject());
        firstNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
        gameIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getGameId()).asObject());
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        scoreColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getScore()).asObject());
        playingDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlayingDate()));
    }

    // Insert data into the database
    @FXML
    private void onInsertButtonClick() {
        if (connection == null) {
            showAlert("Error", "Database connection not established.");
            return;
        }

        try {
            // Insert into GPS_PLAYER table
            String playerQuery = "INSERT INTO GPS_PLAYER (PLAYER_ID, FIRST_NAME, LAST_NAME, ADDRESS, POSTAL_CODE, PROVINCE, PHONE_NUMBER) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement playerStmt = connection.prepareStatement(playerQuery);
            playerStmt.setInt(1, Integer.parseInt(playerIdField.getText()));
            playerStmt.setString(2, firstNameField.getText());
            playerStmt.setString(3, lastNameField.getText());
            playerStmt.setString(4, addressField.getText());
            playerStmt.setString(5, postalCodeField.getText());
            playerStmt.setString(6, provinceField.getText());
            playerStmt.setString(7, phoneNumberField.getText());
            playerStmt.executeUpdate();

            // Insert into GPS_GAME table
            String gameQuery = "INSERT INTO GPS_GAME (GAME_ID, GAME_TITLE) VALUES (?, ?)";
            PreparedStatement gameStmt = connection.prepareStatement(gameQuery);
            gameStmt.setInt(1, Integer.parseInt(gameIdField.getText()));
            gameStmt.setString(2, titleField.getText());
            gameStmt.executeUpdate();

            // Insert into GPS_PLAYER_AND_GAME table
            String playerGameQuery = "INSERT INTO GPS_PLAYER_AND_GAME (PLAYER_ID, GAME_ID, SCORE, PLAYING_DATE) " +
                    "VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
            PreparedStatement playerGameStmt = connection.prepareStatement(playerGameQuery);
            playerGameStmt.setInt(1, Integer.parseInt(playerIdField.getText()));
            playerGameStmt.setInt(2, Integer.parseInt(gameIdField.getText()));
            playerGameStmt.setInt(3, Integer.parseInt(scoreField.getText()));
            playerGameStmt.setString(4, playingDateField.getText());
            playerGameStmt.executeUpdate();

            showAlert("Success", "Data inserted successfully!");

            // Update the table after inserting
            onDisplayButtonClick();  // This will update the table with new data
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to insert data. Check console for details.");
        }
    }

    // Display data in the table
    @FXML
    private void onDisplayButtonClick() {
        try {
            String query = "SELECT p.PLAYER_ID, p.FIRST_NAME, p.LAST_NAME, g.GAME_ID, g.GAME_TITLE, pg.SCORE, pg.PLAYING_DATE " +
                    "FROM GPS_PLAYER_AND_GAME pg " +
                    "JOIN GPS_PLAYER p ON p.PLAYER_ID = pg.PLAYER_ID " +
                    "JOIN GPS_GAME g ON g.GAME_ID = pg.GAME_ID";

            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            ObservableList<PlayerGameData> data = FXCollections.observableArrayList();
            while (rs.next()) {
                data.add(new PlayerGameData(
                        rs.getInt("PLAYER_ID"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME"),
                        rs.getInt("GAME_ID"),
                        rs.getString("GAME_TITLE"),
                        rs.getInt("SCORE"),
                        rs.getString("PLAYING_DATE")
                ));
            }

            reportTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch data.");
        }
    }

    // Display an alert message
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

// Helper class for table data
class PlayerGameData {
    private final SimpleIntegerProperty playerId;
    private final SimpleStringProperty firstName;
    private final SimpleStringProperty lastName;
    private final SimpleIntegerProperty gameId;
    private final SimpleStringProperty title;
    private final SimpleIntegerProperty score;
    private final SimpleStringProperty playingDate;

    public PlayerGameData(int playerId, String firstName, String lastName, int gameId, String title, int score, String playingDate) {
        this.playerId = new SimpleIntegerProperty(playerId);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.gameId = new SimpleIntegerProperty(gameId);
        this.title = new SimpleStringProperty(title);
        this.score = new SimpleIntegerProperty(score);
        this.playingDate = new SimpleStringProperty(playingDate);
    }

    public int getPlayerId() { return playerId.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public int getGameId() { return gameId.get(); }
    public String getTitle() { return title.get(); }
    public int getScore() { return score.get(); }
    public String getPlayingDate() { return playingDate.get(); }
}
