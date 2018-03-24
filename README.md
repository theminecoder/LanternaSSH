# LanternaSSH

A wrapper around Lanterna to help with creating ssh based apps.

### Usage

```java
package test;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import me.theminecoder.util.lanternassh.LanternaSSHAppWrapper;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;

import java.io.File;

public class App extends LanternaSSHApp {
    
    public static void main(String[] args){
      SshServer server = SshServer.setUpDefaultServer();
      server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("hostkey.ser")));
      server.setPasswordAuthenticator(new AcceptAllPasswordAuthenticator());
      server.setCommandFactory(() -> new LanternaSSHAppWrapper(App::new));
      server.setPort(4022);
      server.start();
      
      while(true) {
          // Keep tread alive
      }
    }
    
    public void run() throws IOException {
        //Setup Screen
        Screen screen = new TerminalScreen(getTerminal());
        screen.startScreen();
        
        // Create panel to hold components
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));
        
        panel.addComponent(new Label("Forename"));
        panel.addComponent(new TextBox());
        
        panel.addComponent(new Label("Surname"));
        panel.addComponent(new TextBox());
        
        panel.addComponent(new EmptySpace(new TerminalSize(0,0))); // Empty space underneath labels
        panel.addComponent(new Button("Submit"));
        
        // Create window to hold the panel
        BasicWindow window = new BasicWindow();
        window.setComponent(panel);

        // Create gui and start gui
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(window);
    }
    
}
```