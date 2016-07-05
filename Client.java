import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * client 
 * @author nali
 *
 */
public class Client {

    private static clientFrame frame;

    /**
    * network start
    */
    private void start() {

        Socket socket = null;
        Scanner income = null;
        PrintWriter output = null;

        while (true) {

            //Slow down loop to avoid block.
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            //应用的Socket对象的getInputStream方法从服务器接收数据，
            //并且应用Socket对象的getOuputStream方法发送数据到服务器。
            if (frame != null) {
                if (frame.okConnect()) {
                    try {
                        if (socket != null) socket.close();
                        socket = new Socket(frame.getHostName(), Integer.parseInt(frame.getPort()));
                        output = new PrintWriter(socket.getOutputStream(), true);
                        income = new Scanner(socket.getInputStream());                 
                        frame.outputPrintWriter(output);              
                        frame.isConnect();               
                    } catch (Exception ex1) {
                        frame.isConnect(); 
                        System.out.println("Please open server!");
                    }
                }

                if (income != null && income.hasNextLine()) {
                    String letter = income.nextLine();
                    if (!letter.equals("")) 
                        showChatInGUI("\n" + letter);
                }
            
            }
        }
    }
    
    /**
    *display chat content in window
    *@param chat
    */
    private void showChatInGUI(final String chat) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame.showChat(chat);
            }
        });
    }
    
    /**
    *initialize
    *@param username
    */
    private static void initGUI(final String username) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame = new clientFrame(username);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
       });
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("User name none, please input");
            System.exit(-1);
        }
      
        Client client = new Client();
        initGUI(args[0]);
        client.start(); //Run client.
    }
}



class clientFrame extends JFrame {

  
	/**
	 * main chat part
	 */
    private JTextArea textArea;  
/**
 * input pat
 */
    private JTextArea inputArea;

    private JTextField userName;

    private JTextField hostName;

    private JTextField hostPort;
    /**
     * indicate server and client connect or not
     */
    private boolean okConnect = false;
    /**
    *output object used to send information to another one.
    */
    private PrintWriter outp = null;

  
    public clientFrame(String username) {
        
    	/**
         * build the chat window 
         */
         Toolkit kit = Toolkit.getDefaultToolkit();
         Dimension screenSize = kit.getScreenSize();
         setSize(screenSize.width / 2, screenSize.height / 2);
         setLocationByPlatform(true);

         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setTitle("ChatRoom");
         setResizable(false);
            
         
         JPanel chatPanel = new JPanel();
         
         userName = new JTextField(username, 20);
         hostName = new JTextField("127.0.0.1", 20);
         hostPort = new JTextField("8080", 20);

         chatPanel.setLayout(new GridLayout(1, 4));
         chatPanel.add(new JLabel("User name: ", JLabel.RIGHT));
         chatPanel.add(userName);
         chatPanel.add(new JLabel("Host name: ", JLabel.RIGHT));
         chatPanel.add(hostName);
         chatPanel.add(new JLabel("Host Port: ", JLabel.RIGHT));
         chatPanel.add(hostPort);
         chatPanel.add(new JLabel("",JLabel.RIGHT));
         connectTo(chatPanel);
         add(chatPanel, BorderLayout.NORTH);
         
   
         textArea = new JTextArea(8, 40);
         textArea.setEditable(false);
         JScrollPane chatScrollPane = new JScrollPane(textArea);
         add(chatScrollPane, BorderLayout.CENTER);


      
         JPanel sendPanel = new JPanel();
         sendPanel.add(new JLabel("Please input: ", JLabel.RIGHT));
         inputArea = new JTextArea(2, 45);
         JScrollPane inputScrollPane = new JScrollPane(inputArea);
         sendPanel.add(inputScrollPane);
         sendTo(sendPanel);
         add(sendPanel, BorderLayout.SOUTH);
        
  
    }

    
    /**
     * build connect button and add click event 
     * @param chatPanel
     */
        private void connectTo(final JPanel chatPanel) {
            final JButton connectButton = new JButton("Connect");
            chatPanel.add(connectButton);
            connectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                   /**
                    * host name and host port NOT none
                    */
                    if (getHostName().equals("") || getPort().equals(""))
                    JOptionPane.showMessageDialog(null,"Host name and host port NOT none!");
                    else
                    	okConnect = true;
                    	
                }
            });

        }

    /**
     * build send button and add click event 
     * @param sendPanel
     */

        private void sendTo(final JPanel ctrlPanel) {
            JButton sendButton = new JButton("Send");
            ctrlPanel.add(sendButton);
            sendButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!inputArea.getText().equals("")) {
                        if (getUserName().equals(""))
                        	JOptionPane.showMessageDialog(null,"User name is empty, please input.");
                        else {
                            if (outp == null)
                            	JOptionPane.showMessageDialog(null,"Please click 'connect' button ---- connect server.");
                            else {
                     
                                if (!outp.checkError()) { //check server connect or not
                                    outp.println(getUserName() + ": " + inputArea.getText());
                                    inputArea.setText("");
                                }
                                else
                                	JOptionPane.showMessageDialog(null,"Must open server !!!");
                            }
                        }
                    }
                }
            });
        }

 
    public void outputPrintWriter(PrintWriter output) {
        outp = output;
    }
/**
 * display the chat content
 * @param chat
 */
    public void showChat(String chat) {
    	textArea.append(chat);
    }
/**
 * get user name
 * @return
 */
    public String getUserName() {
        return userName.getText(); 
    }
/**
 * get host name
 * @return
 */
    public String getHostName() {
        return hostName.getText();
    }
    
 /**
  * get host port
  * @return
  */
    public String getPort() {
        return hostPort.getText();
    }

    /**
     * get the statue of chat
     * @return
     */
    public boolean okConnect() {
        return okConnect;
    }

    
    /**
     * chat class call this method make connection is shut down statue
     */
    public void isConnect() {
    	okConnect = false;
    }
}
