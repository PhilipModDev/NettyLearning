package org.example;

import org.example.network.Client;
import org.example.network.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        Thread thread = new Thread(() -> {
            try {
                server.start();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.setDaemon(true);
        Client client = new Client();
        Thread threadTwo = new Thread(client::start);
        threadTwo.setDaemon(true);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please type either or both client and server to start...");
            boolean isClientRunning = false;
            boolean isServerRunning = false;
            for (;;){
                String message = reader.readLine();
                if (message.equalsIgnoreCase("client") && !isClientRunning){
                    isClientRunning = true;
                    threadTwo.start();
                }
                if (message.equalsIgnoreCase("server") && !isServerRunning){
                    isServerRunning = true;
                    thread.start();
                }
                if (message.equalsIgnoreCase("exit")){
                    break;
                }
            }
        }catch (Exception exception) {
            exception.printStackTrace();
        }
        System.exit(0);
    }
}