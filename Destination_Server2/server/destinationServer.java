package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import Decryption_util.DecryptionUtils;

public class destinationServer {
    public static void main(String[] args) {
        int localport = 8182;

        Socket socket = null;
        try (ServerSocket serverSocket = new ServerSocket(localport)) {
            System.out.println("Destination Server is listening on port " + localport);

            socket = serverSocket.accept();

            System.out.println("Received connection from : " + socket.getRemoteSocketAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            // request from client server or
            // message from client server
            String requestData = in.readLine();
            String encryptedMessage = getEncryptedMessage(requestData);
            String encryptedKey = getEncryptedKey(requestData);
            SecretKey key = DecryptionUtils.stringToKey(encryptedKey);

            String originalMessage = null;
            try {
                originalMessage = DecryptionUtils.decrypt(encryptedMessage, key);
            } catch (InvalidKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (BadPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            System.out.println("Request Received from Client : " + originalMessage);

            out.println("Hi Angad ! Myself Destination Server");
            out.flush();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Failed to close socket: " + e.getMessage());
                }
            }
        }
    }

    private static String getEncryptedMessage(String json) {
        String key = "\"message\":";
        int index = json.indexOf(key);

        if (index == -1) {
            return null; // Key not found in JSON
        }

        int firstIndex = index + key.length();
        int i = firstIndex;

        while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') {
            i++;
        }

        return json.substring(firstIndex + 1, i - 1).trim();
    }

    private static String getEncryptedKey(String json) {
        String key = "\"key\":";
        int index = json.indexOf(key);

        if (index == -1) {
            return null; // Key not found in JSON
        }

        int firstIndex = index + key.length();
        int i = firstIndex;

        while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') {
            i++;
        }

        return json.substring(firstIndex + 1, i - 1).trim();
    }
}