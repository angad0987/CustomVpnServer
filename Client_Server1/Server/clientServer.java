package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import javax.net.ssl.TrustManagerFactory;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileInputStream;
import encryption.EncryptionUtils;

public class clientServer {

    public static void main(String[] args) throws InterruptedException {
        int vpnServerPort = 9090;
        String vpnServerHost = "localhost";

        // Load the TrustStore to trust the server's certificate
        SSLSocketFactory sslSocketFactory = null;
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(
                    new FileInputStream(
                            "C:\\Users\\Angad\\Documents\\springbootProjects\\VPN Project\\client-truststore.jks"),
                    "password".toCharArray());

            // Set up TrustManagerFactory to trust the server's certificate
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Set up SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            // Create SSLSocketFactory
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            System.out.println("DOESNOT LOAD SERVER CERTIFICATES : " + e.getMessage());
            return;
        }

        try {
            // Create the SSLSocket and connect to the server

            // first generate key
            // then encrypt key
            // then encrypte message
            // then send message and encrypted key to output stream
            SSLSocket sslSocket = null;
            while (true) {
                sslSocket = (SSLSocket) sslSocketFactory.createSocket(vpnServerHost, vpnServerPort);
                System.out.println("Connected to VPN server at " + vpnServerHost + ":" + vpnServerPort);

                PrintWriter out = new PrintWriter(sslSocket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

                // send message to destinationServer
                String message = "Hello! destination server I am angad";
                try {
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("Enter your username : ");
                    String username = scanner.nextLine();
                    System.out.print("Enter your password : ");
                    String password = scanner.nextLine();

                    SecretKey key = EncryptionUtils.generateSecretKey(128);
                    String encryptedKey = EncryptionUtils.keyToString(key);
                    System.out.println("Encrypted key : " + encryptedKey);
                    String encryptedMessage = EncryptionUtils.encrypt(message, key);
                    System.out.println("Encrypted message: " + encryptedMessage);

                    String messageToBeSent = getJson("service1", encryptedMessage, encryptedKey, username, password);
                    out.println(messageToBeSent);
                    // we now create json object of this data

                    out.flush();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // receive response from destination server
                String response = in.readLine();
                System.out.println("Response from Vpn Server : " + response);
                // no we have to check is server responses with authenticated error
                // then make one more request other wise break the loop
                // and close the socket
                Thread.sleep(2000);

                if (isAuthenticationErrorMessage(response)) {
                    continue;
                } else {
                    break;
                }
            }

            sslSocket.close();
            // after receving respone from vpn serve closes the connection
            // to avoid resource leak

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAuthenticationErrorMessage(String response) {
        String[] arr = response.split(" ");
        boolean containsNot = Arrays.stream(arr).anyMatch(word -> word.equals("not"));
        return containsNot;
    }

    public static String getJson(String serviceType, String message, String key, String username, String password) {
        // Properly concatenate strings and fix typo
        String json = "{\"serviceType\":\"" + serviceType +
                "\",\"message\":\"" + message +
                "\",\"key\":\"" + key +
                "\",\"username\":\"" + username +
                "\",\"password\":\"" + password + "\"}";
        System.out.println(json);
        return json;

    }
}