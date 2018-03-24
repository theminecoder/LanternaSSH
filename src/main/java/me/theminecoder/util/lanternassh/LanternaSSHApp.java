package me.theminecoder.util.lanternassh;

import me.theminecoder.util.lanternassh.terminal.SSHTerminal;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author theminecoder
 */
@SuppressWarnings("WeakerAccess")
public abstract class LanternaSSHApp {
    private SSHTerminal terminal;

    private Consumer<Integer> exitFunction;

    void configure(SSHTerminal terminal, Consumer<Integer> exitFunction) {
        this.terminal = terminal;
        this.exitFunction = exitFunction;
    }

    public abstract void run() throws IOException;

    protected final SSHTerminal getTerminal() {
        return terminal;
    }

    protected final void shutdown() {
        this.shutdown(0);
    }

    protected final void shutdown(int code) {
        this.exitFunction.accept(code);
    }
}
