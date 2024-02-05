package info.kgeorgiy.ja.kadyrov.walk;


import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Walk {
    private static String hashingFile(String str, MessageDigest md) {
        byte[] result = new byte[1024];
        try (InputStream reader = new FileInputStream(str)) {
            int c = 0;
            while ((c = reader.read(result)) >= 0) {
                md.update(result, 0, c);
            }
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
        } catch (IOException | SecurityException e) {
            return "0".repeat(64);
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect arguments");
            return;
        }
        try {
            Path input = Paths.get(args[0]);
            Path output = Paths.get(args[1]);
            try {
                Path parent = output.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
            } catch (IOException | SecurityException e) {
                System.err.println("Cannot creating dirs" + " " + e.getMessage());
                return;
            }
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
                    try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
                        String line = reader.readLine();
                        while (line != null) {
                            String hash = hashingFile(line, md);
                            writer.write(hash + " " + line);
                            writer.newLine();
                            line = reader.readLine();
                        }
                    } catch (SecurityException e) {
                        System.err.println("Security error while writing a file" + " " + e.getMessage());
                    } catch (IOException e) {
                        System.err.println("An error occurs while writing a file" + " " + e.getMessage());
                    }
                } catch (SecurityException e) {
                    System.err.println("Security error while reading a file" + " " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("An error occurs while reading a file" + " " + e.getMessage());
                }
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Incorrect Algorithm");
            }
        } catch (InvalidPathException e) {
            System.err.println("Encountered an invalid character." + " " + e.getMessage());
        }
    }
}


