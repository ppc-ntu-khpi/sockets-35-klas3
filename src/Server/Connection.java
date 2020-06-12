import java.io.*;
import java.net.*;

class Connection implements Runnable {

  ChatServer server = null;
  private Socket communicationSocket = null;
  private OutputStreamWriter out = null;
  private BufferedReader in = null;
  private boolean isGreetingsSent = false;
  private String student = "";

  public Connection(ChatServer server, Socket s) {
    this.server = server;
    this.communicationSocket = s;
  }       

  public void sendMessage(String message) {
    try {
      out.write(message);
      out.flush();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void run() {
    OutputStream socketOutput = null;
    InputStream socketInput = null;
    String magic = server.getMagicPassphrase();

    try {
      socketOutput = communicationSocket.getOutputStream();
      out = new OutputStreamWriter(socketOutput);
      socketInput = communicationSocket.getInputStream();
      in = new BufferedReader(new InputStreamReader(socketInput));
      String input = null;

      while ((input = in.readLine()) != null) {
        if (input.indexOf(magic) != -1 && isGreetingsSent) {
            server.playMagicSound();
            sendMessage("Congratulations "+student+" you sent the passphrase!\n");
            System.out.println(student+" sent the passphrase!");
	} else if (isGreetingsSent) {
            server.sendToAllClients(input+"\n");
	} else {
            InetAddress address = communicationSocket.getInetAddress();
            String hostname = address.getHostName();
            String welcome = 
              "Connection made from host: "+hostname+"\nEverybody say hello";
            student = input;
            if (student != null) welcome += " to "+student;
            welcome+="\n";
            server.sendToAllClients(welcome);
            sendMessage("Welcome "+student+" the passphrase is "+magic+"\n");
            isGreetingsSent = true;
        }
      }
    } catch(Exception e) {
      e.printStackTrace(System.err);
    } finally {
      try {
	if (in != null) in.close();
	if (out != null) out.close();
	communicationSocket.close();
      }  catch(Exception e) {
	e.printStackTrace();
      }
      server.closeConnection(this);
    }
  }
}
