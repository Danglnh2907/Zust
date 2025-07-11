package controller;

import dao.GroupDAO;
import dao.PostDAO;
import dto.InteractGroupDTO;
import dto.RespPostDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/")
public class HomeServlet extends HttpServlet {

	@Serial
	private static final long serialVersionUID = 1L; // Recommended for Servlets

	public HomeServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		Account account = (Account) session.getAttribute("users");
		if (account == null) {
			request.getRequestDispatcher("WEB-INF/views/auth.jsp").forward(request, response);
			return;
		}

		//Get feed
		PostDAO dao = new PostDAO();
		ArrayList<RespPostDTO> feed = dao.getNewsfeedPosts(account.getId());
		request.setAttribute("feeds", feed);
		GroupDAO groupDAO = new GroupDAO();
		List<InteractGroupDTO> groups = groupDAO.getJoinedGroups(account.getId());
		request.setAttribute("joinedGroups", groups);
		request.getRequestDispatcher("WEB-INF/views/post.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
