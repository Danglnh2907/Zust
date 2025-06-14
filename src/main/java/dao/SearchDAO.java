package dao;

import model.Account;
import model.Post;
import model.Group;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.database.DBContext;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.*;

public class SearchDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchDAO.class);
	private static final String INDEX_DIR = "lucene_index";

	private final Analyzer analyzer;
	private final Directory directory;
	private IndexWriter indexWriter;
	private DirectoryReader indexReader;

	public SearchDAO() {
		try {
			this.analyzer = new StandardAnalyzer();
			this.directory = FSDirectory.open(Paths.get(INDEX_DIR));

			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.indexWriter = new IndexWriter(directory, config);
			this.indexWriter.commit();

			this.indexReader = DirectoryReader.open(directory);

			LOGGER.info("Lucene index initialized at: {}", Paths.get(INDEX_DIR).toAbsolutePath());

		} catch (IOException e) {
			LOGGER.error("Error initializing Lucene index", e);
			throw new RuntimeException("Could not initialize Lucene index", e);
		}
	}

	/**
	 * Strip HTML tags from content
	 */
	private String stripHtmlTags(String html) {
		if (html == null || html.trim().isEmpty()) {
			return "";
		}

		// Remove HTML tags but keep content
		String stripped = html.replaceAll("<[^>]*>", " ");

		// Replace multiple whitespaces with single space
		stripped = stripped.replaceAll("\\s+", " ");

		// Decode common HTML entities
		stripped = stripped.replace("&nbsp;", " ")
				.replace("&amp;", "&")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&#39;", "'");

		return stripped.trim();
	}

	/**
	 * Extract preview text from HTML content
	 */
	private String getPreviewText(String htmlContent, int maxLength) {
		if (htmlContent == null || htmlContent.trim().isEmpty()) {
			return "";
		}

		String plainText = stripHtmlTags(htmlContent);

		if (plainText.length() <= maxLength) {
			return plainText;
		}

		// Find last space before maxLength to avoid cutting words
		int lastSpace = plainText.lastIndexOf(' ', maxLength);
		if (lastSpace > maxLength - 20) { // If space is close enough to maxLength
			return plainText.substring(0, lastSpace) + "...";
		} else {
			return plainText.substring(0, maxLength) + "...";
		}
	}

	// Cập nhật method indexPosts để xử lý HTML
	private void indexPosts() throws SQLException, IOException {
		String sql = "SELECT p.post_id, p.post_content, p.account_id, " +
				"STRING_AGG(h.hashtag_name, ' ') WITHIN GROUP (ORDER BY h.hashtag_name) AS post_hashtags " +
				"FROM post p " +
				"LEFT JOIN tag_hashtag th ON p.post_id = th.post_id " +
				"LEFT JOIN hashtag h ON th.hashtag_id = h.hashtag_id " +
				"GROUP BY p.post_id, p.post_content, p.account_id";

		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			int count = 0;
			while (rs.next()) {
				Document doc = new Document();
				String postId = String.valueOf(rs.getInt("post_id"));
				String postContent = rs.getString("post_content");
				String postHashtags = rs.getString("post_hashtags");

				// Strip HTML for indexing
				String plainTextContent = stripHtmlTags(postContent);

				doc.add(new StringField("id", postId, Field.Store.YES));

				// Index both original and plain text content
				doc.add(new TextField("post_content", normalizeText(plainTextContent), Field.Store.YES));
				doc.add(new TextField("post_content_html", postContent != null ? postContent : "", Field.Store.YES));
				doc.add(new TextField("post_hashtags", normalizeText(postHashtags != null ? postHashtags : ""), Field.Store.YES));

				// Tạo search tokens cho nội dung post (using plain text)
				doc.add(new TextField("post_content_tokens", generateSearchTokens(plainTextContent), Field.Store.NO));

				addDocument(doc, postId);
				count++;
			}
			LOGGER.info("Indexed {} posts with HTML processing and smart search tokens.", count);
		}
	}

	// Cập nhật getPostById để include plain text version
	private Post getPostById(int postId) throws SQLException {
		String sql = "SELECT post_id, post_content, account_id, post_create_date, post_last_update, post_privacy, post_status, group_id FROM post WHERE post_id = ?";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, postId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Post post = new Post();
					post.setId(rs.getInt("post_id"));

					String htmlContent = rs.getString("post_content");
					post.setPostContent(htmlContent); // Keep original HTML

					// Add plain text version for display
					String plainTextContent = stripHtmlTags(htmlContent);
					// You might need to add this field to Post model or handle it in JSP

					int accountId = rs.getInt("account_id");
					Account associatedAccount = getAccountById(accountId);
					post.setAccount(associatedAccount);

					java.sql.Timestamp createTimestamp = rs.getTimestamp("post_create_date");
					post.setPostCreateDate(createTimestamp != null ? createTimestamp.toInstant() : null);
					java.sql.Timestamp updateTimestamp = rs.getTimestamp("post_last_update");
					post.setPostLastUpdate(updateTimestamp != null ? updateTimestamp.toInstant() : null);

					post.setPostPrivacy(rs.getString("post_privacy"));
					post.setPostStatus(rs.getString("post_status"));

					Integer groupId = rs.getObject("group_id", Integer.class);
					if (groupId != null) {
						Group associatedGroup = getGroupById(groupId);
						post.setGroup(associatedGroup);
					} else {
						post.setGroup(null);
					}
					return post;
				}
			}
		}
		return null;
	}

	private IndexSearcher getSearcher() throws IOException {
		DirectoryReader newReader = DirectoryReader.openIfChanged(indexReader);
		if (newReader != null) {
			indexReader.close();
			indexReader = newReader;
			LOGGER.debug("Lucene IndexReader refreshed.");
		}
		return new IndexSearcher(indexReader);
	}

	/**
	 * Chuẩn hóa text để loại bỏ dấu tiếng Việt
	 */
	private String normalizeText(String text) {
		if (text == null) return "";
		// Loại bỏ dấu tiếng Việt
		String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
		normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return normalized.toLowerCase();
	}

	/**
	 * Tạo các từ khóa tìm kiếm từ tên đầy đủ
	 * VD: "Nguyen Hoang Khai" -> ["nguyen", "hoang", "khai", "ng", "ngu", "nguy", ...]
	 */
	private String generateSearchTokens(String fullName) {
		if (fullName == null || fullName.trim().isEmpty()) {
			return "";
		}

		Set<String> tokens = new HashSet<>();
		String normalized = normalizeText(fullName);
		String[] words = normalized.split("\\s+");

		for (String word : words) {
			if (word.length() > 0) {
				tokens.add(word); // Từ đầy đủ

				// Tạo các prefix từ 2 ký tự trở lên
				for (int i = 2; i <= word.length(); i++) {
					tokens.add(word.substring(0, i));
				}
			}
		}

		// Tạo các combination của chữ cái đầu
		StringBuilder initials = new StringBuilder();
		for (String word : words) {
			if (word.length() > 0) {
				initials.append(word.charAt(0));
			}
		}

		// Thêm các prefix của initials
		String initialString = initials.toString();
		for (int i = 1; i <= initialString.length(); i++) {
			tokens.add(initialString.substring(0, i));
		}

		return String.join(" ", tokens);
	}

	/**
	 * Tạo query thông minh cho tìm kiếm
	 */
	private Query createSmartQuery(String keyword, String[] fields) throws ParseException {
		if (keyword == null || keyword.trim().isEmpty()) {
			return new MatchAllDocsQuery();
		}

		String normalizedKeyword = normalizeText(keyword.trim());
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

		// 1. Exact match (highest priority)
		for (String field : fields) {
			Query exactQuery = new TermQuery(new Term(field, normalizedKeyword));
			queryBuilder.add(exactQuery, BooleanClause.Occur.SHOULD);
		}

		// 2. Prefix match
		for (String field : fields) {
			Query prefixQuery = new PrefixQuery(new Term(field, normalizedKeyword));
			queryBuilder.add(prefixQuery, BooleanClause.Occur.SHOULD);
		}

		// 3. Wildcard match
		for (String field : fields) {
			Query wildcardQuery = new WildcardQuery(new Term(field, "*" + normalizedKeyword + "*"));
			queryBuilder.add(wildcardQuery, BooleanClause.Occur.SHOULD);
		}

		// 4. Fuzzy match (for typos)
		if (normalizedKeyword.length() > 3) {
			for (String field : fields) {
				Query fuzzyQuery = new FuzzyQuery(new Term(field, normalizedKeyword), 1);
				queryBuilder.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
			}
		}

		return queryBuilder.build();
	}

	private Query parseQuery(String keyword, String[] fields, Analyzer analyzer) throws ParseException {
		// Sử dụng smart query thay vì parser cũ
		return createSmartQuery(keyword, fields);
	}

	private void addDocument(Document doc, String docId) throws IOException {
		indexWriter.updateDocument(new Term("id", docId), doc);
		LOGGER.debug("Indexed document with ID: {}", docId);
	}

	public void indexAllDataToLucene() {
		LOGGER.info("Starting to index all data to Lucene...");
		try {
			indexWriter.deleteAll();
			indexWriter.commit();

			indexAccounts();
			indexPosts();
			indexGroups();

			indexWriter.commit();
			LOGGER.info("All data indexed to Lucene successfully.");
		} catch (IOException | SQLException e) {
			LOGGER.error("Error during batch indexing to Lucene", e);
		}
	}

	private void indexAccounts() throws SQLException, IOException {
		String sql = "SELECT account_id, username, password, fullname, email, phone, gender, dob, avatar, bio, credit, account_status, account_role FROM account";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Document doc = new Document();
				String accountId = String.valueOf(rs.getInt("account_id"));
				String username = rs.getString("username");
				String fullname = rs.getString("fullname");
				String email = rs.getString("email");
				String bio = rs.getString("bio");

				doc.add(new StringField("id", accountId, Field.Store.YES));

				// Index các field bình thường
				doc.add(new TextField("username", normalizeText(username), Field.Store.YES));
				doc.add(new TextField("fullname", normalizeText(fullname), Field.Store.YES));
				doc.add(new TextField("email", normalizeText(email), Field.Store.YES));
				doc.add(new TextField("bio", normalizeText(bio != null ? bio : ""), Field.Store.YES));

				// Index các search tokens để tìm kiếm thông minh
				String searchTokens = generateSearchTokens(fullname);
				doc.add(new TextField("fullname_tokens", searchTokens, Field.Store.NO));
				doc.add(new TextField("username_tokens", generateSearchTokens(username), Field.Store.NO));

				// Combined field
				String combinedAccountFields = normalizeText(username + " " + fullname + " " + email + " " + (bio != null ? bio : ""));
				doc.add(new TextField("account_all_fields", combinedAccountFields, Field.Store.NO));

				// Combined tokens
				String allTokens = searchTokens + " " + generateSearchTokens(username) + " " + normalizeText(email);
				doc.add(new TextField("account_all_tokens", allTokens, Field.Store.NO));

				addDocument(doc, accountId);
			}
			LOGGER.info("Indexed accounts with smart search tokens.");
		}
	}

	private void indexGroups() throws SQLException, IOException {
		String sql = "SELECT group_id, group_name, group_description FROM [group]";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Document doc = new Document();
				String groupId = String.valueOf(rs.getInt("group_id"));
				String groupName = rs.getString("group_name");
				String groupDescription = rs.getString("group_description");

				doc.add(new StringField("id", groupId, Field.Store.YES));
				doc.add(new TextField("group_name", normalizeText(groupName), Field.Store.YES));
				doc.add(new TextField("group_description", normalizeText(groupDescription != null ? groupDescription : ""), Field.Store.YES));

				// Search tokens
				doc.add(new TextField("group_name_tokens", generateSearchTokens(groupName), Field.Store.NO));
				doc.add(new TextField("group_description_tokens", generateSearchTokens(groupDescription), Field.Store.NO));

				String combinedGroupFields = normalizeText(groupName + " " + (groupDescription != null ? groupDescription : ""));
				doc.add(new TextField("group_all_fields", combinedGroupFields, Field.Store.NO));

				String allTokens = generateSearchTokens(groupName) + " " + generateSearchTokens(groupDescription);
				doc.add(new TextField("group_all_tokens", allTokens, Field.Store.NO));

				addDocument(doc, groupId);
			}
			LOGGER.info("Indexed groups with smart search tokens.");
		}
	}

	// Các method get entity by ID (giữ nguyên)
	private Account getAccountById(int accountId) throws SQLException {
		String sql = "SELECT account_id, username, password, fullname, email, phone, gender, dob, avatar, bio, credit, account_status, account_role FROM account WHERE account_id = ?";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, accountId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Account account = new Account();
					account.setId(rs.getInt("account_id"));
					account.setUsername(rs.getString("username"));
					account.setPassword(rs.getString("password"));
					account.setFullname(rs.getString("fullname"));
					account.setEmail(rs.getString("email"));
					account.setPhone(rs.getString("phone"));
					account.setGender(rs.getBoolean("gender"));
					java.sql.Date sqlDate = rs.getDate("dob");
					account.setDob(sqlDate != null ? sqlDate.toLocalDate() : null);
					account.setAvatar(rs.getString("avatar"));
					account.setBio(rs.getString("bio"));
					account.setCredit(rs.getInt("credit"));
					account.setAccountStatus(rs.getString("account_status"));
					account.setAccountRole(rs.getString("account_role"));
					return account;
				}
			}
		}
		return null;
	}

	private Group getGroupById(int groupId) throws SQLException {
		String sql = "SELECT group_id, group_name, group_cover_image, group_description, group_create_date, group_status FROM [group] WHERE group_id = ?";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, groupId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Group group = new Group();
					group.setId(rs.getInt("group_id"));
					group.setGroupName(rs.getString("group_name"));
					group.setGroupCoverImage(rs.getString("group_cover_image"));
					group.setGroupDescription(rs.getString("group_description"));

					java.sql.Timestamp createTimestamp = rs.getTimestamp("group_create_date");
					group.setGroupCreateDate(createTimestamp != null ? createTimestamp.toInstant() : null);

					group.setGroupStatus(rs.getString("group_status"));
					return group;
				}
			}
		}
		return null;
	}

	// Các method search được cải thiện
	public List<Account> searchUsers(String keyword) {
		return searchUsersLimited(keyword, 10);
	}

	private List<Account> searchUsersLimited(String keyword, int limit) {
		List<Account> accounts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Search keyword for users is empty or null");
			return accounts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"username", "fullname", "email", "bio", "username_tokens", "fullname_tokens", "account_all_fields", "account_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			// Sắp xếp theo relevance score
			TopDocs hits = searcher.search(query, limit);
			LOGGER.info("Found {} hits for user keyword '{}'", hits.totalHits.value, keyword);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int accountId = Integer.parseInt(doc.get("id"));
				Account account = getAccountById(accountId);
				if (account != null) {
					accounts.add(account);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error executing Lucene search for users with keyword: {}", keyword, e);
		}
		return accounts;
	}

	public List<Post> searchPostsByContent(String keyword) {
		return searchPostsByContentLimited(keyword, 10);
	}

	private List<Post> searchPostsByContentLimited(String keyword, int limit) {
		List<Post> posts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Search keyword for posts is empty or null");
			return posts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_content", "post_content_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, limit);
			LOGGER.info("Found {} hits for post content keyword '{}'", hits.totalHits.value, keyword);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int postId = Integer.parseInt(doc.get("id"));
				Post post = getPostById(postId);
				if (post != null) {
					posts.add(post);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error executing Lucene search for posts by content with keyword: {}", keyword, e);
		}
		return posts;
	}

	public List<Group> searchGroups(String keyword) {
		return searchGroupsLimited(keyword, 10);
	}

	private List<Group> searchGroupsLimited(String keyword, int limit) {
		List<Group> groups = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Search keyword for groups is empty or null");
			return groups;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"group_name", "group_description", "group_name_tokens", "group_description_tokens", "group_all_fields", "group_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, limit);
			LOGGER.info("Found {} hits for group keyword '{}'", hits.totalHits.value, keyword);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int groupId = Integer.parseInt(doc.get("id"));
				Group group = getGroupById(groupId);
				if (group != null) {
					groups.add(group);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error executing Lucene search for groups with keyword: {}", keyword, e);
		}
		return groups;
	}

	public List<Post> searchPostsByHashtag(String hashtagKeyword) {
		return searchPostsByHashtagLimited(hashtagKeyword, 10);
	}

	private List<Post> searchPostsByHashtagLimited(String keyword, int limit) {
		List<Post> posts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Search keyword for hashtags is empty or null");
			return posts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_hashtags"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, limit);
			LOGGER.info("Found {} hits for post by hashtag keyword '{}'", hits.totalHits.value, keyword);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int postId = Integer.parseInt(doc.get("id"));
				Post post = getPostById(postId);
				if (post != null) {
					posts.add(post);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error executing Lucene search for posts by hashtag with keyword: {}", keyword, e);
		}
		return posts;
	}

	// Các method còn lại (giữ nguyên)
	public Map<String, List<?>> searchAll(String keyword) {
		Map<String, List<?>> allResults = new HashMap<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Combined search keyword is empty or null");
			return allResults;
		}

		LOGGER.info("Performing combined search for keyword: '{}'", keyword);
		allResults.put("users", searchUsers(keyword));
		allResults.put("posts_content", searchPostsByContent(keyword));
		allResults.put("posts_hashtag", searchPostsByHashtag(keyword));
		allResults.put("groups", searchGroups(keyword));

		return allResults;
	}

	public Map<String, List<?>> searchAllLimited(String keyword, int limit) {
		Map<String, List<?>> allResults = new HashMap<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Live search keyword is empty or null");
			return allResults;
		}

		LOGGER.info("Performing live search for keyword: '{}' with limit: {}", keyword, limit);
		allResults.put("users", searchUsersLimited(keyword, limit));
		allResults.put("posts_content", searchPostsByContentLimited(keyword, limit));
		allResults.put("posts_hashtag", searchPostsByHashtagLimited(keyword, limit));
		allResults.put("groups", searchGroupsLimited(keyword, limit));

		return allResults;
	}

	// Thêm vào SearchDAO.java - thay thế method searchCategoryPaged hiện tại

	public Map<String, Object> searchCategoryPaged(String keyword, String category, int limit, int offset) {
		Map<String, Object> result = new HashMap<>();
		List<?> results = new ArrayList<>();
		int totalCount = 0;
		boolean hasMore = false;

		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Paged search keyword is empty or null");
			result.put("results", results);
			result.put("totalCount", totalCount);
			result.put("hasMore", hasMore);
			return result;
		}

		try {
			switch (category.toLowerCase()) {
				case "users":
					results = searchUsersPaged(keyword, limit, offset);
					totalCount = getTotalUsersCount(keyword);
					break;
				case "posts_content":
					results = searchPostsByContentPaged(keyword, limit, offset);
					totalCount = getTotalPostsContentCount(keyword);
					break;
				case "posts_hashtag":
					results = searchPostsByHashtagPaged(keyword, limit, offset);
					totalCount = getTotalPostsHashtagCount(keyword);
					break;
				case "groups":
					results = searchGroupsPaged(keyword, limit, offset);
					totalCount = getTotalGroupsCount(keyword);
					break;
				default:
					LOGGER.warn("Unknown category: {}", category);
					break;
			}

			hasMore = (offset + limit) < totalCount;

		} catch (Exception e) {
			LOGGER.error("Error in paged search for category: {} keyword: {}", category, keyword, e);
		}

		result.put("results", results);
		result.put("totalCount", totalCount);
		result.put("hasMore", hasMore);

		LOGGER.info("Paged search for category: {} keyword: {} - found {} results, total: {}, hasMore: {}",
				category, keyword, results.size(), totalCount, hasMore);

		return result;
	}

	// Implement các method paged search
	private List<Account> searchUsersPaged(String keyword, int limit, int offset) {
		List<Account> accounts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return accounts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"username", "fullname", "email", "bio", "username_tokens", "fullname_tokens", "account_all_fields", "account_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			// Search với limit + offset để lấy đúng trang
			TopDocs hits = searcher.search(query, offset + limit);
			LOGGER.debug("Found {} total hits for paged user search with keyword '{}'", hits.totalHits.value, keyword);

			// Skip offset số kết quả đầu và lấy limit kết quả tiếp theo
			for (int i = offset; i < Math.min(offset + limit, hits.scoreDocs.length); i++) {
				ScoreDoc scoreDoc = hits.scoreDocs[i];
				Document doc = searcher.doc(scoreDoc.doc);
				int accountId = Integer.parseInt(doc.get("id"));
				Account account = getAccountById(accountId);
				if (account != null) {
					accounts.add(account);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error in paged user search", e);
		}
		return accounts;
	}

	private List<Post> searchPostsByContentPaged(String keyword, int limit, int offset) {
		List<Post> posts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return posts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_content", "post_content_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, offset + limit);
			LOGGER.debug("Found {} total hits for paged post content search with keyword '{}'", hits.totalHits.value, keyword);

			for (int i = offset; i < Math.min(offset + limit, hits.scoreDocs.length); i++) {
				ScoreDoc scoreDoc = hits.scoreDocs[i];
				Document doc = searcher.doc(scoreDoc.doc);
				int postId = Integer.parseInt(doc.get("id"));
				Post post = getPostById(postId);
				if (post != null) {
					posts.add(post);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error in paged post content search", e);
		}
		return posts;
	}

	private List<Post> searchPostsByHashtagPaged(String keyword, int limit, int offset) {
		List<Post> posts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return posts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_hashtags"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, offset + limit);
			LOGGER.debug("Found {} total hits for paged hashtag search with keyword '{}'", hits.totalHits.value, keyword);

			for (int i = offset; i < Math.min(offset + limit, hits.scoreDocs.length); i++) {
				ScoreDoc scoreDoc = hits.scoreDocs[i];
				Document doc = searcher.doc(scoreDoc.doc);
				int postId = Integer.parseInt(doc.get("id"));
				Post post = getPostById(postId);
				if (post != null) {
					posts.add(post);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error in paged hashtag search", e);
		}
		return posts;
	}

	private List<Group> searchGroupsPaged(String keyword, int limit, int offset) {
		List<Group> groups = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return groups;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"group_name", "group_description", "group_name_tokens", "group_description_tokens", "group_all_fields", "group_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, offset + limit);
			LOGGER.debug("Found {} total hits for paged group search with keyword '{}'", hits.totalHits.value, keyword);

			for (int i = offset; i < Math.min(offset + limit, hits.scoreDocs.length); i++) {
				ScoreDoc scoreDoc = hits.scoreDocs[i];
				Document doc = searcher.doc(scoreDoc.doc);
				int groupId = Integer.parseInt(doc.get("id"));
				Group group = getGroupById(groupId);
				if (group != null) {
					groups.add(group);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error in paged group search", e);
		}
		return groups;
	}

	// Implement các method count
	private int getTotalUsersCount(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return 0;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"username", "fullname", "email", "bio", "username_tokens", "fullname_tokens", "account_all_fields", "account_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
			return Math.toIntExact(hits.totalHits.value);
		} catch (IOException | ParseException e) {
			LOGGER.error("Error counting users", e);
			return 0;
		}
	}

	private int getTotalPostsContentCount(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return 0;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_content", "post_content_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
			return Math.toIntExact(hits.totalHits.value);
		} catch (IOException | ParseException e) {
			LOGGER.error("Error counting posts by content", e);
			return 0;
		}
	}

	private int getTotalPostsHashtagCount(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return 0;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_hashtags"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
			return Math.toIntExact(hits.totalHits.value);
		} catch (IOException | ParseException e) {
			LOGGER.error("Error counting posts by hashtag", e);
			return 0;
		}
	}

	private int getTotalGroupsCount(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return 0;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"group_name", "group_description", "group_name_tokens", "group_description_tokens", "group_all_fields", "group_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
			return Math.toIntExact(hits.totalHits.value);
		} catch (IOException | ParseException e) {
			LOGGER.error("Error counting groups", e);
			return 0;
		}
	}

	// Thêm vào SearchDAO để test
	public static void testPaging() {
		SearchDAO searchDAO = new SearchDAO();

		System.out.println("=== Testing Paged Search ===");

		// Test với keyword "hi"
		String keyword = "hi";
		String category = "users";
		int limit = 10;
		int offset = 0;

		System.out.println("\n1. Testing total count:");
		int totalCount = searchDAO.getTotalUsersCount(keyword);
		System.out.println("Total users for '" + keyword + "': " + totalCount);

		System.out.println("\n2. Testing paged search:");
		Map<String, Object> pagedResults = searchDAO.searchCategoryPaged(keyword, category, limit, offset);
		System.out.println("Results: " + pagedResults.get("results"));
		System.out.println("Total count: " + pagedResults.get("totalCount"));
		System.out.println("Has more: " + pagedResults.get("hasMore"));

		List<?> results = (List<?>)pagedResults.get("results");
		if (results != null) {
			System.out.println("Found " + results.size() + " items");
			for (Object result : results) {
				if (result instanceof Account) {
					Account acc = (Account) result;
					System.out.println("  - " + acc.getFullname() + " (" + acc.getUsername() + ")");
				}
			}
		}
	}
}
