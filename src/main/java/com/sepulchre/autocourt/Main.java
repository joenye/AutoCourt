package com.sepulchre.autocourt;

import com.sepulchre.autocourt.model.Booking;
import com.sepulchre.autocourt.utils.FileUtils;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static Properties prop;
    private static WebDriver driver;
    private static Map<Booking, ScheduledFuture> activeBookings = new HashMap<>();
    private static String saveFilePath;
    private static boolean liveMode = false;

    public static void main(String[] args) throws IOException {
        setup(args);

        // Only log in at process start
        login();

        // Add test booking
        LocalDateTime time = LocalDateTime.parse("2017-09-25 07:00", Booking.formatter);
        Booking booking = new Booking(false, "3473", time, 1);
        addBooking(booking);
    }


    private static void setup(String[] args) throws IOException {
        // Load Chrome driver
        ChromeDriverManager.getInstance().setup();
        driver = new ChromeDriver();

        // Load properties
        InputStream input = new FileInputStream("config/application.properties");
        prop = new Properties();
        prop.load(input);
        input.close();

        if (args.length > 0)
            liveMode = Boolean.parseBoolean(args[0]);
        if (!liveMode) {
            logger.warn("Live mode is not enabled. Bookings will not be confirmed.");
        }

        saveFilePath = prop.getProperty("SAVE_FILE_PATH");

        // Load active bookings
        List<Booking> bookings = FileUtils.loadBookingsFromFile(saveFilePath);
        if (bookings != null) {
            for (Booking b : bookings) {
                addBooking(b);
            }
        }
    }

    private static void addBooking(Booking booking) {
        //TODO: return error to client when handling duplicate bookings
        if (!activeBookings.containsKey(booking)) {
            LocalDateTime schedulingTime = booking.getStartTime().minusDays(7).toLocalDate()
                    .atStartOfDay();

            Runnable task = () -> bookCourt(booking);
            ScheduledExecutorService execService = Executors.newScheduledThreadPool(1);

            ScheduledFuture bookingFuture = execService.schedule(task, LocalDateTime.now()
                    .until(schedulingTime.minusSeconds(5), ChronoUnit.SECONDS), TimeUnit
                    .SECONDS);
            activeBookings.put(booking, bookingFuture);
            logger.info(booking.toString() + " has been scheduled to run at " + Booking
                    .long_formatter
                    .format(schedulingTime.minusSeconds(5)));
            FileUtils.saveBookingsToFile(activeBookings, saveFilePath);
        }
    }

    private static void cancelBooking(Booking booking) {
        ScheduledFuture future = activeBookings.get(booking);
        future.cancel(true);
        activeBookings.remove(booking);
        FileUtils.saveBookingsToFile(activeBookings, saveFilePath);
    }

    private static void login() {
        driver.get("https://www.openplay.co.uk/booking/place/3473/login");

        WebDriverWait wait = new WebDriverWait(driver, 40);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector
                ("a[href='#login']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginEmail")))
                .sendKeys(prop.getProperty("USERNAME"));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginPassword")))
                .sendKeys(prop.getProperty("PASSWORD"));
        wait.until(ExpectedConditions.elementToBeClickable(By.id
                ("loginBtn"))).submit();
    }

    private static void bookCourt(Booking booking) {
        logger.info("Booking court for " + booking.getDuration() + " hour(s), from time " +
                Booking.formatter.format(booking.getStartTime()));

        activeBookings.remove(booking);
        FileUtils.saveBookingsToFile(activeBookings, saveFilePath);

        String URL = "https://www.openplay.co" +
                ".uk/booking/place/" + booking.getLocationId() +
                "/pricing?start=" + String.format("%02d", booking.getStartTime().getHour()) +
                ":00&end=" + String.format("%02d", booking.getStartTime().getHour() + booking.getDuration()) +
                ":00&date=" + Booking.url_formatter.format(booking.getStartTime()) +
                "&resource_id=2571&use_id=42";

        /* Keep refreshing until booking is ready */
        Function<WebDriver, WebElement> function = driver -> {
            // 1. Refresh page
            driver.get(URL);

            FluentWait<WebDriver> wait = new FluentWait<>(driver);
            // 2. Click "Standard £0.00" button
            wait.until(ExpectedConditions.elementToBeClickable(By.id("pricingOption"))).click();
            // 3. Check if next button exists
            return driver.findElement(By.cssSelector
                    ("a[href='/booking/place/3473/login']"));
        };

        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.pollingEvery(1000, TimeUnit.MILLISECONDS);
        wait.withTimeout(60 * 3, TimeUnit.SECONDS);
        wait.ignoring(NoSuchElementException.class);
        wait.until(function);
        driver.findElement(By.cssSelector("a[href='/booking/place/3473/login']")).click();

        driver.findElement(By.id("confirm-checkbox")).click();

        /* CAUTION: Only uncomment when ready */
        // TODO: Check order before confirming
        if (liveMode) {
            logger.info("Confirming order...");
            driver.findElement(By.id("complete-order")).click();
        } else {
            logger.info("Not confirming order as live mode is not enabled...");
        }
    }

}
