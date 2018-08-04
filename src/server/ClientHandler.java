//Naimul Haque

package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;


public class ClientHandler extends Thread{

    private final Socket clientSocket;
    private String login = null;
    private String password = null;
    private boolean isOnline;
    private  Server server;
    private OutputStream outputStream;
    private HashSet<String> groupSet = new HashSet<>(); 
    private ArrayList<String> friendList = new ArrayList<>(); 
    private ArrayList<String> reqfriendlist = new ArrayList<>(); 
    
    public ClientHandler(Server server,Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.isOnline = false;
    }

    @Override
    public void run() {
        try {
            
            handleClientSocket();
            
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleClientSocket() throws IOException {
        this.outputStream = clientSocket.getOutputStream();
        
        InputStream inputStream = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        
        String line;
        
        while((line = reader.readLine())!= null){
            String[] tokens = StringUtils.split(line);
            
            if(tokens!=null&& tokens.length > 0){
                String command = tokens[0];

                if (command.equalsIgnoreCase("quit") || command.equals("logoff")) {
                    handleLogoff();
                    break;
                }
                else if (command.equalsIgnoreCase("reg")) {
                    
                    handleRegister(outputStream,tokens);
                    
                }
                else if(command.equalsIgnoreCase("broadcast")){
                    String[] broadcastMsgTokens = StringUtils.split(line,null,2);
                    handleBroadcast(broadcastMsgTokens);
                }
                else if(command.equalsIgnoreCase("multicast")){
                    handleMulticast(tokens,line);
                }
                else if(command.equalsIgnoreCase("msg"))
                {
                    String[] directMsgTokens = StringUtils.split(line,null,3);
                    handleMessage(directMsgTokens);
                }
                else if(command.equalsIgnoreCase("join")){
                    handleJoin(tokens);
                }
                else if(command.equalsIgnoreCase("show")){
                    handleShowList();
                }
                else if(command.equalsIgnoreCase("req")){
                    handleRequest(tokens);
                }
                else if(command.equalsIgnoreCase("ac")){
                    handleAccept(tokens);
                }
                else if(command.equalsIgnoreCase("leave")){
                    handleLeave(tokens);
                }
                else if(command.equalsIgnoreCase("login")){
                    
                    handleLogin(outputStream,tokens);
                }
                else{
                    String msg = "Unkown Command: " + command + "\n";
                    outputStream.write(msg.getBytes());
                }
                
            }
                    
        }
        
        
    }    
    
    private void handleRegister(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length == 3){
            String username = tokens[1];
            String password =  tokens[2];
             
            
            User newUser = new User(username, password);
            
            boolean isValidId = true;
            
            for(User user: server.getValidUsers()){
                
                if(user.getUserName().equals(username) || user.getPassword().equals(password)){
                    String error = "Username or password already exit" + "\n";
                    outputStream.write(error.getBytes());
                    isValidId = false;

                    System.err.println("Registration failed");
                    break;
                }
            }
            
            if(isValidId){
                server.addUser(newUser);
                
                String msg = "Ok login" +"\n";
                outputStream.write(msg.getBytes());
                this.login = newUser.username;
                this.password = newUser.password;
                
                System.out.println("User logged in successfully : "+login);
                
                ArrayList<ClientHandler> workerList = server.getWorkerList();
                
                //send current user all other online users
                for(ClientHandler worker: workerList){
                    
                        if (worker.getLogin() != null && !login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                    }
                        
                    
                    
                }
                
                //send other online users current user's status
                String onlineMsg = "online " + login + "\n";
                for(ClientHandler worker : workerList){
                    
                    if(!login.equals(worker.getLogin())){
                        worker.send(onlineMsg);
                   }
                }
                
            }
            
            
                
        }
    }
    
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length == 3){
            String login = tokens[1];
            String password =  tokens[2];
            
            
            User reqUser = new User(login,password);
            
          
            
            for(User user: server.getValidUsers()){
                if(user.username.equals(reqUser.username) && user.password.equals(reqUser.password)){
                    isOnline = true;
                    break;
                }
            }
            
            if(isOnline){
                
                String msg = "Ok login" +"\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                 
                System.out.println("User logged in successfully : "+login);
                
                ArrayList<ClientHandler> workerList = server.getWorkerList();
                
                //send current user all other online users
                for(ClientHandler worker: workerList){
                    
                        if (worker.getLogin() != null && !login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                    }
                        
                    
                    
                }
                
                //send other online users current user's status
                String onlineMsg = "online " + login + "\n";
                for(ClientHandler worker : workerList){
                    
                    if(!login.equals(worker.getLogin())){
                        worker.send(onlineMsg);
                   }
                }
            }else {
                String msg = "Error login " + login + "\n";
                outputStream.write(msg.getBytes());
                
                System.err.println("Login failed for: "+login);
            }
        }
    }
    
    String getLogin()
    {
        return login;
    }

    private void send(String msg) throws IOException {
        if(login!=null)
        {
            outputStream.write(msg.getBytes());
        }
        
    }

    private void handleLogoff() throws IOException {
        server.remove(this);
        ArrayList<ClientHandler> workerList = server.getWorkerList();
        //send other online users current user's status
        String onlineMsg = "offline " + login + "\n";
        for (ClientHandler worker : workerList) {

            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
        
    }

    private void handleMessage(String[] tokens) throws IOException {
        String reciever = tokens[1];
        String msg = tokens[2];
        
        
        boolean isGroupMsg = reciever.charAt(0) == '@';
        
        ArrayList<ClientHandler> workerList = server.getWorkerList();
        for(ClientHandler clientHandler: workerList)
        {
            if(isGroupMsg){
                
                if(clientHandler.isMemberOfGroupSet(reciever) && friendList.contains(reciever)){
                    
                    String outMessage = "msg " + reciever+":"+login + " " + msg + "\n";
                    System.out.println(outMessage);
                    clientHandler.send(outMessage);
                }
            }
            else{
                
                if (clientHandler.getLogin().equalsIgnoreCase(reciever ) && friendList.contains(reciever)) {
                    String outMessage = "msg " + login + " " + msg + "\n";
                    clientHandler.send(outMessage);
                }
            }
        }
    }

    private void handleJoin(String[] tokens) {
        if(tokens.length>1){
            String group = tokens[1];
            groupSet.add(group);
            System.out.println("You are added to "+group);
        }
    }
    
    private void handleShowList() throws IOException {
        
            String fred = "fred  ";
            String reqfred = "req ";
            
            for(String user: friendList){
                fred += user + " ";
                
            }
            
            fred += ":";
            
            for(String user: reqfriendlist){
                reqfred += user + " ";
                
            }
            
            reqfred += "\n";
            
            fred += reqfred;
            outputStream.write(fred.getBytes());
            
        
    }
    
    
    private boolean isMemberOfGroupSet(String group){
        
        return groupSet.contains(group);
    }

    private void handleLeave(String[] tokens) {
        if(tokens.length>1){
            String group = tokens[1];
            groupSet.remove(group);
        }
    }
    private void handleRequest(String[] tokens) {
        String user = tokens[1];
        
        ArrayList<ClientHandler> workers = server.getWorkerList();
        
        for(ClientHandler worker: workers){
            if (worker.getLogin().equalsIgnoreCase(user)) {
                String outMessage = "Request from " + login + "\n";
                worker.setFriendReq(login);
            }
        }
        
        
    }
    private void handleAccept(String[] tokens) throws IOException {
        String user = tokens[1];
        
        
        
        ArrayList<ClientHandler> workers = server.getWorkerList();
        
        for(ClientHandler worker: workers){
            if (worker.getLogin().equalsIgnoreCase(user)) {
                
                worker.setFriend(login);
                setFriend(user);
                removeFriendReq(user);
                break;
            }
        }
        
        
        
        
        
    }
    
    
    private void handleBroadcast(String[] tokens) throws IOException {

        String msg = tokens[1];
        
        
        
        ArrayList<ClientHandler> workerList = server.getWorkerList();
        for(ClientHandler clientHandler: workerList)
        {
            if (!clientHandler.getLogin().equalsIgnoreCase(login)) {
                String outMessage = "msg " + login + " " + msg + "\n";
                clientHandler.send(outMessage);
            }
            
        }
    }

    private void handleMulticast(String[] tokens,String line) throws IOException {
        int n = Integer.parseInt(tokens[1]);

        String[] users = new String[n];
        String[] multicastMsg = StringUtils.split(line, null, n + 3);
        String msg = multicastMsg[n + 2];

        for (int i = 0; i < n; i++) {
            users[i] = tokens[i + 2];

            ArrayList<ClientHandler> workerList = server.getWorkerList();
            for (ClientHandler clientHandler : workerList) {

                if (clientHandler.getLogin().equalsIgnoreCase(users[i])) {
                    String outMessage = "msg " + login + " " + msg + "\n";
                    clientHandler.send(outMessage);
                }

            }
        }
        
        
         
        
    }

    
    public void setFriendReq(String user) {
        reqfriendlist.add(user);
    }
    
    public void setFriend(String user) {
        friendList.add(user);
    }
    
    public void removeFriendReq(String user) {
        reqfriendlist.remove(user);
    }
    
    public void removeFriend(String user) {
        friendList.remove(user);
    }
    public ArrayList<String> getFriendList(){
        return  friendList;
    }
    
    public ArrayList<String> getReqFriendList(){
        return  reqfriendlist;
    }
    
}
