package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/testLogin")
public class TestLoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Set tạm accountId vào session
        Integer accountId = 1003; // Hoặc 1004
        req.getSession().setAttribute("accountId", accountId);
        System.out.println("TestLoginServlet - Set accountId: " + accountId);
        resp.sendRedirect("/feed");
    }
}