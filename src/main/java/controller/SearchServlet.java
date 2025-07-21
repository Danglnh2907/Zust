package controller;

import dao.GroupDAO;
import dao.SearchDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchServlet.class);
	private static final int RESULTS_PER_PAGE = 10;

	private SearchDAO searchDAO;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			this.searchDAO = new SearchDAO();
			LOGGER.info("SearchServlet initialized successfully");
		} catch (Exception e) {
			LOGGER.error("Failed to initialize SearchDAO", e);
			throw new ServletException("Failed to initialize search functionality", e);
		}
	}

	@Override
	public void destroy() {
		if (searchDAO != null) {
			try {
				searchDAO.close();
				LOGGER.info("SearchDAO closed successfully");
			} catch (Exception e) {
				LOGGER.error("Error closing SearchDAO", e);
			}
		}
		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");

		try {
			if ("live".equals(action)) {
				handleLiveSearch(request, response);
			} else if ("viewMore".equals(action)) {
				handleViewMoreSearch(request, response);
			} else if ("forceRefresh".equals(action)) {
				handleForceRefresh(request, response);
			} else {
				handleFullSearch(request, response);
			}
		} catch (Exception e) {
			LOGGER.error("Error in SearchServlet", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Search error occurred");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	private Account getAccount(HttpServletRequest request) {
		HttpSession session = request.getSession();
        return (Account) session.getAttribute("users");
	}

	/**
	 * Handle main search functionality
	 */
	private void handleFullSearch(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//Get session
		Account account = getAccount(request);
		if (account == null) {
			request.getRequestDispatcher("/auth").forward(request, response);
			return;
		}
		request.setAttribute("account", account);
		GroupDAO groupDAO = new GroupDAO();
		request.setAttribute("joinedGroups", groupDAO.getJoinedGroups(account.getId()));

		String keyword = request.getParameter("keyword");

		LOGGER.info("Full search request - keyword: {}", keyword);

		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Full search with empty keyword, showing empty results");
			request.setAttribute("keyword", "");
			request.setAttribute("searchResults", Map.of(
					"users", java.util.Collections.emptyList(),
					"posts_content", java.util.Collections.emptyList(),
					"posts_hashtag", java.util.Collections.emptyList(),
					"groups", java.util.Collections.emptyList()
			));
			request.getRequestDispatcher("/WEB-INF/views/search_results.jsp").forward(request, response);
			return;
		}

		try {
			// Perform search across all categories with limited results for overview
			Map<String, java.util.List<?>> searchResults = searchDAO.searchAllLimited(keyword, 10, account.getId());

			LOGGER.info("Full search completed for keyword: {} - Users: {}, Posts: {}, Hashtag Posts: {}, Groups: {}",
					keyword,
					searchResults.get("users").size(),
					searchResults.get("posts_content").size(),
					searchResults.get("posts_hashtag").size(),
					searchResults.get("groups").size());

			request.setAttribute("keyword", keyword);
			request.setAttribute("searchResults", searchResults);
			request.getRequestDispatcher("/WEB-INF/views/search_results.jsp").forward(request, response);

		} catch (Exception e) {
			LOGGER.error("Full search failed for keyword: {}", keyword, e);
			request.setAttribute("keyword", keyword);
			request.setAttribute("errorMessage", "Search failed. Please try again.");
			request.getRequestDispatcher("/WEB-INF/views/search_results.jsp").forward(request, response);
		}
	}

	/**
	 * Handle live search - return HTML fragment instead of JSON
	 */
	private void handleLiveSearch(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//Get session
		Account account = getAccount(request);
		if (account == null) {
			request.getRequestDispatcher("/auth").forward(request, response);
			return;
		}
		request.setAttribute("account", account);

		String keyword = request.getParameter("keyword");
		int limit = 3; // Fewer results for live search

		LOGGER.debug("Live search request - keyword: {}", keyword);

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		if (keyword == null || keyword.trim().isEmpty()) {
			response.getWriter().write("<div style='padding: 20px; text-align: center; color: #65676b;'>Enter search terms...</div>");
			return;
		}

		try {
			Map<String, java.util.List<?>> liveResults = searchDAO.searchAllLimited(keyword, limit, account.getId());

			LOGGER.debug("Live search completed for keyword: {} - Users: {}, Posts: {}, Hashtag Posts: {}, Groups: {}",
					keyword,
					liveResults.get("users").size(),
					liveResults.get("posts_content").size(),
					liveResults.get("posts_hashtag").size(),
					liveResults.get("groups").size());

			// Set attributes for JSP
			request.setAttribute("keyword", keyword);
			request.setAttribute("searchResults", liveResults);

			// Forward to live search JSP fragment
			request.getRequestDispatcher("/WEB-INF/views/live_search_results.jsp").forward(request, response);

		} catch (Exception e) {
			LOGGER.error("Live search failed for keyword: {}", keyword, e);
			response.getWriter().write("<div style='padding: 20px; text-align: center; color: #d73502;'><i class='fas fa-exclamation-triangle'></i> Search failed. Please try again.</div>");
		}
	}


	/**
	 * Handle view more search for specific category with pagination
	 */
	private void handleViewMoreSearch(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//Get session
		Account account = getAccount(request);
		if (account == null) {
			request.getRequestDispatcher("/auth").forward(request, response);
			return;
		}
		request.setAttribute("account", account);
		GroupDAO groupDAO = new GroupDAO();
		request.setAttribute("joinedGroups", groupDAO.getJoinedGroups(account.getId()));

		String keyword = request.getParameter("keyword");
		String category = request.getParameter("category");

		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Full search (view more search) with empty keyword, showing empty results");
			request.setAttribute("keyword", "");
			request.setAttribute("searchResults", Map.of(
					"users", java.util.Collections.emptyList(),
					"posts_content", java.util.Collections.emptyList(),
					"posts_hashtag", java.util.Collections.emptyList(),
					"groups", java.util.Collections.emptyList()
			));
			request.getRequestDispatcher("/WEB-INF/views/search_view_more.jsp").forward(request, response);
			return;
		}

		try {
			//Only fetch users or groups based on category
			switch (category) {
				case "users" -> {
					List<Account> users = searchDAO.searchUsers(keyword);
					request.setAttribute("users", users);
				}
				case "groups" -> {
					List<Group> groups = searchDAO.searchGroups(keyword);
					request.setAttribute("groups", groups);
				}
				default -> {
					request.setAttribute("message", "Invalid category: " + category);
				}
			}
			request.setAttribute("keyword", keyword);
			request.setAttribute("category", category);
			request.getRequestDispatcher("/WEB-INF/views/search_view_more.jsp").forward(request, response);
		} catch (Exception e) {
			LOGGER.error("Full search (view more results) failed for keyword: {}", keyword, e);
			request.setAttribute("keyword", keyword);
			request.setAttribute("errorMessage", "Search failed. Please try again.");
			request.getRequestDispatcher("/WEB-INF/views/search_view_more.jsp").forward(request, response);
		}
//		int page = 1;

//		try {
//			String pageParam = request.getParameter("page");
//			if (pageParam != null && !pageParam.isEmpty()) {
//				page = Integer.parseInt(pageParam);
//			}
//		} catch (NumberFormatException e) {
//			LOGGER.warn("Invalid page parameter: {}, using default page 1", request.getParameter("page"));
//			page = 1;
//		}
//
//		LOGGER.info("ViewMore search request - keyword: {}, category: {}, page: {}", keyword, category, page);
//
//		if (keyword == null || keyword.trim().isEmpty()) {
//			LOGGER.warn("ViewMore search with empty keyword, redirecting to post page");
//			response.sendRedirect(request.getContextPath() + "/post");
//			return;
//		}
//
//		if (category == null || category.trim().isEmpty()) {
//			LOGGER.warn("ViewMore search with empty category, redirecting to post page");
//			response.sendRedirect(request.getContextPath() + "/post");
//			return;
//		}
//
//		try {
//			int offset = (page - 1) * RESULTS_PER_PAGE;
//			LOGGER.info("Calculating offset: page={}, resultsPerPage={}, offset={}", page, RESULTS_PER_PAGE, offset);
//
//			Map<String, Object> pagedResults = searchDAO.searchCategoryPaged(keyword, category, RESULTS_PER_PAGE, offset);
//
//			LOGGER.info("Paged search results: totalCount={}, hasMore={}, resultSize={}",
//					pagedResults.get("totalCount"),
//					pagedResults.get("hasMore"),
//					pagedResults.get("results") != null ? ((java.util.List<?>)pagedResults.get("results")).size() : 0);
//
//			request.setAttribute("keyword", keyword);
//			request.setAttribute("category", category);
//			request.setAttribute("pagedResults", pagedResults);
//			request.setAttribute("currentPage", page);
//			request.setAttribute("resultsPerPage", RESULTS_PER_PAGE);
//
//			request.getRequestDispatcher("/WEB-INF/views/search_view_more.jsp").forward(request, response);
//		} catch (Exception e) {
//			LOGGER.error("View more search failed for keyword: {} category: {} page: {}", keyword, category, page, e);
//			response.sendRedirect(request.getContextPath() + "/post?error=" + java.net.URLEncoder.encode("Search failed", "UTF-8"));
//		}
	}

	/**
	 * Handle force refresh of search index (admin function)
	 */
	private void handleForceRefresh(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LOGGER.info("Force refresh search index requested");

		try {
			searchDAO.forceRefreshIndex();
			LOGGER.info("Search index force refresh completed successfully");

			// Redirect back to where they came from or search page
			String referer = request.getHeader("Referer");
			if (referer != null && !referer.isEmpty()) {
				response.sendRedirect(referer + (referer.contains("?") ? "&" : "?") + "refreshed=true");
			} else {
				response.sendRedirect(request.getContextPath() + "/search?refreshed=true");
			}
		} catch (Exception e) {
			LOGGER.error("Force refresh failed", e);
			response.sendRedirect(request.getContextPath() + "/search?error=" +
					java.net.URLEncoder.encode("Refresh failed", "UTF-8"));
		}
	}
}