package me.theminecoder.util.lanternassh;

import me.theminecoder.util.lanternassh.terminal.SSHTerminal;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author theminecoder
 */
public class LanternaSSHAppWrapper implements Command {

    private static final AtomicInteger APP_COUNTER = new AtomicInteger(1);

    private int clientId;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback exitCallback;

    private boolean running = false;
    private LanternaSSHApp app;
    private SSHTerminal terminal;
    private ThreadGroup threadGroup;
    private Thread thread;

    public LanternaSSHAppWrapper(Supplier<LanternaSSHApp> appSupplier) {
        this.app = appSupplier.get();
        clientId = APP_COUNTER.getAndIncrement();
        this.threadGroup = new ThreadGroup("SSH Client - " + clientId);
    }

    @Override
    public void setInputStream(final InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(final OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(final OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback exitCallback) {
        this.exitCallback = exitCallback;
    }

    @Override
    public void start(Environment environment) throws IOException {
        if (running) {
            return;
        }

        this.terminal = new SSHTerminal(this.in, this.out, Charset.forName("UTF8"), environment);
        app.configure(this.terminal, (code) -> {
            try {
                this.terminal.close();
            } catch (Exception ignored) {
            }
            new Thread(() -> {
                exitCallback.onExit(code);
                if (!this.threadGroup.isDestroyed()) {
                    this.threadGroup.destroy();
                }
            }).start();
        });

        this.thread = new Thread(threadGroup, () -> {
            Throwable e = null;

            try {
                app.run();
            } catch (Throwable ex) {
                e = ex;
            }

            try {
                this.terminal.close();
            } catch (Exception ignored) {
            }

            if (e != null) {
                PrintWriter errWriter = new PrintWriter(err, true) {
                    @Override
                    public void println() {
                        print('\r'); //SSH Fix
                        super.println();
                    }
                };

                errWriter.println("Error in application:");
                e.printStackTrace(errWriter);
                e.printStackTrace();
                errWriter.println();
                errWriter.println("Press any key to exit...");
                try {
                    in.skip(in.available());
                    in.read();
                } catch (IOException ignored) {
                }
            }

            exitCallback.onExit(0);
        });
        this.thread.setName("SSH Client Thread - " + clientId);
        this.thread.start();

        running = true;
    }

    @Override
    public void destroy() throws Exception {
        if (!running) {
            return;
        }

        try {
            this.terminal.close();
        } catch (Exception ignored) {
        }

        if (this.thread.isAlive()) {
            try {
                this.thread.stop();
            } catch (ThreadDeath ignored) {
            }
        }

        if (!this.threadGroup.isDestroyed()) {
            this.threadGroup.destroy();
        }
        running = false;
    }

}
