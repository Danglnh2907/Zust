package controller;

import dao.SearchDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@WebServlet("/search")
public class SearchServlet extends HttpServlet implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchServlet.class);
	private SearchDAO searchDAO;

	@Override
	public void init() throws ServletException {
		super.init();
		searchDAO = new SearchDAO();
		LOGGER.info("SearchServlet initialized. Starting Lucene data indexing...");
		searchDAO.indexAllDataToLucene();
		LOGGER.info("Lucene data indexing complete.");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("loggedInAccount") == null) {
			response.sendRedirect(request.getContextPath() + "/login?message=" + java.net.URLEncoder.encode("Please log in to access search.", "UTF-8"));
			return;
		}

		String keyword = request.getParameter("keyword");
		Map<String, List<?>> searchResults = Collections.emptyMap();
		String errorMessage = null;

		if (keyword != null && !keyword.trim().isEmpty()) {
			try {
				searchResults = searchDAO.searchAll(keyword);
			} catch (Exception e) {
				LOGGER.error("Search failed for keyword: {}", keyword, e);
				errorMessage = "An error occurred during search: " + e.getMessage();
			}
		} else if (keyword != null && keyword.trim().isEmpty()) {
			errorMessage = "Please enter a search keyword.";
		}

		request.setAttribute("keyword", keyword);
		request.setAttribute("searchResults", searchResults);
		request.setAttribute("errorMessage", errorMessage);

		request.getRequestDispatcher("/WEB-INF/views/search_results.jsp").forward(request, response);
	}
}
