package com.sepulchre.autocourt;

import com.sepulchre.autocourt.api.API;
import com.sepulchre.autocourt.model.Booking;
import com.sepulchre.autocourt.utils.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.net.URL;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static Properties prop;
    private static WebDriver driver;
    private static Map<Booking, ScheduledFuture> activeBookings = new HashMap<>();
    private static String saveFilePath;

    private static boolean isLiveMode = false;
    private static boolean isHeadless = true;

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchFieldException {
        setup(args);

        // Only log in at process start
        login();
    }


    private static void setup(String[] args) throws IOException, NoSuchFieldException {
        // Load properties
        InputStream input = App.class.getResourceAsStream("/config/application.properties");
        prop = new Properties();
        prop.load(input);
        input.close();
        saveFilePath = prop.getProperty("SAVE_FILE_PATH");

        if (args.length > 0) {
            isLiveMode = (Objects.equals(args[0].toLowerCase(), "--live"));
            if (args.length > 1) {
                isHeadless = (Objects.equals(args[1].toLowerCase(), "--headless"));
            }
        }
        if (isLiveMode) {
            logger.warn("CAUTION: Live mode is enabled. Bookings will be confirmed and " +
                    "real money shall be spent.");
        } else {
            logger.info("Live mode is not enabled. Bookings will not be confirmed.");
        }

	logger.info("Connecting to driver... (restart driver if this hangs)");

	ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        // options.addArguments("--disable-gpu");
        // options.addArguments("--remote-debugin-port=9222");
        // options.addArguments("--screen-size=1200x800");
	
	//DesiredCapabilities cap = DesiredCapabilities.chrome();
	//FirefoxOptions options = new FirefoxOptions();
	//DesiredCapabilities dc = new DesiredCapabilities();
	//dc.setBrowserName(DesiredCapabilities.firefox().getBrowserName());
	
	//cap.setCapability(FirefoxOptions.CAPABILITY, chromeOptions);
	driver = new RemoteWebDriver(new URL("http://172.18.0.1:4444/wd/hub"), options);
        
        // if (isHeadless) {
        //     options.addArguments("--headless");
        //     logger.warn("Chrome headless mode is enabled.");
        // } else {
        //     logger.info("Chrome headless mode is NOT enabled.");
        // }

        // Configure HTTP endpoints
        API.createEndPoints();

	logger.info("Created endpoints");

        // Load active bookings
        List<Booking> bookings = FileUtils.loadBookingsFromFile(saveFilePath);
        if (bookings != null) {
            for (Booking b : bookings) {
                addBooking(b);
            }
	    logger.info("Loaded bookings");
        }
    }

    public static void addBooking(Booking booking) {
        if (!activeBookings.containsKey(booking)) {
            LocalDateTime schedulingTime = booking.getStartTime().minusDays(7).toLocalDate()
                    .atStartOfDay();

            Runnable task = () -> {
                try {
                    bookCourt(booking);
                } catch (InterruptedException e) {
                    logger.warn("Failed to book court for booking: " + booking.toString());
                    e.printStackTrace();
                }
            };

            ScheduledExecutorService execService = Executors.newScheduledThreadPool(1);

            logger.info("New booking added to active bookings: " + booking.toString());
//            if (LocalDateTime.now().until(schedulingTime.minusSeconds(0), ChronoUnit.SECONDS) < 0) {
//                logger.warn("This booking can already be made online. Booking" +
//                        " has not been added to scheduler.");
//                return;
//            }

            ScheduledFuture bookingFuture = execService.schedule(task, LocalDateTime.now()
                    .until(schedulingTime.minusSeconds(0), ChronoUnit.SECONDS), TimeUnit
                    .SECONDS);
            activeBookings.put(booking, bookingFuture);
            logger.info(booking.toString() + " has been scheduled to run at " + Booking
                    .API_URL_FORMATTER
                    .format(schedulingTime.plusSeconds(1)));
            FileUtils.saveBookingsToFile(new ArrayList<>(activeBookings.keySet()), saveFilePath);
        }
    }

    public static boolean isLiveMode() {
        return isLiveMode;
    }

    public static List<Booking> getBookings() {
        List<Booking> bookings = new ArrayList<>(activeBookings.keySet());
        bookings.sort(Comparator.comparing(Booking::getStartTime));
        return bookings;
    }

    public static void cancelBooking(UUID booking_id) {
        ScheduledFuture future = activeBookings.get(new Booking(booking_id));
        future.cancel(true);
        activeBookings.remove(new Booking(booking_id));
        FileUtils.saveBookingsToFile(new ArrayList<>(activeBookings.keySet()), saveFilePath);
    }

    private static void login() throws InterruptedException {
        driver.get("https://www.openplay.co.uk/booking/place/3473/login");

        WebDriverWait wait = new WebDriverWait(driver, 40);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector
                ("a[href='#login']"))).click();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginEmail")))
                .sendKeys(prop.getProperty("USERNAME"));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginPassword")))
                .sendKeys(prop.getProperty("PASSWORD"));
        Thread.sleep(2000);
        wait.until(ExpectedConditions.elementToBeClickable(By.id
                ("loginBtn"))).submit();
	logger.info("Logged in successfully");
    }

    private static void bookCourt(Booking booking) throws InterruptedException {
        activeBookings.remove(booking);
        if (activeBookings.size() > 0)
            FileUtils.saveBookingsToFile(new ArrayList<>(activeBookings.keySet()), saveFilePath);

        logger.info("Attempting to book court for " + booking.getDuration() + " hour" +
                "(s) from " + Booking.API_URL_FORMATTER.format(booking.getStartTime()));

        String URL = "https://www.openplay.co" +
                ".uk/booking/place/" + booking.getLocation().id +
                "/pricing?start=" + String.format("%02d", booking.getStartTime().getHour()) +
                ":00&end=" + String.format("%02d", booking.getStartTime().getHour() + booking.getDuration()) +
                ":00&date=" + Booking.CLISSOLD_URL_FORMATTER.format(booking.getStartTime()) +
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

        Thread.sleep(1000);
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.pollingEvery(5000, TimeUnit.MILLISECONDS);
        wait.withTimeout(60 * 3, TimeUnit.SECONDS);
        wait.ignoring(NoSuchElementException.class);
        wait.until(function);
        driver.findElement(By.cssSelector("a[href='/booking/place/3473/login']")).click();

        Thread.sleep(1000);
        driver.findElement(By.id("confirm-checkbox")).click();

        if (isLiveMode) {
            logger.info("Confirming order...");
            Thread.sleep(1000);
            driver.findElement(By.id("complete-order")).click();
        } else {
            logger.info("Not confirming order as live mode is not enabled.");
        }
    }

}
