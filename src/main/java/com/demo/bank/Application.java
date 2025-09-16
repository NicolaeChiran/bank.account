package com.demo.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import com.demo.bank.account.controller.CliController;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Profile("cli")
    @org.springframework.stereotype.Component
    static class CliRunner implements CommandLineRunner {

        private final CliController cliController;

        @Autowired
        public CliRunner(CliController cliController) {
            this.cliController = cliController;
        }

        @Override
        public void run(String... args) {
            Scanner scanner = new Scanner(System.in);
            log.info("Welcome to the Bank Account CLI!");

            while (true) {
                log.info("Enter command: ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("Quit")) {
                    log.info("Goodbye!");
                    break;
                }

                String[] parts = input.split("\\s+");
                if (parts.length == 0) continue;

                String command = parts[0];

                try {
                    switch (command) {
                        case "NewAccount":
                            cliController.handleNewAccount(parts);
                            break;
                        case "Deposit":
                            cliController.handleDeposit(parts);
                            break;
                        case "Withdraw":
                            cliController.handleWithdraw(parts);
                            break;
                        case "Balance":
                            cliController.handleBalance(parts);
                            break;
                        case "Convert":
                            cliController.handleConvert(parts);
                            break;
                        default:
                            cliController.handleUnknownCommand();
                    }
                } catch (NumberFormatException e) {
                    log.error("Invalid number format.");
                } catch (Exception e) {
                    log.error("Error: {}", e.getMessage());
                }
            }
        }
    }
}
