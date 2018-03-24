package me.theminecoder.util.lanternassh.terminal;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.Signal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author theminecoder
 */
public class SSHTerminal extends UnixTerminal {

    private Environment environment;

    public SSHTerminal(InputStream terminalInput, OutputStream terminalOutput, Charset terminalCharset, Environment environment) throws IOException {
        super(terminalInput, terminalOutput, terminalCharset, CtrlCBehaviour.TRAP);
        this.environment = environment;
        this.getInputDecoder().addProfile(SSHInputMapping.INSTANCE);
        environment.addSignalListener((signal) -> {
            if (signal == Signal.WINCH) {
                try {
                    final TerminalSize size = getTerminalSize();
                    onResized(size.getColumns(), size.getRows());
                } catch (final IOException e) {
                    System.err.println("Failed to read terminal size after being notified of a resize.");
                    e.printStackTrace();
                }
            }
        }, Signal.WINCH);
    }

    @Override
    public TerminalSize findTerminalSize() throws IOException {
        String colsStr = this.environment.getEnv().getOrDefault(Environment.ENV_COLUMNS, "80");
        String linesStr = this.environment.getEnv().getOrDefault(Environment.ENV_LINES, "22");
        return new TerminalSize(Integer.parseInt(colsStr), Integer.parseInt(linesStr));
    }

    public String getRunningUser() {
        return this.environment.getEnv().get("USER");
    }

    public Environment getEnvironment() {
        return environment;
    }
}
