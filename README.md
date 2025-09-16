# Bank Account Application

This application provides a simple banking system for managing customer accounts and currency conversions. It can be run in two modes:

**1. CLI (Command Line Interface) Mode:**
- Allows users to interact with the banking system via terminal commands.
- Supports account creation, deposits, withdrawals, balance inquiries, and currency conversion directly from the command line.
- Suitable for automation, scripting, or environments without a web interface.

**2. WEB (REST API) Mode:**
- Exposes a RESTful API for integration with web clients, mobile apps, or other systems.
- Endpoints are available for account management and currency conversion.
- Includes OpenAPI/Swagger documentation for easy exploration and integration.

---

## How to Select Application Mode
To choose how the application runs, set the following property in `src/main/resources/application.properties`:
- **CLI Mode:**
  ```properties
  spring.profiles.active=cli
  ```
  This will start the application in command-line mode.
- **WEB Mode:**
  ```properties
  spring.profiles.active=web
  ```
  This will start the application as a REST API server.
---

## Business Features

- **Account Management:**
  - Create new customer accounts with support for multiple currencies (USD, EUR, etc.).
  - Deposit and withdraw funds in different currencies, with automatic conversion using up-to-date exchange rates.
  - View account details and balances at any time.

- **Currency Conversion:**
  - Convert amounts between supported currencies using real-time or configured rates.
  - Integrated conversion logic for seamless multi-currency transactions.

- **Error Handling:**
  - Centralized exception handling ensures consistent and informative error responses for all operations.

- **Extensibility:**
  - Easily add new currencies, business rules, or integrations as needed.

---
