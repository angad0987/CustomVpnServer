package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import encryption.EncryptionUtils;

public class clientServer {

    public static void main(String[] args) {
        int vpnServerPort = 9090;
        String vpnServerHost = "localhost";

        try {
            Socket socket = new Socket(vpnServerHost, vpnServerPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send message to destinationServer
            String message = "Hello! destination server I am angad";

            // first generate key
            // then encrypt key
            // then encrypte message
            // then send message and encrypted key to output stream
            try {
                SecretKey key = EncryptionUtils.generateSecretKey(128);
                String encryptedKey = EncryptionUtils.keyToString(key);
                System.out.println("Encrypted key : " + encryptedKey);
                String encryptedMessage = EncryptionUtils.encrypt(message, key);
                System.out.println("Encrypted message: " + encryptedMessage);

                String messageToBeSent = getJson("service2", encryptedMessage, encryptedKey);
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

            socket.close();
            // after receving respone from vpn serve closes the connection
            // to avoid resource leak

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getJson(String serviceType, String message, String key) {
        // Properly concatenate strings and fix typo
        String json = "{\"serviceType\":\"" + serviceType +
                "\",\"message\":\"" + message +
                "\",\"key\":\"" + key + "\"}";
        System.out.println(json);
        return json;

    }
}