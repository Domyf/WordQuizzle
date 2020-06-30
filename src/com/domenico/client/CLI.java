package com.domenico.client;

import java.util.Scanner;

public class CLI implements Runnable {

    private static final String COMMAND_LINE_START = "> ";
    private Scanner scanner;

    public CLI(Scanner scanner) {
        this.scanner = scanner;
    }

    public synchronized UserCommand askForCommand() {
        System.out.print("> ");
        String line = scanner.nextLine().trim();
        if (line.isBlank())
            return null;

        return new UserCommand(line);
    }

    public synchronized boolean askChoice(String message) {
        System.out.println(message);
        System.out.print(COMMAND_LINE_START);
        String answer = scanner.nextLine();

        answer = answer.trim().toLowerCase();

        return answer.equals("si") || answer.equals("s");
    }

    @Override
    public void run() {

    }
}
