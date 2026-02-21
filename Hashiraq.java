import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hashiraq {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No input files provided.");
            return;
        }

        for (String file : args) {
            try {
                System.out.println("Processing " + file + "...");
                String jsonInput = new String(Files.readAllBytes(Paths.get(file)));
                
                Object[] parsed = parseJSON(jsonInput);
                if (parsed != null) {
                    int k = (int) parsed[0];
                    @SuppressWarnings("unchecked")
                    List<BigInteger[]> allShares = (List<BigInteger[]>) parsed[1];
                    
                    // Filter out the decoys by checking combinations
                    BigInteger secret = findSecretWithCombinations(allShares, k);
                    if (secret != null) {
                        System.out.println("Recovered Secret: " + secret);
                    } else {
                        System.out.println("Failed to find a valid positive integer secret.");
                    }
                }
                System.out.println("-------------------------");
            } catch (IOException e) {
                System.out.println("Could not read " + file);
            }
        }
    }

    static Object[] parseJSON(String json) {
        int k = 0;
        Matcher kMatcher = Pattern.compile("\"k\"\\s*:\\s*(\\d+)").matcher(json);
        if (kMatcher.find()) {
            k = Integer.parseInt(kMatcher.group(1));
        } else {
            return null;
        }

        List<BigInteger[]> allShares = new ArrayList<>();
        Pattern rootPattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher rootMatcher = rootPattern.matcher(json);

        while (rootMatcher.find()) {
            String xStr = rootMatcher.group(1);
            String contentBlock = rootMatcher.group(2);

            Matcher baseMatcher = Pattern.compile("\"base\"\\s*:\\s*\"(\\d+)\"").matcher(contentBlock);
            Matcher valueMatcher = Pattern.compile("\"value\"\\s*:\\s*\"([a-zA-Z0-9]+)\"").matcher(contentBlock);

            if (baseMatcher.find() && valueMatcher.find()) {
                BigInteger x = new BigInteger(xStr);
                int base = Integer.parseInt(baseMatcher.group(1));
                String valueStr = valueMatcher.group(1);

                BigInteger y = new BigInteger(valueStr, base);
                allShares.add(new BigInteger[]{x, y});
            }
        }

        allShares.sort((a, b) -> a[0].compareTo(b[0]));
        return new Object[]{k, allShares};
    }

    static BigInteger findSecretWithCombinations(List<BigInteger[]> allShares, int k) {
        List<List<BigInteger[]>> combinations = new ArrayList<>();
        generateCombinations(allShares, k, 0, new ArrayList<>(), combinations);

        Map<BigInteger, Integer> secretCounts = new HashMap<>();

        for (List<BigInteger[]> combo : combinations) {
            BigInteger[][] shares = combo.toArray(new BigInteger[0][0]);
            BigInteger c = reconstruct(shares);
            
            // The problem constraints dictate the secret 'c' must be a positive integer
            if (c != null && c.compareTo(BigInteger.ZERO) > 0) {
                secretCounts.put(c, secretCounts.getOrDefault(c, 0) + 1);
            }
        }

        if (secretCounts.isEmpty()) return null;

        // If there are multiple valid points, the most frequent positive integer is the true secret
        BigInteger bestSecret = null;
        int maxCount = -1;
        for (Map.Entry<BigInteger, Integer> entry : secretCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestSecret = entry.getKey();
            }
        }

        return bestSecret;
    }

    static void generateCombinations(List<BigInteger[]> list, int k, int start, List<BigInteger[]> current, List<List<BigInteger[]>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            generateCombinations(list, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    static BigInteger reconstruct(BigInteger[][] shares) {
    int k = shares.length;
    BigInteger num = BigInteger.ZERO;
    BigInteger den = BigInteger.ONE;

    for (int j = 0; j < k; j++) {
        BigInteger xj = shares[j][0];
        BigInteger yj = shares[j][1];

        BigInteger bNum = BigInteger.ONE;
        BigInteger bDen = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            if (i == j) continue;
            BigInteger xi = shares[i][0];
            bNum = bNum.multiply(xi.negate());
            bDen = bDen.multiply(xj.subtract(xi));
        }

        BigInteger tNum = yj.multiply(bNum);
        BigInteger tDen = bDen;
        BigInteger g = tNum.gcd(tDen);
        tNum = tNum.divide(g);
        tDen = tDen.divide(g);
        if (tDen.signum() < 0) { tNum = tNum.negate(); tDen = tDen.negate(); }

        num = num.multiply(tDen).add(tNum.multiply(den));
        den = den.multiply(tDen);
        g = num.gcd(den);
        num = num.divide(g);
        den = den.divide(g);
        if (den.signum() < 0) { num = num.negate(); den = den.negate(); }
    }

    BigInteger[] dr = num.divideAndRemainder(den);
    if (!dr[1].equals(BigInteger.ZERO)) return null;
    return dr[0];
}
}