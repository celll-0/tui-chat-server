package com.cello;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;

public class CLILoggingRoutines {
    private static final String UI_SEPARATOR = "\n============================================\n\n";
    private static final String UI_INSERT_CARROT = "\n>>>>>>  ";

    /**
     * This method is designed to handle exceptions that occur when a connection is closed.
     * It prompts the user to decide whether they want to see the stack trace of the
     * exception. If the user chooses to see the stack trace, it will be printed to the
     * terminal; otherwise, it will simply print a separator line.
     *
     * @param   terminal A Terminal object used to interact with the terminal interface.
     * @param   e The exception that occurred when the connection was closed. This can be
     *            null, in which case the method will simply print a separator line without
     *            prompting the user.
     *
     * @return  This method does not return any value. It performs its operations by
     *          writing to the terminal.
     */
    public static void closedConnectionException_seeStacktrace(Terminal terminal, Exception e) {
        PrintWriter terminalWriter = terminal.writer();
        LineReader terminalReader = LineReaderBuilder.builder().terminal(terminal).build();
        closedConnectionException_seeStacktrace(terminalWriter, terminalReader, e);
    }

    /**
     * This method is the core implementation of 'closedConnectionException_seeStacktrace'
     * to handle exceptions that occur when a connection is closed. It takes a PrintWriter
     * and a LineReader as parameters, which are used to interact with the terminal. The
     * method prompts the user to decide whether they want to see the stack trace of the
     * exception. If the user chooses to see the stack trace, it will be printed to the
     * terminal; otherwise, it will simply print a separator line.
     *
     *
     *
     * @param   terminalWriter A PrintWriter object used to write output to the terminal.
     * @param   terminalReader A LineReader object used to read input from the terminal.
     * @param   e The exception that occurred when the connection was closed. This can be
     *            null, in which case the method will simply print a separator line
     *            without prompting the user.
     *
     * @return  This method does not return any value. It performs its operations by writing
     *          to the terminal.
     */
    public static void closedConnectionException_seeStacktrace(PrintWriter terminalWriter, LineReader terminalReader, Exception e) {
        if (e != null) {
                terminalWriter.println(UI_SEPARATOR);
                String prompt = UI_INSERT_CARROT + "Do want to see the stracktrace ('" + e.getClass().getSimpleName() + "') -- (y)es / (n)o:  ";

            for(String inp; (inp = terminalReader.readLine(prompt)) != null;) {
                terminalWriter.write(UI_SEPARATOR);

                switch(inp){
                    case "y":
                        e.printStackTrace(terminalWriter);
                        terminalWriter.write(UI_SEPARATOR);
                        terminalWriter.flush();
                        break;

                    case "n":
                        terminalWriter.write(UI_SEPARATOR);
                        break;
                }
            }
        }
    }

}
