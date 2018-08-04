
package client;

import com.sun.org.apache.xerces.internal.xs.PSVIProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class UserMain {
    
    
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost",9876);
        
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("Online: "+login);
            }

            @Override
            public void offline(String login) {
                System.out.println("Offline: "+login);
            }
        });
        
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String source, String msg) {
                System.out.println("Message from : "+source+" : "+msg);
                
            }

            
        });
        
        client.addFriendListListener(new FriendListListener() {
            @Override
            public void onFriendListShow(String fred, String reqFred) {
                System.out.println(fred);
                System.out.println(reqFred);
            }
        });
               if (!client.connect()) {
            System.err.println("Connection Failed.");
        } else {
            System.out.println("Connection Successful!");

            System.out.println("1. Login");
            System.out.println("2. Sign Up");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String input1 = reader.readLine();

            if (Integer.parseInt(input1) == 1) {
                System.out.println("Enter you user name ");

                String userName = reader.readLine();

                System.out.println("Enter you password");

                String password = reader.readLine();

                if (client.login(userName, password)) {
                    System.out.println("Login Successful");
                    
                    
                    hangleLogin(client,reader);
                    //client.sendMessage("naimul","hello world");
                } else {
                    System.err.println("Login failed");
                }
            } else if (Integer.parseInt(input1) == 2) {

                System.out.println("Enter you user name ");

                String userName = reader.readLine();

                System.out.println("Enter you password");

                String password = reader.readLine();

                if (client.register(userName, password)) {
                    System.out.println("Login Successful");

                    hangleLogin(client, reader);

                    //client.sendMessage("naimul","hello world");
                } else {
                    System.err.println("Login failed");
                }
            }

            
            
            
           // client.logoff();
        }
    }
    
    static void hangleLogin(Client client,BufferedReader reader) throws IOException {
        
        boolean isOnline = true;
        
        while (isOnline) {
            System.out.println("1. Send direct message");
            System.out.println("2. Send broadcast message");
            System.out.println("3. Join Group");
            System.out.println("4. Leave Group");
            System.out.println("5. Group Chat");
            System.out.println("6. Multicast ");
            System.out.println("7. Quit or logoff");
            System.out.println("8. Send Request: ");
            System.out.println("9. Show Friendlist: ");
            System.out.println("10. Accept Friend req: ");
            
            String input2 = reader.readLine();
            switch (Integer.parseInt(input2)) {
                case 1:
                    System.out.print("To: ");
                    String user = reader.readLine();

                    System.out.print("Type msg: ");
                    String msg = reader.readLine();

                    client.sendMessage(user, msg);
                    break;
                case 2:
                    System.out.print("Type msg: ");
                    client.sendBroadcast(reader.readLine());
                    
                    break;
                case 3:
                    System.out.print("Join to: ");
                    String group = reader.readLine();
                    client.joinGroup(group);
                    
                    System.out.println("You are Joined to @"+group);
                    break;
                case 4:
                    System.out.print("Group to leave : ");
                    String leavegroup = reader.readLine();
                    client.leaveGroup(leavegroup);
                    
                    System.out.println("You are removed from @"+leavegroup);
                    break;
                case 5:
                    System.out.print("Type Group Name: ");
                    String groupName = reader.readLine();
                    
                    groupName = "@"+groupName;
                    
                    System.out.print("Type msg: ("+groupName+"): ");
                    String msg3 = reader.readLine();
                    client.sendMessage(groupName, msg3);
                    
                    break;
                case 6:
                    System.out.print("Type Number of users: ");
                    
                    int n = Integer.parseInt(reader.readLine());
                    
                    String[] users = new String[n];
                    
                    System.out.println("Enter the users: ");
                    
                    for(int i = 0; i < n ; i++){
                        String username = reader.readLine();
                        
                        users[i] = username;
                    }
                    
                    System.out.println("Type message: ");
                    
                    String msg2 = reader.readLine();
                    
                    client.sendMulticast(users, n, msg2);
                    break;
                case 7:
                    client.logoff();
                    isOnline = false;
                    break;
                case 8:
                    System.out.print("Request user: ");
                    String reqUser = reader.readLine();
                    client.sendRequest(reqUser);
                    
                    System.out.println("A request sent to "+reqUser);
                    
                    client.showFriendList();
                    break;
                    
                case 9:
                    
                    client.showFriendList();
                    break;
                case 10:
                    client.showFriendList();
                    
                    System.out.print("Type username: ");
                    
                    String acFrd = reader.readLine();
                    client.acceptReq(acFrd);
                    break;
            }
            
        }
            
    }
}
