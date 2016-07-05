import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * server
 * @author nali
 *
 */

public class Server {

   // private static int i=1;
    private static serverFrame frame;
 
    
    /**
    *server  port.
    */
    private static String serverPort = "8080";
    
    /**
    *client list
    */
    private static final ArrayList<ClientList> clientList = new ArrayList<ClientList>();
    
    
    /**
    * message queue.
    */
    private static final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>(); 

    /**
    *add client to client list
    *@param client
    */
    private static synchronized void addClient(ClientList client) {
        clientList.add(client);
    }

    /**
     * remove client to client list
     * @param client
     */
   private static synchronized void removeClient(ClientList client) {
        clientList.remove(client);
        client.interrupt();//stop client thread
    }
    
    /**
    *thread to new client for his income and output
    */
    class ClientList extends Thread {
        private Scanner income;

        private PrintWriter output;

        /**
        *The constructor of the class ClientList.
        *@param inCome
        *@param outPut
        */
        public ClientList(Scanner inCome, PrintWriter outPut) {
        	income= inCome;
        	output = outPut;
        }  
        /**
        *put income message to messageQueue
        */
        public void run() {
            while(!isInterrupted()) {
                try {
                    if (income != null && income.hasNextLine()) {
                        String letter = income.nextLine();
                        messageQueue.put("\n" + letter);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
    *thread--- send message to every client
    */
    static class sendMessage implements Runnable {
      
        public void run() {
            while(true) {
                try {
                	String message = messageQueue.take(); 
                    for (ClientList client : clientList) {
                    	
                         if(client.output.checkError())
                        	 removeClient(client);
                         
                         Date dNow = new Date( );
                         SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
                         client.output.println(ft.format(dNow) + '\n'+ message);	 
                         //client.output.println(message);
                    }
                
                } catch (InterruptedException e) {
                   e.printStackTrace();
                }
            }
        }
    }

    /**
    *network 
    */
    private void startNetwork() {
        ServerSocket severSocket =null ;
        Socket clientsocket = null;
        Scanner income = null;
        PrintWriter output = null;
       
        try {
        	severSocket = new ServerSocket(Integer.parseInt(serverPort));
        } catch(IOException ex1) {
            System.out.println("Can not build server socket!");
        }

        while (true) {

            //Slow down loop to avoid block.
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if (frame != null) {
                try {
                	   //Waiting for new client log in.
                        clientsocket = severSocket.accept(); 
                        income = new Scanner(clientsocket.getInputStream());
                        output = new PrintWriter(clientsocket.getOutputStream(), true);
                        output.println("This is chat room! Connect server successfully!");
                        ClientList client = new ClientList(income, output);
                        addClient(client);
                        
                    //    output.println("severSocket" + (i++));
                        client.start();
                        

                    } catch (IOException ex2) {
                        System.out.println("Please open Server!");
                   
                    }

            }
        }
    }

    public static void main(String[] args) {
    	 EventQueue.invokeLater(new Runnable() {
             public void run() {
                 frame = new serverFrame(serverPort);
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 frame.setVisible(true);
             }
        });
        Server server = new Server();
        new Thread(new sendMessage()).start(); //Start message sending thread.
        server.startNetwork(); //Run server.
    }
}



 class serverFrame extends JFrame {

    public serverFrame(String serverPort) {
        
        //Initrialize the window.
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        setSize(500, 100);
        setLocationByPlatform(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("ChatRoom Server");
        setResizable(false);
           
 
        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Server Start!  Server port is " + serverPort + "."));
        add(infoPanel);
    }

}
