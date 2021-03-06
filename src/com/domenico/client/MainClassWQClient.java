package com.domenico.client;

/** Starting point for the client.
 *  The usage can be printed with the --help argument (java MainClassWQClient --help) */
public class MainClassWQClient {

    //The usage of this program
    public static final String USAGE = "usage : COMMAND [ ARGS ...]";
    private static final String HELP_COMMAND_ARG = "--help";    //The program argument associated to the usage

    public static void main(String[] args) {
        //Checks if the program has the help argument
        if (args.length == 1 && args[0].equals(HELP_COMMAND_ARG)) {
            System.out.println(USAGE);
            CLI.printCommandsUsage();
            return;
        }

        try {
            CLI cli = new CLI();
            WQClient wqClient = new WQClient(cli);
            cli.loop(wqClient);
        } catch (Exception e) {
            System.out.println("Disconnesso dal server, termino.");
        }
    }
}
