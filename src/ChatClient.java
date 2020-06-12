import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import javax.swing.JOptionPane;

//based on code snippets from original Java course
public class ChatClient {
    
  private Socket connection = null;
  private BufferedReader serverIn = null;
  private PrintStream serverOut = null;

  private TextArea output;
  private TextField input;
  private Button sendButton;
  private Button quitButton;
  private Button connectButton;
  private Frame frame;
  private TextArea username;
  private Dialog aboutDialog;
  
  private boolean isGreetingsSent = false;

  public ChatClient() {
    output = new TextArea(10,50);
    input = new TextField(50);
    sendButton = new Button("Send");
    quitButton = new Button("Quit");
    connectButton = new Button("Connect");
    username = new TextArea(1, 20);
  }

  public void launchFrame() {
    frame = new Frame("PPC Chat");

    // Use the Border Layout for the frame
    frame.setLayout(new BorderLayout());

    frame.add(output, BorderLayout.WEST);
    frame.add(input, BorderLayout.SOUTH);

    // Create the button panel
    Panel p1 = new Panel(); 
    p1.setLayout(new GridLayout(3,1));
    p1.add(connectButton);
    p1.add(sendButton);
    p1.add(quitButton);
    
    Panel p2 = new Panel();
    p2.add(username);
    
    sendButton.setEnabled(false);
    quitButton.setEnabled(false);

    // Add the button panel to the center
    frame.add(p1, BorderLayout.CENTER);
    frame.add(p2, BorderLayout.EAST);
    
    username.setFont(new Font("Arial Black", Font.BOLD, 18));
    

    // Create menu bar and File menu
    MenuBar mb = new MenuBar();
    Menu file = new Menu("File");
    MenuItem quitMenuItem = new MenuItem("Quit");
    quitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	System.exit(0);
      }
    });
    file.add(quitMenuItem);
    mb.add(file);
    frame.setMenuBar(mb);

    // Add Help menu to menu bar
    Menu help = new Menu("Help");
    MenuItem aboutMenuItem = new MenuItem("About");
    aboutMenuItem.addActionListener(new AboutHandler());
    help.add(aboutMenuItem);
    mb.setHelpMenu(help);

    // Attach listener to the appropriate components
    sendButton.addActionListener(new SendHandler());
    input.addActionListener(new SendHandler());
    frame.addWindowListener(new CloseHandler());
    quitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
    });
    connectButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (username.getText().compareTo("") == 0) {
                JOptionPane.showMessageDialog(frame, "Enter your name to connect to the chat!");
                return;
            }
            String serverIP = System.getProperty("serverIP", "127.0.0.1");
            String serverPort = System.getProperty("serverPort", "2000");
            try {
                connection = new Socket(serverIP, Integer.parseInt(serverPort));
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
                serverIn = new BufferedReader(isr);
                serverOut = new PrintStream(connection.getOutputStream());    
                Thread t = new Thread(new RemoteReader());
                sendButton.setEnabled(true);
                quitButton.setEnabled(true);
                connectButton.setEnabled(false);
                t.start();
            } catch (Exception ex) {
                System.err.println("Unable to connect to server!");
                ex.printStackTrace();
            }
            sendMessage();
        }
    });

    frame.pack();
    frame.setVisible(true);
    frame.setLocationRelativeTo(null);
  }
  
  private void sendMessage() {
      String text = input.getText();
      text = username.getText() + (isGreetingsSent ? ": " + text : "") + "\n";
      serverOut.print(text);
      input.setText("");
      if (!isGreetingsSent) {
          username.setEnabled(false);
          isGreetingsSent = true;
      }
  }
  
    
    private class RemoteReader implements Runnable {
        public void run() {
          try {
            while ( true ) {
              String nextLine = serverIn.readLine();
              output.append(nextLine + "\n");
            }
          } catch (Exception e) {
              System.err.println("Error while reading from server.");
              e.printStackTrace();
            }
        } // закінчення методу run 
    }

  private class SendHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      sendMessage();
    }
  }
  
  private class CloseHandler extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }
  }

  private class AboutHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JOptionPane.showMessageDialog(frame, "The ChatClient is a neat tool that allows you to talk " +
                  "to other ChatClients via a ChatServer");
    }
  }

  private class AboutDialog extends Dialog implements ActionListener  {
    public AboutDialog(Frame parent, String title, boolean modal) {
      super(parent,title,modal);
      add(new Label("The ChatClient is a neat tool that allows you to talk " +
                  "to other ChatClients via a ChatServer"),BorderLayout.NORTH);
      Button b = new Button("OK");
      add(b,BorderLayout.SOUTH);
      b.addActionListener(this);
      pack();
    }
    // Hide the dialog box when the OK button is pushed
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  public static void main(String[] args) {
    ChatClient c = new ChatClient();
    c.launchFrame();
  }
}
