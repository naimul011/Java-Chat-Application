/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


class Server extends Thread{

    private final int serverPort;
    private ArrayList<ClientHandler> workerList = new ArrayList<>();
    private ArrayList<User> validUsers = new ArrayList<>();
    
    public Server(int serverport) {
        this.serverPort = serverport;
    }

    
    
    
    
    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(serverPort);
            
            
            while(true){
            System.out.println("The server is waiting for client...");
            Socket clientSocket =  serverSocket.accept();
            System.out.println("The server is conneted to client "+clientSocket);
            
            ClientHandler clientHandler = new ClientHandler(this,clientSocket);
            workerList.add(clientHandler);
            clientHandler.start();
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    ArrayList<ClientHandler> getWorkerList() {
        return workerList;
    }

    void remove(ClientHandler clientHandler) {
        workerList.remove(clientHandler);
    }

    public ArrayList<User> getValidUsers() {
        return validUsers;
    }
    
    public void addUser(User user){
        validUsers.add(user);
    }

    public void removeValidUsers(User user) {
        validUsers.remove(user);
    }
}
