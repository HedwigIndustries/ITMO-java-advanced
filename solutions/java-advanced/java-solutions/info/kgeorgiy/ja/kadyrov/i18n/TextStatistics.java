package info.kgeorgiy.ja.kadyrov.i18n;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class TextStatistics {
    public static void main(String[] args) {
        Locale textLocale = Locale.forLanguageTag(args[0]);
        Locale outputLocale = Locale.forLanguageTag(args[1]);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(textLocale);
        String inputFile = args[2];
        String outputFile = args[3];

        try {
            String text = Files.readString(Path.of(inputFile));
            ResultStatistics<String> resultSentenceStatistics = getSentenceResultStatistics(textLocale, text);
            ResultStatistics<String> resultWordStatistics = getWordResultStatistics(textLocale, text);
            ResultStatistics<Double> resultNumberStatistics = getNumberResultStatistics(textLocale, text);
            ResultStatistics<Number> resultCurrencyStatistics = getCurrencyResultStatistics(textLocale, text);
            ResultStatistics<Date> resultDateStatistics = getDateResultStatistics(textLocale, text);
            ResourceBundle bundle = ResourceBundle.getBundle("info/kgeorgiy/ja/kadyrov/i18n/Bundle", outputLocale);
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFile), StandardCharsets.UTF_8)) {
                writeReport(inputFile, resultSentenceStatistics, resultWordStatistics, resultNumberStatistics, resultCurrencyStatistics, resultDateStatistics, bundle, writer, currencyFormat);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResultStatistics<String> getSentenceResultStatistics(Locale textLocale, String text) {
        SentenceStatistics sentenceStatistics = new SentenceStatistics(text, textLocale);
        sentenceStatistics.getStringStatistics();
        return sentenceStatistics.getResult();
    }

    private static ResultStatistics<String> getWordResultStatistics(Locale textLocale, String text) {
        WordStatistics wordStatistics = new WordStatistics(text, textLocale);
        wordStatistics.getStringStatistics();
        return wordStatistics.getResult();
    }

    private static ResultStatistics<Double> getNumberResultStatistics(Locale textLocale, String text) {
        NumberStatistics numberStatistics = new NumberStatistics(text, textLocale);
        numberStatistics.getNumberStatistics();
        return numberStatistics.getResult();
    }

    private static ResultStatistics<Number> getCurrencyResultStatistics(Locale textLocale, String text) {
        CurrencyStatistics currencyStatistics = new CurrencyStatistics(text, textLocale);
        currencyStatistics.getNumberStatistics();
        return currencyStatistics.getResult();
    }

    private static ResultStatistics<Date> getDateResultStatistics(Locale textLocale, String text) {
        DateStatistics dateStatistics = new DateStatistics(text, textLocale);
        dateStatistics.getNumberStatistics();
        return dateStatistics.getResult();
    }


    private static void writeReport(String inputFile, ResultStatistics<String> resultSentenceStatistics, ResultStatistics<String> resultWordStatistics, ResultStatistics<Double> resultNumberStatistics, ResultStatistics<Number> resultCurrencyStatistics, ResultStatistics<Date> resultDateStatistics, ResourceBundle bundle, BufferedWriter writer, NumberFormat currencyFormat) throws IOException {
        writeSummary(inputFile, resultSentenceStatistics, resultWordStatistics, resultNumberStatistics, resultDateStatistics, bundle, writer);
        writeSentenceStatistics(resultSentenceStatistics, bundle, writer);
        writeWordStatistics(resultWordStatistics, bundle, writer);

        writeBaseStatistics(resultNumberStatistics, bundle, writer, "number");
        writeCurrencyStatistics(resultCurrencyStatistics, bundle, writer, currencyFormat);
        writeBaseStatistics(resultDateStatistics, bundle, writer, "date");
    }

    private static void writeSummary(String inputFile, ResultStatistics<String> resultSentenceStatistics, ResultStatistics<String> resultWordStatistics, ResultStatistics<Double> resultNumberStatistics, ResultStatistics<Date> resultDateStatistics, ResourceBundle bundle, BufferedWriter writer) throws IOException {
        writer.write(bundle.getString("file_name") + " " + "\"" + inputFile + "\"");
        writer.newLine();
        writer.write(bundle.getString("summary_statistics"));
        writer.newLine();
        writer.write("\t" + bundle.getString("num_sentences") + " " + resultSentenceStatistics.numberOfEntries);
        writer.newLine();
        writer.write("\t" + bundle.getString("num_words") + " " + resultWordStatistics.numberOfEntries);
        writer.newLine();
        writer.write("\t" + bundle.getString("num_numbers") + " " + resultNumberStatistics.numberOfEntries);
        writer.newLine();
        writer.write("\t" + bundle.getString("num_dates") + " " + resultDateStatistics.numberOfEntries);
        writer.newLine();
    }

    private static void writeSentenceStatistics(ResultStatistics<String> resultSentenceStatistics, ResourceBundle bundle, BufferedWriter writer) throws IOException {
        writer.write(bundle.getString("sentence_statistics"));
        writer.newLine();
        writer.write("\t" + bundle.getString("num_sentences") + " " + resultSentenceStatistics.numberOfEntries + " " +
                "(" + bundle.getString("unique") + " " + resultSentenceStatistics.numberOfDistinctEntries + ")");
        writer.newLine();
        writer.write("\t" + bundle.getString("min_sentence") + " " + resultSentenceStatistics.minimumValue);
        writer.newLine();
        writer.write("\t" + bundle.getString("max_sentence") + " " + resultSentenceStatistics.maximumValue);
        writer.newLine();
        writer.write("\t" + bundle.getString("min_sentence_length") + " " + resultSentenceStatistics.minimumLength);
        writer.newLine();
        writer.write("\t" + bundle.getString("max_sentence_length") + " " + resultSentenceStatistics.maximumLength);
        writer.newLine();
        writer.write("\t" + bundle.getString("avg_sentence_length") + " " + resultSentenceStatistics.averageLength);
        writer.newLine();
    }

    private static void writeWordStatistics(ResultStatistics<String> resultWordStatistics, ResourceBundle bundle, BufferedWriter writer) throws IOException {
        writer.write(bundle.getString("word_statistics"));
        writer.newLine();
        writer.write("\t" + bundle.getString("num_words") + " " + resultWordStatistics.numberOfEntries + " " +
                "(" + bundle.getString("unique") + " " + resultWordStatistics.numberOfDistinctEntries + ")");
        writer.newLine();
        writer.write("\t" + bundle.getString("min_word") + " " + resultWordStatistics.minimumValue);
        writer.newLine();
        writer.write("\t" + bundle.getString("max_word") + " " + resultWordStatistics.maximumValue);
        writer.newLine();
        writer.write("\t" + bundle.getString("min_word_length") + " " + resultWordStatistics.minimumLength);
        writer.newLine();
        writer.write("\t" + bundle.getString("max_word_length") + " " + resultWordStatistics.maximumLength);
        writer.newLine();
        writer.write("\t" + bundle.getString("avg_word_length") + " " + resultWordStatistics.averageLength);
        writer.newLine();
    }

    private static void writeNumberStatistics(ResultStatistics<Double> resultNumberStatistics, ResourceBundle bundle, BufferedWriter writer) throws IOException {
        writer.write(bundle.getString("number_statistics"));
        writer.newLine();
        writer.write("\t" + bundle.getString("num_numbers") + " " + resultNumberStatistics.numberOfEntries + " " +
                "(" + bundle.getString("unique") + " " + resultNumberStatistics.numberOfDistinctEntries + ")");
        writer.newLine();
        writer.write("\t" + bundle.getString("min_number") + " " + resultNumberStatistics.minimumValue);
        writer.newLine();
        writer.write("\t" + bundle.getString("max_number") + " " + resultNumberStatistics.maximumValue);
        writer.newLine();
        writer.write("\t" + bundle.getString("avg_number") + " " + resultNumberStatistics.averageValue);
        writer.newLine();
    }

    private static void writeCurrencyStatistics(ResultStatistics<Number> resultCurrencyStatistics, ResourceBundle bundle, BufferedWriter writer, NumberFormat currencyFormat) throws IOException {
        writer.write(bundle.getString("money_sum_statistics"));
        writer.newLine();
        writer.write("\t" + bundle.getString("num_money_sums") + " " + resultCurrencyStatistics.numberOfEntries + " " +
                "(" + bundle.getString("unique") + " " + resultCurrencyStatistics.numberOfDistinctEntries + ")");
        writer.newLine();
        writer.write("\t" + bundle.getString("min_money_sum") + " " + currencyFormat.format(resultCurrencyStatistics.minimumValue));
        writer.newLine();
        writer.write("\t" + bundle.getString("max_money_sum") + " " + currencyFormat.format(resultCurrencyStatistics.maximumValue));
        writer.newLine();
        writer.write("\t" + bundle.getString("avg_money_sum") + " " + currencyFormat.format(resultCurrencyStatistics.averageValue));
        writer.newLine();
    }

    private static <T> void writeBaseStatistics(ResultStatistics<T> resultDateStatistics, ResourceBundle bundle,
                                                BufferedWriter writer, String key) throws IOException {
        writer.write(bundle.getString(key + "_statistics"));
        writer.newLine();
        writer.write("\t" + bundle.getString("num_" + key + "s") + " " + resultDateStatistics.numberOfEntries + " " +
                "(" + bundle.getString("unique") + " " + resultDateStatistics.numberOfDistinctEntries + ")");
        writer.newLine();
        writer.write("\t" + bundle.getString("min_" + key) + " " + resultDateStatistics.minimumValue);
        writer.newLine();
        writer.write("\t" + bundle.getString("max_" + key) + " " + resultDateStatistics.maximumValue);
        writer.newLine();
        writer.write("\t" + bundle.getString("avg_" + key) + " " + resultDateStatistics.averageValue);
        writer.newLine();
    }


}
