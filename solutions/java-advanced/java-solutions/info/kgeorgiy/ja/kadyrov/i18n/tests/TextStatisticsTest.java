package info.kgeorgiy.ja.kadyrov.i18n.tests;

import info.kgeorgiy.ja.kadyrov.i18n.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TextStatisticsTest {
    private TextStatistics textStatistics;

    private static final String NUMBERS = "C:\\Users\\hedwi\\Desktop\\tears of happiness\\java-advanced-2023-main\\" +
            "solutions\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\kadyrov\\i18n\\tests\\test_numbers";
    private static final String STRINGS = "C:\\Users\\hedwi\\Desktop\\tears of happiness\\java-advanced-2023-main\\" +
            "solutions\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\kadyrov\\i18n\\tests\\test_strings";

    @BeforeClass
    public static void beforeClass() {

    }

    @Test
    public void test1() {
        System.err.println("test_1::number_statistics.");
        try {
            String text = Files.readString(Path.of(NUMBERS));
            Locale locale = Locale.forLanguageTag("en-US");
            NumberStatistics numberStatistics = new NumberStatistics(text, locale);
            numberStatistics.getNumberStatistics();
            ResultStatistics result = numberStatistics.getResult();
            Assert.assertEquals(29, result.getNumberOfEntries());
            Assert.assertEquals(18, result.getNumberOfDistinctEntries());
            Assert.assertEquals(2023.0, result.getMaximumValue());
            Assert.assertEquals(-2000.0, result.getMinimumValue());
            Assert.assertEquals(612.6896551724138, result.getAverageValue());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        System.err.println("test_1::complete!!");
    }

    @Test
    public void test2() {
        System.err.println("test_2::sentence_statistics.");
        try {
            String text = Files.readString(Path.of(STRINGS));
            Locale locale = Locale.forLanguageTag("en-US");
            SentenceStatistics sentenceStatistics = new SentenceStatistics(text, locale);
            sentenceStatistics.getStringStatistics();
            ResultStatistics<String> result = sentenceStatistics.getResult();
            Assert.assertEquals(8, result.getNumberOfEntries());
            Assert.assertEquals(4, result.getNumberOfDistinctEntries());
            Assert.assertEquals("This sentence will be the longest in this file!", result.getMaximumValue().trim());
            Assert.assertEquals("Hello, world!", result.getMinimumValue().trim());
            Assert.assertEquals(49, result.getMaximumLength());
            Assert.assertEquals(15, result.getMinimumLength());
            Assert.assertEquals(32, result.getAverageLength());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        System.err.println("test_2::complete!!");
    }

    @Test
    public void test3() {
        System.err.println("test_3::word_statistics.");
        try {
            String text = Files.readString(Path.of(STRINGS));
            Locale locale = Locale.forLanguageTag("en-US");
            WordStatistics wordStatistics = new WordStatistics(text, locale);
            wordStatistics.getStringStatistics();
            ResultStatistics<String> result = wordStatistics.getResult();
            Assert.assertEquals(38, result.getNumberOfEntries());
            Assert.assertEquals(18, result.getNumberOfDistinctEntries());
            Assert.assertEquals("world", result.getMaximumValue());
            Assert.assertEquals("be", result.getMinimumValue());
            Assert.assertEquals(8, result.getMaximumLength());
            Assert.assertEquals(1, result.getMinimumLength());
            Assert.assertEquals(4, result.getAverageLength());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        System.err.println("test_3::complete!!");
    }

    @Test
    public void test4() {
        System.err.println("test_4::currencies_statistics.");
        try {
            String text = Files.readString(Path.of(NUMBERS));
            Locale locale = Locale.forLanguageTag("en-US");
            CurrencyStatistics currencyStatistics = new CurrencyStatistics(text, locale);
            currencyStatistics.getNumberStatistics();
            ResultStatistics<Number> result = currencyStatistics.getResult();
            Assert.assertEquals(13, result.getNumberOfEntries());
            Assert.assertEquals(9, result.getNumberOfDistinctEntries());
            Assert.assertEquals(10000.0, result.getMaximumValue());
            Assert.assertEquals(-10000.0, result.getMinimumValue());
            Assert.assertEquals(0.07692307692307693, result.getAverageValue());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        System.err.println("test_4::complete!!");
    }

    @Test
    public void test5() {
        System.err.println("test_5::dates_statistics.");
        try {
            String text = Files.readString(Path.of(NUMBERS));
            Locale locale = Locale.forLanguageTag("en-US");
            DateStatistics dateStatistics = new DateStatistics(text, locale);
            dateStatistics.getNumberStatistics();
            ResultStatistics<Date> result = dateStatistics.getResult();
            Assert.assertEquals(7, result.getNumberOfEntries());
            Assert.assertEquals(4, result.getNumberOfDistinctEntries());
            Assert.assertEquals("Fri Jun 02 00:00:00 MSK 2023", String.valueOf(result.getMaximumValue()));
            Assert.assertEquals("Mon Jun 18 00:00:00 MSK 1979", String.valueOf(result.getMinimumValue()));
            Assert.assertEquals("Fri Aug 04 21:25:42 MSD 2000", String.valueOf(result.getAverageValue()));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        System.err.println("test_4::complete!!");
    }

}
