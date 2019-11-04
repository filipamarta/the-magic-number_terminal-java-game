package org.academiadecodigo.splicegirls;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Server {

    public final static int PORT = 6666;
    public final String LIST_CMD = "/LIST";
    String line;
    public List<ServerHelper> helpers = Collections.synchronizedList(new ArrayList<ServerHelper>());           // Synchronized List of worker threads
    private int magicNumber;
    private int maxNumber = 10000;


    public static void main(String[] args) {
        int port = PORT;
        try {
            Server server = new Server();
            server.start(port);
        } catch (NumberFormatException ex) {
            System.exit(1);
        }
    }






    // Starts the server on a specified port
    public void start(int port) {

        magicNumber = (int)(Math.random() * maxNumber);

        int connectionCount = 0;
        try {
            System.out.print("Please wait, connecting server to port " + port);
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("  ... Server connected");

            while (true) {                                                      // Block waiting for client connections
                Socket clientSocket = serverSocket.accept();

                System.out.println("Client accepted: " + clientSocket);
                try {
                    connectionCount++;
                    String name = "Player_" + connectionCount;                                                                  // PLAYER NAMES
                    ServerHelper helper = new ServerHelper(name, clientSocket, connectionCount);                                                 // Create a new Server Worker with streams and reader/writer
                    helpers.add(helper);                                                                                        // Adds the worker to the list
                    Thread thread = new Thread(helper);                                                                         // Serve the client connection with a new Thread
                    thread.setName(name);
                    thread.start();
                } catch (IOException ex) {
                    System.out.println("Error receiving client connection: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to start server on port " + port);
        }

    }

    public String listClients() {
        StringBuilder builder = new StringBuilder("\n\n");
        synchronized (helpers) {
            Iterator<ServerHelper> it = helpers.iterator();
            while (it.hasNext()) {
                builder.append("\t");
                builder.append(it.next().getName());
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    // threadName name of the client thread that the message originated from
    // message    the message to broadcast
    private void sendAll(String threadName, String message) {                    // Broadcast a message to all server connected clients
        synchronized (helpers) {                                                // Acquire lock for safe iteration
            Iterator<ServerHelper> it = helpers.iterator();
            while (it.hasNext()) {
                it.next().send(threadName, message);
            }
        }
    }

    private class ServerHelper implements Runnable {                            // Handles client connections
        private String name;
        private int index;
        final private Socket clientSocket;
        final private BufferedReader in;
        final private BufferedWriter out;

        // name: the name of the thread handling this client connection
        // clientSocket: the client socket connection
        public ServerHelper(String name, Socket clientSocket, int index) throws IOException {
            this.name = name;
            this.index = index;
            this.clientSocket = clientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }

        public String getName() {
            return name;
        }

        public boolean isNumeric(String strNum) {
            return strNum.matches("-?\\d+(\\.\\d+)?");
        }


        @Override
        public void run() {
            System.out.println("Thread " + name + " started");

            //////////////////////////////////////////////////////////
            // Code to create player object
            //////////////////////////////////////////////////////////

            Player player = new Player(name, clientSocket.getInetAddress());
            player.setPort(clientSocket.getPort());
            try {
                int maxValue = 2147483647;

                send(name, "First, enter a nickName: ");
                line = in.readLine();            // <<<<<<<<<<<<<<< block
                player.setNickName(line);

                send(name,"WELCOME TO MAGIC NUMBER LAND " + player.getNickName() +"! :D JUST GUESS A NUMBER AND SPREAD THE MAGIC.");
                send(name, "Now, start your bet. Can you guess?! ");
                send(name, "(Note: Did you know that the maximum value of an integer is " + maxValue + " ? So..that is the limit of your bet)");
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Player logged... " +player.getNickName() + " " + player.getIP() + " " + player.getPlayerPort());
            //send(name , "Player logged... " +player.getNickName() + " " + player.getIP() + " " + player.getPlayerPort());

            try {
                while (!clientSocket.isClosed()) {
                    // Blocks waiting for client messages
                    String line = in.readLine();// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< LISTENER

                    if (line == null) {
                        System.out.println("Client " + name + " closed, exiting...");
                        in.close();
                        clientSocket.close();
                        continue;
                    } else if (!(isNumeric(line))){
                        send(name, "Write a number my dude...come on...");
                        continue;
                    }/*else if (!line.isEmpty()) {
                        if (line.toUpperCase().equals(LIST_CMD)) {
                            send("Clients Connected", listClients());
                        }

                    }*/
                    int answer = Integer.parseInt(line);

                    if(answer == magicNumber){
                        String winner = player.getNickName();
                        send(name, " " + player.getNickName() + " : Cool! You just WON with the number " + answer);
                        sendAll(name, " This is your SERVER calling: " + winner + " just fucking won!!!!!!!!");
                        System.out.println(name + " " + player.getNickName() + " : just won with the number " + answer);
                    } else if(answer > magicNumber ){
                        send(name, " " + player.getNickName() + " : The number " + answer + " is far away my friend, try again! (Go down go down go down) ");
                        System.out.println(name + " " + player.getNickName() + ": " + answer);
                    } else if(answer < magicNumber) {
                        send(name, " " + player.getNickName() + " : The number " + answer + " is so cold that I frost, try again! (Go UP go UP go UP) ");
                        System.out.println(name + " " + player.getNickName() + ": " + answer);
                    } else if (line.isEmpty()){
                        send(name, "Write a number my dude...come on...");
                    } else {
                        System.out.println("entrou aqui >>>>>>>>>>>>>>>>><<<<<<<<<<<<<<");
                    }
                }
                helpers.remove(this);
            } catch (IOException ex) {
                System.out.println("Receiving error on " + name + " : " + ex.getMessage());
            } catch (NumberFormatException e){
                e.getStackTrace();
            }
        }
        // Send a message to the client served by this thread
        // origClient: the name of the client thread the message originated from
        // message: the message to send
        private void send(String origClient, String message) {
            try {
                out.write(origClient + ": " + message);
                out.newLine();
                out.flush();
            } catch (IOException ex) {
                System.out.println("Error sending message to Client " + name + " : " + ex.getMessage());
            }
        }
    }
}
