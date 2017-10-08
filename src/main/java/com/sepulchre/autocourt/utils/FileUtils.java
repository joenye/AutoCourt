package com.sepulchre.autocourt.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sepulchre.autocourt.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static void saveBookingsToFile(List<Booking> bookings,
                                          String filePath) {
        // Delete existing file
        File file = new File(filePath);
        file.delete();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath),
                    bookings);
            logger.info("Wrote " + bookings.size() + " bookings to save file: " +
                    bookings.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Booking> loadBookingsFromFile(String filePath) {

        File file = new File(filePath);
        if (file.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            List<Booking> bookings = null;
            try {
                bookings = mapper.readValue(new File(filePath), new TypeReference<List<Booking>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            file.delete();
            logger.info("Loaded " + bookings.size() + " bookings from save file: " + bookings.toString());
            return bookings;
        } else return null;
    }
}
