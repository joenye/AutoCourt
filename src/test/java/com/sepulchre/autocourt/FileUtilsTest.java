package com.sepulchre.autocourt;

import com.sepulchre.autocourt.model.Booking;
import com.sepulchre.autocourt.utils.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static org.junit.Assert.assertTrue;

public class FileUtilsTest {

    private static String saveFilePath;

    @Before
    public void before() throws IOException {
        // Load properties
        InputStream input = new FileInputStream("config/application-test.properties");
        Properties prop = new Properties();
        prop.load(input);
        input.close();
        saveFilePath = prop.getProperty("SAVE_FILE_PATH");
    }

    @After
    public void after() {
        File file = new File(saveFilePath);
        file.delete();
    }

    @Test
    public void testSaveAndLoadBookingsToAndFromFile() {

        // Save bookings
        Booking booking1 = new Booking(true, "1000", LocalDateTime.now(), 1);
        Booking booking2 = new Booking(false, "500", LocalDateTime.now().plusDays(1), 2);
        Map<Booking, ScheduledFuture> bookingsToSave = new LinkedHashMap<>();

        bookingsToSave.put(booking1, null);
        bookingsToSave.put(booking2, null);

        FileUtils.saveBookingsToFile(bookingsToSave, saveFilePath);

        // Load bookings
        List<Booking> bookingsToLoad = FileUtils.loadBookingsFromFile(saveFilePath);

        // Assertions
        assert bookingsToLoad != null;
        assertTrue(bookingsToLoad.size() == 2);

        assertTrue(bookingsToLoad.get(0).isRecurrent());
        assertTrue(Objects.equals(bookingsToLoad.get(0).getLocationId(), "1000"));
        assertTrue(bookingsToLoad.get(0).getStartTime().getDayOfYear() ==
                LocalDateTime.now().getDayOfYear());
        assertTrue(bookingsToLoad.get(0).getDuration() == 1);

        assertTrue(!bookingsToLoad.get(1).isRecurrent());
        assertTrue(Objects.equals(bookingsToLoad.get(1).getLocationId(), "500"));
        assertTrue(bookingsToLoad.get(1).getStartTime().getDayOfYear() ==
                LocalDateTime.now().plusDays(1).getDayOfYear());
        assertTrue(bookingsToLoad.get(1).getDuration() == 2);
    }
}
