package info.kgeorgiy.ja.kadyrov.crawler;


import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Crawls websites.
 *
 * @author Kadyrov Rustam.
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaderService;
    private final ExecutorService extractorService;
    private final int perHosts;

    /**
     * Constructor for WebCrawler.
     *
     * @param downloader  allows you to download pages and extract links from them.
     * @param downloaders the maximum number of simultaneously downloaded pages.
     * @param extractors  the maximum number of pages from which links are simultaneously extracted.
     * @param perHost     the maximum number of pages that can be simultaneously loaded from one host.
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaderService = Executors.newFixedThreadPool(downloaders);
        this.extractorService = Executors.newFixedThreadPool(extractors);
        this.perHosts = perHost;
    }

    /**
     * Downloads website up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(String url, int depth) {
        Worker worker = new Worker();
        worker.runWorker(url, depth);
        return new Result(new ArrayList<>(worker.downloaded), worker.errors);
    }


    private class Worker {
        Set<String> downloaded = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Phaser phaser = new Phaser(1);
        Queue<String> curLinks = new ConcurrentLinkedDeque<>();
        Queue<String> nextLinks = new ConcurrentLinkedDeque<>();

        private void runWorker(String url, int depth) {
            visited.add(url);
            curLinks.add(url);
            for (int i = 0; i < depth; i++) {
                for (String link : curLinks) {
                    runDownloaderService(link, depth - i);
                }
                phaser.arriveAndAwaitAdvance();
                updateQueue();
            }
        }

        private void runDownloaderService(String link, int depth) {
            phaser.register();
            downloaderService.submit(() -> {
                try {
                    Document document = downloader.download(link);
                    downloaded.add(link);
                    if (depth > 1) {
                        runExtractorService(link, document);
                    }
                } catch (IOException e) {
                    errors.put(link, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }

        private void runExtractorService(String parentLink, Document document) {
            phaser.register();
            extractorService.submit(() -> {
                try {
                    for (String link : document.extractLinks()) {
                        if (visited.add(link)) {
                            nextLinks.add(link);
                        }
                    }
                } catch (IOException e) {
                    errors.put(parentLink, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }

        private void updateQueue() {
            curLinks.clear();
            curLinks.addAll(nextLinks);
            nextLinks.clear();
        }


    }


    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        downloaderService.shutdownNow();
        extractorService.shutdownNow();
    }

    /**
     * Allows you to run the traversal with WebCrawler from the command line.
     * Command line format: WebCrawler url [depth [downloads [extractors [perHost]]]]
     *
     * @param args First argument: URL - link to page.
     *             Second argument: depth - the maximum number of recursion depth when loading pages.
     *             Third argument: downloaders - the maximum number of simultaneously downloaded pages.
     *             Fourth argument: extractors - the maximum number of pages from which links are simultaneously extracted.
     *             Fifth argument: perHosts - the maximum number of pages that can be simultaneously loaded from one host.
     */

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 5) {
            System.err.println("Incorrect arguments passed. Please check correct input.");
            return;
        }
        try {
            int depth = getArg(args, 1, 1);
            int downloaders = getArg(args, 2, 1);
            int extractors = getArg(args, 3, 1);
            int perHost = getArg(args, 4, 1);
            try (WebCrawler crawler = new WebCrawler(new CachingDownloader(1.0), depth, downloaders, extractors)) {
                crawler.download(args[0], perHost);
            }
        } catch (IOException e) {
            System.err.println("Failed to running CashingDownloader.");
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse arguments, check input.");
        }
    }

    private static int getArg(String[] args, int index, int defaultValue) {
        return index > args.length ? defaultValue : Integer.parseInt(args[index]);
    }
}
