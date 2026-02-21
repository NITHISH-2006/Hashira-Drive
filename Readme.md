# Placement Assignment — Shamir's Secret Sharing Implementation

**Language:** Java | **Algorithm:** Lagrange Interpolation | **JDK:** 8+

## Overview
This repository provides a robust Java solution for recovering a secret integer—the constant term of an unknown polynomial—using a simplified variant of Shamir's Secret Sharing scheme. Given a set of encoded `(x, y)` points from a JSON file, the program decodes each y-value from its declared numerical base, applies exact-precision Lagrange interpolation, and returns the secret.

The implementation is intentionally self-contained, uses no external libraries, and handles malformed or corrupt share sets by evaluating all valid combinations and selecting the majority result.

## Problem Statement
A secret integer is hidden as the constant term `c` of a polynomial of degree `m`. To recover `c`, you need at least `k = m + 1` distinct points that lie on the polynomial.

Each point is provided in a JSON file where:
* `x` is the numeric key of the JSON object (e.g., `"3"`).
* `y` is the value encoded in an arbitrary base (binary, octal, hexadecimal, etc.).
* `k` (minimum roots required) is declared in the keys object.

The goal is to evaluate the polynomial at x = 0, which directly yields `c`.

## Key Features
* **Zero External Dependencies:** Built entirely with native Java libraries. JSON parsing is handled through `java.util.regex`, eliminating the need for Gson, Jackson, or any third-party parser.
* **Format-Agnostic JSON Parsing:** The custom regex-based parser handles any valid JSON formatting—minified single-line, multi-line with whitespace, or keys declared in arbitrary order.
* **Exact Precision Arithmetic:** All interpolation is performed using `BigInteger` rational arithmetic (numerator/denominator pairs with GCD reduction at every step). This completely avoids the catastrophic rounding errors that occur with `double` floating-point math when dealing with large polynomials.
* **Corrupt Share Detection:** When more shares are provided than the minimum required (`n > k`), the program evaluates all possible combinations of `k` shares and returns the result that the majority of combinations agree on—automatically discarding any corrupt or tampered data points.

## How It Works

### 1. JSON Parsing
The program scans the JSON for all numeric-keyed objects and extracts three fields from each: the x-coordinate (the key itself), the base, and the encoded value string.

```json
"4": {    
    "base": "16",    
    "value": "e1b5e05623d881f"
}
// Decoded: x = 4,  y = new BigInteger("e1b5e05623d881f", 16)

2. Base DecodingEach y-value string is decoded from its declared base into a BigInteger using Java's built-in new BigInteger(value, base) constructor, which natively supports any base from 2 to 36.3. Lagrange Interpolation at x = 0The secret is recovered by evaluating the interpolating polynomial at x = 0 using the Lagrange formula:$$f(0) = \sum_{j} \left[ y_j \prod_{i \neq j} \frac{0 - x_i}{x_j - x_i} \right]$$Each term is computed as an exact fraction, reduced by GCD, with the sign of the denominator normalized to positive before accumulation. The final result is always a whole integer for a valid, uncorrupted share set.4. Combination Voting (Corrupt Share Handling)If n > k, all combinations of k elements out of n are evaluated. Results that are non-integers are discarded. The most frequently occurring integer is returned as the secret, effectively outvoting any corrupt shares.How to RunPrerequisitesJava Development Kit (JDK) 8 or higher (No other dependencies required).ExecutionCompile the program:Bashjavac Hashiraq.java
Run the program (accepts one or multiple files):Bash# Single file
java Hashiraq testcase1.json

# Multiple files in one command
java Hashiraq testcase1.json testcase2.json
Expected OutputPlaintextProcessing testcase1.json...
Recovered Secret: 3
-------------------------
Processing testcase2.json...
Recovered Secret: 79836264049851
-------------------------
Test CasesFilenkSecretNotestestcase1.json433All shares valid.testcase2.json107798362640498512 corrupt shares (x=2, x=8) — auto-detected and discarded.Repository StructurePlaintext.
├── Hashiraq.java        # Main source file
├── testcase1.json       # Sample test case 1 (n=4, k=3)
├── testcase2.json       # Sample test case 2 (n=10, k=7)
└── README.md            # This file
Design DecisionsWhy BigInteger over double?The y-values in the test cases exceed the range of a 64-bit long and lose precision with double-precision floating point. BigInteger guarantees exact arithmetic at any scale—the final division of the interpolation result is always exact (remainder zero) for a valid polynomial.Why regex over a JSON library?The assignment prohibits Python and discourages external dependencies. A targeted regex approach is sufficient for the well-defined schema used in these test cases and eliminates all library version or classpath concerns.Why evaluate all combinations?The problem provides n >= k shares, implying the extra shares may be decoys. Rather than assuming the first k shares are always valid, the combination approach is mathematically rigorous and resilient to any subset of corrupt inputs, provided fewer than k shares are corrupted.