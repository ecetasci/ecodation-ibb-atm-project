package com.hamitmizrak.ibb_ecodation_javafx;

import com.hamitmizrak.ibb_ecodation_javafx.database.SingletonDBConnection;
import com.hamitmizrak.ibb_ecodation_javafx.database.SingletonPropertiesDBConnection;
import com.hamitmizrak.ibb_ecodation_javafx.utils.SpecialColor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class HelloApplication extends Application {

    // Proje Açıldığında İlk Açılacak Sayfa
    @Override
    public void start(Stage stage) throws IOException {
        // PROJE AYAĞA KALKALKEN DATABASE(H2DB) ÇALIŞSIN
        initializeDatabase();

        // Caused by: java.lang.IllegalStateException: Location is not set.
        // Yukarıdaki hatanın anlamı sayfayı bulamıyor.
        /*
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
         */

        // Başlangıçta Login Ekranı Gelsin
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/login.fxml"));
        Parent parent= fxmlLoader.load();
        stage.setTitle("Kullanıcı Yönetimi Login Sayfası");
        stage.setScene(new Scene(parent));
        stage.show();
    }

    /// //////////////////////////////////////////////////////////////////////////
    /// DATABASE
    // Proje ayağa kalkarken veritabanından örnek veriler eklesin
    // Database Başlangıçtaki değeri
    private void initializeDatabase() {
        try {
            Connection conn = SingletonDBConnection.getInstance().getConnection(); // STATIC BAĞLANTI ALINDI
            Statement stmt = conn.createStatement();

            String createTableSQL = """
                -- User login
                CREATE TABLE IF NOT EXISTS usertable (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    role VARCHAR(10) DEFAULT 'USER'
                );
                
                -- Fişler Ekle
                    CREATE TABLE receipts (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        receipt_number VARCHAR(50) NOT NULL UNIQUE,
                        receipt_date DATE NOT NULL,
                        tax_number VARCHAR(20) NOT NULL,
                        company_name VARCHAR(100) NOT NULL,
                        customer_name VARCHAR(100) NOT NULL,
                        description TEXT,
                        created_by VARCHAR(100) NOT NULL,
                        account_code VARCHAR(50) NOT NULL,
                     
                        -- ENUM yerine CHECK constraint ile değerleri sınırlıyoruz
                        receipt_type VARCHAR(20) NOT NULL CHECK (receipt_type IN ('Ödeme', 'Tahsilat', 'Masraf', 'Gelir')),
                        amount DECIMAL(10,2) NOT NULL,
                        vat_rate DECIMAL(5,2) NOT NULL,
                        total_amount DECIMAL(10,2) NOT NULL,
                    
                        -- ENUM yerine CHECK constraint
                        payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('Nakit', 'Kredi Kartı', 'Havale', 'Çek')),
                    
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );   
                """;
            stmt.execute(createTableSQL);

            // Şifreleri hashle
            String hashedPassword1 = BCrypt.hashpw("root", BCrypt.gensalt());
            String hashedPassword2 = BCrypt.hashpw("root", BCrypt.gensalt());

            // Örnek verileri hashlenmiş şekilde, rollerle birlikte ekle
            String insertSQL1 = String.format("""
            MERGE INTO usertable (username, password, email, role)
            KEY(username) VALUES ('hamitmizrak', '%s', 'hamitmizrak@gmail.com', 'USER');
        """, hashedPassword1);

                String insertSQL2 = String.format("""
            MERGE INTO usertable (username, password, email, role)
            KEY(username) VALUES ('admin', '%s', 'admin@gmail.com', 'ADMIN');
        """, hashedPassword2);

            stmt.execute(insertSQL1);
            stmt.execute(insertSQL2);

            System.out.println("✅ BCrypt ile şifrelenmiş ve roller atanmış kullanıcılar başarıyla eklendi.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /// //////////////////////////////////////////////////////////////////////////
    /// PSVM
    public static void main(String[] args) {
        launch();
    }
} //end HelloApplication