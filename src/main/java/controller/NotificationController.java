package controller;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.*;
import dao.NotificationDAO;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.ServletException;
import model.Account;

@WebServlet(name = "NotificationController", value = "/notification")
public class NotificationController extends HttpServlet {
    private Gson gson;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
         * /notification
         */

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (account == null) {
            response.sendRedirect("/auth");
        }

        NotificationDAO dao = new NotificationDAO();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                        (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = gson.toJson(dao.getNotifications(account.getId()));
        response.getWriter().println(json);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
         * /notification?action=mark&id=NOTIFICATION_ID&userID=USER_ID
         * /notification?action=mark&userID=USER_ID
         */

        //Won't expose the URL for add notification, this will be called the backend code, not the client

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (account == null) {
            response.sendRedirect("/auth");
        }

        NotificationDAO dao = new NotificationDAO();

        //Get action parameter
        String action = request.getParameter("action");
        switch (action) {
            case "mark" -> {
                //Try to get the notification ID
                try {
                    String id = request.getParameter("id");
                    if (id == null || id.isEmpty()) {
                        //If no ID provided, then we this is the mark all
                        boolean success = dao.markAllNotificationAsRead(account.getId());
                        if (success) {
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    } else {
                        //If ID provided, only mark 1
                        boolean success = dao.markNotificationAsRead(Integer.parseInt(id));
                        if (success) {
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            case "" -> {}
        }
    }
}