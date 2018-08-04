//Naimul Haque

package server;


public class ServerMain {
    public static void main(String[] args)  {
        
        int port = 9876;
        Server server = new Server(port);
        server.start();
        
        
    }
}
