package controller;

import java.io.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;

import dao.AccountDAO;
import dao.CommentDAO;
import dao.PostDAO;
import model.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.ServletException;

@WebServlet(name = "ReportControllerServlet", value = "/report")
public class ReportController extends HttpServlet {
    private PrintWriter out;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        /*
         * /report?type=post&id=post_id
         * /report?type=comment&id=comment_id
         * /report?type=account&id=account_id
         */
        out = response.getWriter();

        //Fetch session
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (account == null) {
            request.getRequestDispatcher("/auth").forward(request, response);
            return;
        }

        //Get type and if
        String type = "";
        int id = -1;
        try {
            type = request.getParameter("type");
            if (type == null || type.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            logger.warning("Failed to parse id");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        request.setAttribute("account", account);
        String typeAttr = "", reportLink = "";
        switch (type) {
            case "post" -> {
                typeAttr = "post";
                reportLink = "/post?postID=" + id;
            }
            case "comment" -> {
                typeAttr = "comment";
                reportLink = "/comment?commentID=" + id;
            }
            case "account" -> {
                typeAttr = "account";
                reportLink = "/account?accountID=" + id;
            }
        }
        request.setAttribute("type", typeAttr);
        request.setAttribute("id", id);
        request.setAttribute("reportLink", reportLink);
        request.getRequestDispatcher("/WEB-INF/views/report.jsp").forward(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        out = response.getWriter();

        //Fetch session
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("users");
        if (account == null) {
            request.getRequestDispatcher("/auth").forward(request, response);
            return;
        }

        //Get request parameter
        try {
            String type = request.getParameter("type");
            int id = Integer.parseInt(request.getParameter("id"));
            String content = request.getParameter("content");
            switch (type) {
                case "post" -> {
                    PostDAO postDAO = new PostDAO();
                    ReportPostDTO dto = new ReportPostDTO();
                    dto.setAccountID(account.getId());
                    dto.setCreatedAt(LocalDateTime.now());
                    dto.setPostID(id);
                    dto.setStatus("sent");
                    dto.setContent(content);
                    boolean success = postDAO.report(dto);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }
                case "comment" -> {
                    CommentDAO commentDAO = new CommentDAO();
                    ReportCommentDTO dto = new ReportCommentDTO();
                    dto.setAccountID(account.getId());
                    dto.setCreatedAt(LocalDateTime.now());
                    dto.setCommentID(id);
                    dto.setStatus("sent");
                    dto.setContent(content);
                    boolean success = commentDAO.report(dto);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }
                case "account" -> {
                    AccountDAO accountDAO = new AccountDAO();
                    ReportAccountDTO dto = new ReportAccountDTO();
                    dto.setReporterId(account.getId());
                    dto.setCreatedAt(LocalDateTime.now());
                    dto.setReportedId(id);
                    dto.setStatus("sent");
                    dto.setContent(content);
                    boolean success = accountDAO.report(dto);
                    if (success) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }
            }
        } catch (NumberFormatException e) {
            logger.warning("Failed to parse id");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

    }
}