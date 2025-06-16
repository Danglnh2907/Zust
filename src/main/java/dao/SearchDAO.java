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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SearchDAO implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchDAO.class);
	private static final String INDEX_DIR = "lucene_index";

	// Cache để track thay đổi data
	private static final Map<String, Integer> dataCountCache = new ConcurrentHashMap<>();
	private static LocalDateTime lastFullIndexTime = LocalDateTime.now().minusHours(1);

	// Thời gian auto-refresh
	private static final int AUTO_REFRESH_MINUTES = 0;

	private final Analyzer analyzer;
	private final Directory directory;
	private IndexWriter indexWriter;
	private DirectoryReader indexReader;

	public SearchDAO() {
		try {
			this.analyzer = new StandardAnalyzer();

			if (!Files.exists(Paths.get(INDEX_DIR))) {
				Files.createDirectories(Paths.get(INDEX_DIR));
			}

			this.directory = FSDirectory.open(Paths.get(INDEX_DIR));

			// Clear existing incompatible index
			boolean shouldClearIndex = false;
			try {
				DirectoryReader.open(directory).close();
			} catch (Exception e) {
				// If we can't open the existing index, it's incompatible
				LOGGER.info("Existing index is incompatible, will recreate it");
				shouldClearIndex = true;
			}

			// Create IndexWriter with proper configuration
			IndexWriterConfig config = new IndexWriterConfig(analyzer);

			if (shouldClearIndex) {
				config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // Force recreate
			} else {
				config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			}

			try {
				this.indexWriter = new IndexWriter(directory, config);
			} catch (LockObtainFailedException e) {
				LOGGER.warn("Index is locked, trying to create new IndexWriter...");
				Thread.sleep(1000);
				this.indexWriter = new IndexWriter(directory, config);
			}

			try {
				this.indexReader = DirectoryReader.open(directory);
			} catch (IOException e) {
				LOGGER.info("No existing index found, creating new one...");
				this.indexWriter.commit();
				this.indexReader = DirectoryReader.open(directory);
			}

			// If we cleared the index, reindex all data
			if (shouldClearIndex) {
				LOGGER.info("Reindexing all data due to version incompatibility...");
				indexAllDataToLucene();
			}

			LOGGER.info("Search index initialized");

		} catch (IOException | InterruptedException e) {
			LOGGER.error("Error initializing index", e);
			throw new RuntimeException("Could not initialize search index", e);
		}
	}

	/**
	 * Check if data has changed and update index accordingly
	 */
	private void checkAndUpdateIndex() {
		try {
			LocalDateTime now = LocalDateTime.now();

			if (ChronoUnit.MINUTES.between(lastFullIndexTime, now) >= AUTO_REFRESH_MINUTES) {
				if (hasDataChanged()) {
					LOGGER.info("Data changes detected, updating index...");
					indexAllDataToLucene();
					lastFullIndexTime = now;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Error during auto-index check", e);
		}
	}

	/**
	 * Check if database data has changed
	 */
	private boolean hasDataChanged() {
		try {
			int currentPostCount = getCurrentPostCount();
			int currentAccountCount = getCurrentAccountCount();
			int currentGroupCount = getCurrentGroupCount();

			Integer cachedPostCount = dataCountCache.get("posts");
			Integer cachedAccountCount = dataCountCache.get("accounts");
			Integer cachedGroupCount = dataCountCache.get("groups");

			boolean hasChanged = !Objects.equals(cachedPostCount, currentPostCount) ||
					!Objects.equals(cachedAccountCount, currentAccountCount) ||
					!Objects.equals(cachedGroupCount, currentGroupCount);

			if (hasChanged) {
				dataCountCache.put("posts", currentPostCount);
				dataCountCache.put("accounts", currentAccountCount);
				dataCountCache.put("groups", currentGroupCount);

				LOGGER.info("Data changed - Posts: {}->{}, Accounts: {}->{}, Groups: {}->{}",
						cachedPostCount, currentPostCount,
						cachedAccountCount, currentAccountCount,
						cachedGroupCount, currentGroupCount);
			}

			return hasChanged;
		} catch (SQLException e) {
			LOGGER.error("Error checking data changes", e);
			return true;
		}
	}

	private int getCurrentPostCount() throws SQLException {
		String sql = "SELECT COUNT(*) FROM post WHERE post_status = 'published'";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}

	private int getCurrentAccountCount() throws SQLException {
		String sql = "SELECT COUNT(*) FROM account WHERE account_status = 'active'";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}

	private int getCurrentGroupCount() throws SQLException {
		String sql = "SELECT COUNT(*) FROM [group] WHERE group_status = 'active'";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}

	private IndexSearcher getSearcher() throws IOException {
		checkAndUpdateIndex();

		DirectoryReader newReader = DirectoryReader.openIfChanged(indexReader);
		if (newReader != null) {
			indexReader.close();
			indexReader = newReader;
		}
		return new IndexSearcher(indexReader);
	}

	private void refreshSearcher() throws IOException {
		DirectoryReader newReader = DirectoryReader.openIfChanged(indexReader);
		if (newReader != null) {
			indexReader.close();
			indexReader = newReader;
		}
	}

	private String normalizeText(String text) {
		if (text == null) return "";
		String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
		normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return normalized.toLowerCase();
	}

	private String generateSearchTokens(String fullName) {
		if (fullName == null || fullName.trim().isEmpty()) {
			return "";
		}

		Set<String> tokens = new HashSet<>();
		String normalized = normalizeText(fullName);
		String[] words = normalized.split("\\s+");

		for (String word : words) {
			if (word.length() > 0) {
				tokens.add(word);

				for (int i = 2; i <= word.length(); i++) {
					tokens.add(word.substring(0, i));
				}
			}
		}

		StringBuilder initials = new StringBuilder();
		for (String word : words) {
			if (word.length() > 0) {
				initials.append(word.charAt(0));
			}
		}

		String initialString = initials.toString();
		for (int i = 1; i <= initialString.length(); i++) {
			tokens.add(initialString.substring(0, i));
		}

		return String.join(" ", tokens);
	}

	private Query createSmartQuery(String keyword, String[] fields) throws ParseException {
		if (keyword == null || keyword.trim().isEmpty()) {
			return new MatchAllDocsQuery();
		}

		String normalizedKeyword = normalizeText(keyword.trim());
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

		// Exact match
		for (String field : fields) {
			Query exactQuery = new TermQuery(new Term(field, normalizedKeyword));
			queryBuilder.add(exactQuery, BooleanClause.Occur.SHOULD);
		}

		// Prefix match
		for (String field : fields) {
			Query prefixQuery = new PrefixQuery(new Term(field, normalizedKeyword));
			queryBuilder.add(prefixQuery, BooleanClause.Occur.SHOULD);
		}

		// Wildcard match
		for (String field : fields) {
			Query wildcardQuery = new WildcardQuery(new Term(field, "*" + normalizedKeyword + "*"));
			queryBuilder.add(wildcardQuery, BooleanClause.Occur.SHOULD);
		}

		if (normalizedKeyword.length() > 3) {
			for (String field : fields) {
				Query fuzzyQuery = new FuzzyQuery(new Term(field, normalizedKeyword), 1);
				queryBuilder.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
			}
		}

		return queryBuilder.build();
	}

	private Query parseQuery(String keyword, String[] fields, Analyzer analyzer) throws ParseException {
		return createSmartQuery(keyword, fields);
	}

	private void addDocument(Document doc, String docId) throws IOException {
		indexWriter.updateDocument(new Term("id", docId), doc);
	}

	private String stripHtmlTags(String html) {
		if (html == null || html.trim().isEmpty()) {
			return "";
		}

		String stripped = html.replaceAll("<[^>]*>", " ");
		stripped = stripped.replaceAll("\\s+", " ");
		stripped = stripped.replace("&nbsp;", " ")
				.replace("&amp;", "&")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&#39;", "'");

		return stripped.trim();
	}

	private String getHashtagsForPost(int postId) throws SQLException {
		String sql = "SELECT STRING_AGG(h.hashtag_name, ' ') WITHIN GROUP (ORDER BY h.hashtag_name) AS hashtags " +
				"FROM tag_hashtag th " +
				"JOIN hashtag h ON th.hashtag_id = h.hashtag_id " +
				"WHERE th.post_id = ?";

		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, postId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("hashtags") != null ? rs.getString("hashtags") : "";
				}
			}
		}
		return "";
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
			refreshSearcher();
			LOGGER.info("All data indexed to Lucene successfully.");
		} catch (IOException | SQLException e) {
			LOGGER.error("Error during batch indexing to Lucene", e);
		}
	}

	private void indexAccounts() throws SQLException, IOException {
		String sql = "SELECT account_id, username, password, fullname, email, phone, gender, dob, avatar, bio, credit, account_status, account_role FROM account WHERE account_status = 'active'";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			int count = 0;
			while (rs.next()) {
				Document doc = new Document();
				String accountId = String.valueOf(rs.getInt("account_id"));
				String username = rs.getString("username");
				String fullname = rs.getString("fullname");
				String email = rs.getString("email");
				String bio = rs.getString("bio");

				doc.add(new StringField("id", accountId, Field.Store.YES));

				doc.add(new TextField("username", normalizeText(username), Field.Store.YES));
				doc.add(new TextField("fullname", normalizeText(fullname), Field.Store.YES));
				doc.add(new TextField("email", normalizeText(email), Field.Store.YES));
				doc.add(new TextField("bio", normalizeText(bio != null ? bio : ""), Field.Store.YES));

				String searchTokens = generateSearchTokens(fullname);
				doc.add(new TextField("fullname_tokens", searchTokens, Field.Store.NO));
				doc.add(new TextField("username_tokens", generateSearchTokens(username), Field.Store.NO));

				String combinedAccountFields = normalizeText(username + " " + fullname + " " + email + " " + (bio != null ? bio : ""));
				doc.add(new TextField("account_all_fields", combinedAccountFields, Field.Store.NO));

				String allTokens = searchTokens + " " + generateSearchTokens(username) + " " + normalizeText(email);
				doc.add(new TextField("account_all_tokens", allTokens, Field.Store.NO));

				addDocument(doc, accountId);
				count++;
			}
			LOGGER.info("Indexed {} accounts", count);
		}
	}

	private void indexPosts() throws SQLException, IOException {
		String sql = "SELECT p.post_id, p.post_content, p.account_id, " +
				"STRING_AGG(h.hashtag_name, ' ') WITHIN GROUP (ORDER BY h.hashtag_name) AS post_hashtags " +
				"FROM post p " +
				"LEFT JOIN tag_hashtag th ON p.post_id = th.post_id " +
				"LEFT JOIN hashtag h ON th.hashtag_id = h.hashtag_id " +
				"WHERE p.post_status = 'published' " +
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

				String plainTextContent = stripHtmlTags(postContent);

				doc.add(new StringField("id", postId, Field.Store.YES));

				doc.add(new TextField("post_content", normalizeText(plainTextContent), Field.Store.YES));
				doc.add(new TextField("post_content_html", postContent != null ? postContent : "", Field.Store.YES));
				doc.add(new TextField("post_hashtags", normalizeText(postHashtags != null ? postHashtags : ""), Field.Store.YES));

				doc.add(new TextField("post_content_tokens", generateSearchTokens(plainTextContent), Field.Store.NO));

				addDocument(doc, postId);
				count++;
			}
			LOGGER.info("Indexed {} posts", count);
		}
	}

	private void indexGroups() throws SQLException, IOException {
		String sql = "SELECT group_id, group_name, group_description FROM [group] WHERE group_status = 'active'";
		try (Connection conn = new DBContext().getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			int count = 0;
			while (rs.next()) {
				Document doc = new Document();
				String groupId = String.valueOf(rs.getInt("group_id"));
				String groupName = rs.getString("group_name");
				String groupDescription = rs.getString("group_description");

				doc.add(new StringField("id", groupId, Field.Store.YES));
				doc.add(new TextField("group_name", normalizeText(groupName), Field.Store.YES));
				doc.add(new TextField("group_description", normalizeText(groupDescription != null ? groupDescription : ""), Field.Store.YES));

				doc.add(new TextField("group_name_tokens", generateSearchTokens(groupName), Field.Store.NO));
				doc.add(new TextField("group_description_tokens", generateSearchTokens(groupDescription), Field.Store.NO));

				String combinedGroupFields = normalizeText(groupName + " " + (groupDescription != null ? groupDescription : ""));
				doc.add(new TextField("group_all_fields", combinedGroupFields, Field.Store.NO));

				String allTokens = generateSearchTokens(groupName) + " " + generateSearchTokens(groupDescription);
				doc.add(new TextField("group_all_tokens", allTokens, Field.Store.NO));

				addDocument(doc, groupId);
				count++;
			}
			LOGGER.info("Indexed {} groups", count);
		}
	}

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
					post.setPostContent(htmlContent);

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

	public List<Account> searchUsers(String keyword) {
		return searchUsersLimited(keyword, 10);
	}

	private List<Account> searchUsersLimited(String keyword, int limit) {
		List<Account> accounts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return accounts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"username", "fullname", "email", "bio", "username_tokens", "fullname_tokens", "account_all_fields", "account_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, limit);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int accountId = Integer.parseInt(doc.get("id"));
				Account account = getAccountById(accountId);
				if (account != null) {
					accounts.add(account);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error searching users", e);
		}
		return accounts;
	}

	public List<Post> searchPostsByContent(String keyword) {
		return searchPostsByContentLimited(keyword, 10);
	}

	private List<Post> searchPostsByContentLimited(String keyword, int limit) {
		List<Post> posts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return posts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_content", "post_content_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, limit);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int postId = Integer.parseInt(doc.get("id"));
				Post post = getPostById(postId);
				if (post != null) {
					posts.add(post);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error searching posts by content", e);
		}
		return posts;
	}

	public List<Post> searchPostsByHashtag(String keyword) {
		return searchPostsByHashtagLimited(keyword, 10);
	}

	private List<Post> searchPostsByHashtagLimited(String keyword, int limit) {
		List<Post> posts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return posts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_hashtags"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, limit);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int postId = Integer.parseInt(doc.get("id"));
				Post post = getPostById(postId);
				if (post != null) {
					posts.add(post);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error searching posts by hashtag", e);
		}
		return posts;
	}

	public List<Group> searchGroups(String keyword) {
		return searchGroupsLimited(keyword, 10);
	}

	private List<Group> searchGroupsLimited(String keyword, int limit) {
		List<Group> groups = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return groups;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"group_name", "group_description", "group_name_tokens", "group_description_tokens", "group_all_fields", "group_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, limit);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int groupId = Integer.parseInt(doc.get("id"));
				Group group = getGroupById(groupId);
				if (group != null) {
					groups.add(group);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error searching groups", e);
		}
		return groups;
	}

	public Map<String, List<?>> searchAll(String keyword) {
		Map<String, List<?>> allResults = new HashMap<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return allResults;
		}

		allResults.put("users", searchUsers(keyword));
		allResults.put("posts_content", searchPostsByContent(keyword));
		allResults.put("posts_hashtag", searchPostsByHashtag(keyword));
		allResults.put("groups", searchGroups(keyword));

		return allResults;
	}

	public Map<String, List<?>> searchAllLimited(String keyword, int limit) {
		Map<String, List<?>> allResults = new HashMap<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return allResults;
		}

		allResults.put("users", searchUsersLimited(keyword, limit));
		allResults.put("posts_content", searchPostsByContentLimited(keyword, limit));
		allResults.put("posts_hashtag", searchPostsByHashtagLimited(keyword, limit));
		allResults.put("groups", searchGroupsLimited(keyword, limit));

		return allResults;
	}

	public Map<String, Object> searchCategoryPaged(String keyword, String category, int limit, int offset) {
		Map<String, Object> result = new HashMap<>();
		List<?> results = new ArrayList<>();
		int totalCount = 0;
		boolean hasMore = false;

		if (keyword == null || keyword.trim().isEmpty()) {
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
			}

			hasMore = (offset + limit) < totalCount;

		} catch (Exception e) {
			LOGGER.error("Error in paged search", e);
		}

		result.put("results", results);
		result.put("totalCount", totalCount);
		result.put("hasMore", hasMore);

		return result;
	}

	private List<Account> searchUsersPaged(String keyword, int limit, int offset) {
		List<Account> accounts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			return accounts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"username", "fullname", "email", "bio", "username_tokens", "fullname_tokens", "account_all_fields", "account_all_tokens"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, offset + limit);

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

	public void forceRefreshIndex() {
		try {
			LOGGER.info("Force refreshing search index...");
			indexAllDataToLucene();
			lastFullIndexTime = LocalDateTime.now();
			LOGGER.info("Search index force refresh completed");
		} catch (Exception e) {
			LOGGER.error("Error during force refresh", e);
		}
	}

	@Override
	public void close() {
		try {
			if (indexReader != null) {
				indexReader.close();
			}
			if (indexWriter != null) {
				indexWriter.close();
			}
			if (directory != null) {
				directory.close();
			}
			if (analyzer != null) {
				analyzer.close();
			}
		} catch (IOException e) {
			LOGGER.error("Error closing SearchDAO resources", e);
		}
	}

	public static void main(String[] args) {
		SearchDAO dao = new SearchDAO();
		dao.searchAll("hi");
	}
}
