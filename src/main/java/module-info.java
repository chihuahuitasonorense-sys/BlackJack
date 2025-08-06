module blackjack {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;

    opens com.blackjack to javafx.fxml;
    opens com.blackjack.controllers to javafx.fxml;

    exports com.blackjack;
    exports com.blackjack.controllers;
    exports com.blackjack.models;
    exports com.blackjack.services;
    exports com.blackjack.database;
    exports com.blackjack.exceptions;
    exports com.blackjack.utils;
}
