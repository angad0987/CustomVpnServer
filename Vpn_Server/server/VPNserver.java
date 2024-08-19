package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.net.Socket;

import route.RoutingTable;

import java.io.PrintWriter;

public class VPNserver {
    private static RoutingTable routingTable = new RoutingTable();

    private static String jsonData = null;

    public static void main(String[] args) {

        // port or door no of this vpn server
        int localport = 9090;

        String destinationHost = "localhost";
        int destinationPort = 8181;
        try (ServerSocket serverSocket = new ServerSocket(localport)) {
            System.out.println("VPN server is listening on port : " + localport);
            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("Accepted connection from " + socket.getRemoteSocketAddress());

                // now find the route
                // first extract the service name like database,FTP,email server

                String route = getRoute(socket);
                String hostName = getHostName(route);
                int portNo = getPortNo(route);

                System.out.println("Forwarding request to destination server ...........");

                // request from client is handled by this thread
                new Thread(() -> handleClient(socket, hostName, portNo)).start();

            }
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }

    }

    private static String getHostName(String route) {
        int i = route.indexOf(':');
        String host = route.substring(0, i);
        return host;
    }

    private static Integer getPortNo(String route) {
        int i = route.indexOf(':');
        String portno = route.substring(i + 1, route.length());
        return Integer.parseInt(portno);
    }

    private static String getRoute(Socket socket) {
        BufferedReader inClient;
        try {
            inClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            jsonData = inClient.readLine();
            String getServiceName = getService(jsonData);
            String route = routingTable.getRoute(getServiceName);

            System.out.println("Route is : " + route);

            return route;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

    private static String getService(String json) {
        String key = "\"serviceType\":";
        int index = json.indexOf(key);

        int firstIndex = index + key.length();
        int i = firstIndex;

        int endIndex = 0;

        while (json.charAt(i) != ',') {
            i++;
            endIndex = i;
        }
        System.out.println(json.substring(firstIndex + 1, endIndex - 1));
        return json.substring(firstIndex + 1, endIndex - 1).trim();

    }

    private static void handleClient(Socket clientSocket, String destinationHost, int destinationPort) {
        try (Socket destinationSocket = new Socket(destinationHost, destinationPort)) {
            BufferedReader inClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // System.out.println("client data size" + in.);
            PrintWriter outClient = new PrintWriter(clientSocket.getOutputStream());

            BufferedReader inDest = new BufferedReader(new InputStreamReader(destinationSocket.getInputStream()));
            PrintWriter outDest = new PrintWriter(destinationSocket.getOutputStream());

            // Forward the buffered JSON data first
            if (jsonData != null) {
                outDest.println(jsonData);
                outDest.flush();
            }
            // forward data to destination server
            Thread clientToServerThread = new Thread(() -> forwardData(inClient, outDest));
            clientToServerThread.start();

            // forward data to client
            Thread serverToClientThread = new Thread(() -> forwardData(inDest, outClient));
            serverToClientThread.start();

            // Wait for threads to finish
            clientToServerThread.join();
            serverToClientThread.join();

        } catch (Exception ex) {

            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    // this forward data function is used to transfer data to destination server
    // and to client server
    private static void forwardData(BufferedReader in, PrintWriter out) {

        String line = null;
        try {
            while ((line = in.readLine()) != null) {

                System.out.println(line);
                out.println(line);
                out.flush();
            }

        } catch (java.net.SocketException ex) {
            // Handle the case where the connection is reset by the client or server
            System.out.println("Connection reset by peer: " + ex.getMessage());
        } catch (Exception ex) {
            // Handle other potential exceptions
            System.out.println("Data forwarding error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                out.close(); // Ensure the output stream is closed
            } catch (Exception e) {
                System.out.println("Failed to close output stream: " + e.getMessage());
            }
        }
    }
}