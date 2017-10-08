package com.sepulchre.autocourt.api;

import com.sepulchre.autocourt.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.sepulchre.autocourt.App.*;
import static spark.Spark.*;


public class API {

    private static final Logger logger = LoggerFactory.getLogger(API.class);

    public static void createEndPoints() {
        ThymeleafTemplateEngine engine = new ThymeleafTemplateEngine();
        addJava8Dialect(engine);
        staticFiles.location("/public");

        get("/", (req, res) -> {
            res.redirect("/bookings");
            return "Redirected to booking list.";
        });

        get("/bookings", (req, res) -> {
            List<Booking> bookings = getBookings();
            // return bookings;
            Map<String, Object> model = new HashMap<>();
            model.put("bookings", bookings);
            model.put("liveMode", isLiveMode());
            model.put("locations", Booking.Location.class);
            model.put("newBooking", new Booking());

            return engine.render(new ModelAndView(model, "hello"));
        });

        get("/bookings/delete/:id", (req, res) -> {

            UUID id = UUID.fromString(req.params("id"));
            cancelBooking(id);
            res.redirect("/bookings");
            return "Booking " + id + " deleted successfully.";
        });

        post("/bookings/add", (req, res) -> {

            LocalDateTime startTime = LocalDateTime.parse(
                    req.queryParams("year-input") + '-' + req.queryParams("month-input") +
                            '-' + req.queryParams("day-input") + '-' + req.queryParams("hour-input") +
                            ":00", Booking.API_URL_FORMATTER);

            Booking booking = new Booking(
                    false,
                    Booking.Location.valueOf(req.queryParams("location-input")),
                    startTime,
                    Integer.parseInt(req.queryParams("duration-input"))
            );

            addBooking(booking);
            res.redirect("/bookings");
            return "Added booking: " + booking.toString();
        });
    }

    private static void addJava8Dialect(ThymeleafTemplateEngine engine) {
        Field f = null;
        try {
            f = engine.getClass().getDeclaredField("templateEngine");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        assert f != null;
        f.setAccessible(true);
        TemplateEngine baseTemplateEngine = null;
        try {
            baseTemplateEngine = (TemplateEngine) f.get(engine);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        assert baseTemplateEngine != null;
        baseTemplateEngine.addDialect(new Java8TimeDialect());
    }
}
