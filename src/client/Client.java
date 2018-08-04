
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

public class Client {
    
    private String serverName;
    private int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;
    
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private ArrayList<FriendListListener> friendListListeners = new ArrayList<>();
    
    public Client(String serverName,int serverPort)
    {
        this.serverName =serverName;
        this.serverPort=serverPort;
    }
    
    
    

    boolean connect() {
        
        try {
            this.socket = new Socket(serverName,serverPort);
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " +password+"\n";
        
        serverOut.write(cmd.getBytes());
        
        String response = bufferedIn.readLine();
        System.out.println("Server response: "+response);
        
        if(response.equalsIgnoreCase("ok login")){
            startMessageReader();
            return true;
        }
        else{
            return false;
        }
    }
    
    void joinGroup(String groupName) throws IOException{
        String cmd = "join @" + groupName + "\n";
        
        
        serverOut.write(cmd.getBytes());
        
    }
    
    void leaveGroup(String groupName) throws IOException{
        String cmd = "leave @" + groupName + "\n";
        
        serverOut.write(cmd.getBytes());
        
    }
    
    boolean register(String login, String password) throws IOException {
        String cmd = "reg " + login + " " +password+"\n";
        
        serverOut.write(cmd.getBytes());
        
        String response = bufferedIn.readLine();
        System.out.println("Server response: "+response);
        
        if(response.equalsIgnoreCase("ok login")){
            startMessageReader();
            return true;
        }
        else{
            return false;
        }
    }
    
    public void logoff() throws IOException {
        String cmd = "logoff" +"\n";
        
        serverOut.write(cmd.getBytes());
        
    }
     

    private void startMessageReader() {
    
        Thread t = new Thread(){
            @Override
            public void run() {
                readMessageLoop();
            }

            
            
        };
        
        t.start();
    }
    
    private void readMessageLoop() {
        String line;
        
        try {
            while((line = bufferedIn.readLine())!=null){
                String[] tokens = StringUtils.split(line);
                if(tokens != null && tokens.length>0){
                    String cmd = tokens[0];
                    if(cmd.equalsIgnoreCase("online")){
                        handleOnline(tokens);
                    }
                    else if(cmd.equalsIgnoreCase("offline")){
                        handleOffline(tokens);
                    }
                    else if(cmd.equalsIgnoreCase("msg")){
                        String[] directMsgTokens = StringUtils.split(line,null,3);
                        handleMessage(directMsgTokens);
                    } 
                    else if(cmd.equalsIgnoreCase("fred")){
                        String[] list = line.split(":");
                        handleFriendList(list[0], list[1]);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            try {
                socket.close();
            } catch (IOException ex1) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private void handleOnline(String[] tokens) {
        String login =  tokens[1];
        for(UserStatusListener listener: userStatusListeners){
            listener.online(login);
        }
        
    }

    private void handleOffline(String[] tokens) {
        
        String login =  tokens[1];
        for(UserStatusListener listener: userStatusListeners){
            listener.offline(login);
        }
    }
    
    
    private void handleMessage(String[] tokensMsg) {
        
        String login = tokensMsg[1];
        String msg = tokensMsg[2];
        
        for(MessageListener listener: messageListeners){
            listener.onMessage(login, msg);
        }
        
    }
    
    private void handleFriendList(String fred, String reqFred){
        
        for(FriendListListener listener: friendListListeners){
            listener.onFriendListShow(fred, reqFred);
        }
    }
    
    void sendBroadcast(String msg) throws IOException {
        String cmd = "broadcast "+msg+"\n";
        serverOut.write(cmd.getBytes());
    }
    
    void acceptReq(String user) throws IOException {
        String cmd = "ac "+user+"\n";
        serverOut.write(cmd.getBytes());
    }
    
    void sendMulticast(String[] users,int n,String msg) throws IOException {
        String multiUsers = "";
        
        for(String user : users){
            multiUsers += user +" ";
        }
        
        String cmd = "multicast "+n+" "+multiUsers+" "+msg+"\n";
        serverOut.write(cmd.getBytes());
    }
    
    void sendRequest(String reciever) throws IOException {
        String cmd = "req "+reciever+"\n";
        serverOut.write(cmd.getBytes());
    }
    
    void sendMessage(String reciever, String msg) throws IOException {
        String cmd = "msg "+reciever+" "+msg+"\n";
        serverOut.write(cmd.getBytes());
    }
    
    void showFriendList() throws IOException {
        String cmd = "show "+"\n";
        serverOut.write(cmd.getBytes());
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
        
    }
    
    
    
    public void ramoveUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }  
    
    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }
    
    
    public void ramoveMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }  
    
    
    public void addFriendListListener(FriendListListener listener){
        friendListListeners.add(listener);
    }
    
    
    public void ramoveFriendListListener(FriendListListener listener){
        friendListListeners.remove(listener);
    }  

    
}
