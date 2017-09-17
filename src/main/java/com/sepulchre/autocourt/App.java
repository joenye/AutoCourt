package com.sepulchre.autocourt;

import com.sepulchre.autocourt.model.Booking;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    private static Properties prop;
    private static WebDriver driver;

    public static void main(String[] args) throws IOException {

        setup();

        // Only log in at process start
        login();

//        System.out.println(format.format(dateNow.plusDays(7).minusDays(7).toLocalDate()
//                .atStartOfDay()));

        LocalDateTime time = LocalDateTime.parse("2017-09-20 7:00", Booking.formatter);

        Booking booking = new Booking(false, "3473", time, 1);
        System.out.println(booking.toString());
        Runnable task = () -> bookCourt(booking);

        ScheduledExecutorService execService = Executors.newScheduledThreadPool(1);
        execService.schedule(task, 5L, TimeUnit.SECONDS);
    }

    private static void setup() throws IOException {
        // Load Chrome driver
        ChromeDriverManager.getInstance().setup();
        driver = new ChromeDriver();

        // Load properties
        InputStream input = new FileInputStream("application.properties");
        prop = new Properties();
        prop.load(input);
        input.close();
    }

    /**
     * Logs in
     */
    private static void login() {
        driver.get("https://www.openplay.co.uk/booking/place/3473/login");

        driver.findElement(By.cssSelector("a[href='#login']")).click();

        WebDriverWait wait = new WebDriverWait(driver, 40);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginEmail")))
                .sendKeys(prop.getProperty("USERNAME"));
        driver.findElement(By.id("loginPassword")).sendKeys(prop.getProperty("PASSWORD"));
        driver.findElement(By.id("loginBtn")).submit();
    }

    /**
     * Books a court
     */
    private static void bookCourt(Booking booking) {

        String URL = "https://www.openplay.co" +
                ".uk/booking/place/" + booking.getLocationId() +
                "/pricing?start=" + booking.getStartTime().getHour() +
                ":00&end=" + (booking.getStartTime().getHour() + booking.getDuration()) +
                ":00&date=" + Booking.url_formatter.format(booking.getStartTime()) +
                "&resource_id=2571&use_id=42";

        driver.get(URL);
        driver.findElement(By.id("pricingOption")).click();
        driver.findElement(By.cssSelector("a[href='/booking/place/3473/login']")).click();
        // TODO: Check order before confirming
        driver.findElement(By.id("confirm-checkbox")).click();
        // driver.findElement(By.id("complete-order")).click();
    }

}
