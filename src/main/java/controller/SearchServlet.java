package controller;

import dao.SearchDAO;
import model.Account;
import model.Post;
import model.Group;

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
	private static final int RESULTS_PER_PAGE = 10;
	private static final int LIVE_SEARCH_LIMIT = 5;

	@Override
	public void init() throws ServletException {
		super.init();
		searchDAO = new SearchDAO();
		LOGGER.info("SearchServlet initialized. Starting Lucene data indexing...");
		try {
			searchDAO.indexAllDataToLucene();
			LOGGER.info("Lucene data indexing complete.");
		} catch (Exception e) {
			LOGGER.error("Error during initialization", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("users") == null) {
			response.sendRedirect(request.getContextPath() + "/login?message=" + java.net.URLEncoder.encode("Please log in to access search.", "UTF-8"));
			return;
		}

		String action = request.getParameter("action");

		if ("live".equals(action)) {
			handleLiveSearch(request, response);
		} else if ("viewMore".equals(action)) {
			handleViewMoreSearch(request, response);
		} else {
			handleFullSearch(request, response);
		}
	}

	private void handleLiveSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String keyword = request.getParameter("keyword");

		LOGGER.info("Live search request for keyword: {}", keyword);

		if (keyword == null || keyword.trim().isEmpty() || keyword.trim().length() < 2) {
			request.setAttribute("liveResults", Collections.emptyMap());
			request.setAttribute("keyword", keyword);
			request.getRequestDispatcher("/WEB-INF/views/live_search_results.jsp").forward(request, response);
			return;
		}

		try {
			Map<String, List<?>> liveResults = searchDAO.searchAllLimited(keyword, LIVE_SEARCH_LIMIT);
			request.setAttribute("keyword", keyword);
			request.setAttribute("liveResults", liveResults);

			LOGGER.info("Live search results: users={}, posts_content={}, posts_hashtag={}, groups={}",
					liveResults.get("users") != null ? ((List<?>)liveResults.get("users")).size() : 0,
					liveResults.get("posts_content") != null ? ((List<?>)liveResults.get("posts_content")).size() : 0,
					liveResults.get("posts_hashtag") != null ? ((List<?>)liveResults.get("posts_hashtag")).size() : 0,
					liveResults.get("groups") != null ? ((List<?>)liveResults.get("groups")).size() : 0);

			request.getRequestDispatcher("/WEB-INF/views/live_search_results.jsp").forward(request, response);
		} catch (Exception e) {
			LOGGER.error("Live search failed for keyword: {}", keyword, e);
			request.setAttribute("liveResults", Collections.emptyMap());
			request.setAttribute("keyword", keyword);
			request.getRequestDispatcher("/WEB-INF/views/live_search_results.jsp").forward(request, response);
		}
	}

	private void handleViewMoreSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String keyword = request.getParameter("keyword");
		String category = request.getParameter("category");
		int page = 1;

		try {
			String pageParam = request.getParameter("page");
			if (pageParam != null && !pageParam.isEmpty()) {
				page = Integer.parseInt(pageParam);
			}
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid page parameter: {}, using default page 1", request.getParameter("page"));
			page = 1;
		}

		LOGGER.info("ViewMore search request - keyword: {}, category: {}, page: {}", keyword, category, page);

		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("ViewMore search with empty keyword, redirecting to post page");
			response.sendRedirect(request.getContextPath() + "/post");
			return;
		}

		if (category == null || category.trim().isEmpty()) {
			LOGGER.warn("ViewMore search with empty category, redirecting to post page");
			response.sendRedirect(request.getContextPath() + "/post");
			return;
		}

		try {
			int offset = (page - 1) * RESULTS_PER_PAGE;
			LOGGER.info("Calculating offset: page={}, resultsPerPage={}, offset={}", page, RESULTS_PER_PAGE, offset);

			Map<String, Object> pagedResults = searchDAO.searchCategoryPaged(keyword, category, RESULTS_PER_PAGE, offset);

			LOGGER.info("Paged search results: totalCount={}, hasMore={}, resultSize={}",
					pagedResults.get("totalCount"),
					pagedResults.get("hasMore"),
					pagedResults.get("results") != null ? ((List<?>)pagedResults.get("results")).size() : 0);

			request.setAttribute("keyword", keyword);
			request.setAttribute("category", category);
			request.setAttribute("pagedResults", pagedResults);
			request.setAttribute("currentPage", page);
			request.setAttribute("resultsPerPage", RESULTS_PER_PAGE);

			request.getRequestDispatcher("/WEB-INF/views/search_view_more.jsp").forward(request, response);
		} catch (Exception e) {
			LOGGER.error("View more search failed for keyword: {} category: {} page: {}", keyword, category, page, e);
			response.sendRedirect(request.getContextPath() + "/post?error=" + java.net.URLEncoder.encode("Search failed", "UTF-8"));
		}
	}

	private void handleFullSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
