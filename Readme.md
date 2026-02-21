# Shamir's Secret Sharing (Simplified) - Polynomial Constant Solver

This repository contains a robust Java solution to find the constant term (the "secret") of an unknown polynomial using a simplified version of Shamir's Secret Sharing algorithm. 

## 📌 Problem Statement
Given a set of roots in a JSON file where each root is an `(x, y)` coordinate and the `y` values are encoded in various numerical bases (e.g., base 2, base 10, base 16), the goal is to:
1. Parse the JSON dynamically to extract the minimum number of roots (`k`) required.
2. Decode the `y` values from their respective bases into base-10 integers.
3. Calculate the constant term `c` (which represents the secret) of the polynomial evaluated at `x = 0`.

## ✨ Key Features
* **Zero External Dependencies:** Built entirely using native Java libraries (`java.util.regex`). No need for external JSON parsers like Gson or Jackson.
* **Format-Agnostic Parsing:** The custom Regex parser handles any JSON formatting—whether it's minified on a single line, spans multiple lines, or has the keys out of order.
* **Exact Precision Math:** Uses `BigInteger` fractional arithmetic for Lagrange interpolation to guarantee 100% precision, completely avoiding the catastrophic rounding errors common with standard `double` floating-point math.

## 🚀 How to Run

### Prerequisites
* **Java Development Kit (JDK):** Java 8 or higher.

### Execution
The program accepts the JSON test case files via command-line arguments, making it highly flexible for automated testing environments.

1. Open your terminal and navigate to the project directory.
2. Compile the Java source file:
   ```bash
   javac Hashiraq.java