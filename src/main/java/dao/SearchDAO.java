package dao;

import model.Account;
import model.Post;
import model.Group;
import model.Hashtag;

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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant; // Import Instant
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

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
			this.indexWriter.commit(); // Ensures a segments file exists even if index is empty

			this.indexReader = DirectoryReader.open(directory);

			LOGGER.info("Lucene index initialized at: {}", Paths.get(INDEX_DIR).toAbsolutePath());

		} catch (IOException e) {
			LOGGER.error("Error initializing Lucene index", e);
			throw new RuntimeException("Could not initialize Lucene index", e);
		}
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

	private Query parseQuery(String keyword, String[] fields, Analyzer analyzer) throws ParseException {
		MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
		parser.setDefaultOperator(org.apache.lucene.queryparser.classic.QueryParser.Operator.OR);

		String processedKeyword = Arrays.stream(keyword.split("\\s+"))
				.map(term -> {
					if (term.contains("*") || term.contains("?")) {
						return term;
					} else {
						return term + "*";
					}
				})
				.collect(Collectors.joining(" "));
		return parser.parse(processedKeyword);
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

				doc.add(new TextField("username", username, Field.Store.YES));
				doc.add(new TextField("fullname", fullname, Field.Store.YES));
				doc.add(new TextField("email", email, Field.Store.YES));
				doc.add(new TextField("bio", bio != null ? bio : "", Field.Store.YES));

				String combinedAccountFields = username + " " + fullname + " " + email + " " + (bio != null ? bio : "");
				doc.add(new TextField("account_all_fields", combinedAccountFields, Field.Store.NO));

				addDocument(doc, accountId);
			}
			LOGGER.info("Indexed accounts.");
		}
	}

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

			while (rs.next()) {
				Document doc = new Document();
				String postId = String.valueOf(rs.getInt("post_id"));
				String postContent = rs.getString("post_content");
				String postHashtags = rs.getString("post_hashtags");

				doc.add(new StringField("id", postId, Field.Store.YES));
				doc.add(new TextField("post_content", postContent != null ? postContent : "", Field.Store.YES));
				doc.add(new TextField("post_hashtags", postHashtags != null ? postHashtags : "", Field.Store.YES));

				addDocument(doc, postId);
			}
			LOGGER.info("Indexed posts.");
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
				doc.add(new TextField("group_name", groupName, Field.Store.YES));
				doc.add(new TextField("group_description", groupDescription != null ? groupDescription : "", Field.Store.YES));

				String combinedGroupFields = groupName + " " + (groupDescription != null ? groupDescription : "");
				doc.add(new TextField("group_all_fields", combinedGroupFields, Field.Store.NO));

				addDocument(doc, groupId);
			}
			LOGGER.info("Indexed groups.");
		}
	}

	// --- Helper methods to fetch full objects from DB by ID ---
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
					post.setPostContent(rs.getString("post_content"));

					int accountId = rs.getInt("account_id");
					Account associatedAccount = getAccountById(accountId);
					post.setAccount(associatedAccount); // Correct: Use setAccount for Account object

					java.sql.Timestamp createTimestamp = rs.getTimestamp("post_create_date");
					post.setPostCreateDate(createTimestamp != null ? createTimestamp.toInstant() : null); // Correct: toInstant() for Instant field
					java.sql.Timestamp updateTimestamp = rs.getTimestamp("post_last_update");
					post.setPostLastUpdate(updateTimestamp != null ? updateTimestamp.toInstant() : null); // Correct: toInstant() for Instant field

					post.setPostPrivacy(rs.getString("post_privacy"));
					post.setPostStatus(rs.getString("post_status"));

					Integer groupId = rs.getObject("group_id", Integer.class);
					if (groupId != null) {
						Group associatedGroup = getGroupById(groupId);
						post.setGroup(associatedGroup); // Correct: Use setGroup for Group object
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
					group.setId(rs.getInt("group_id")); // Correct: maps to Group's 'id' field
					group.setGroupName(rs.getString("group_name"));
					group.setGroupCoverImage(rs.getString("group_cover_image"));
					group.setGroupDescription(rs.getString("group_description"));

					java.sql.Timestamp createTimestamp = rs.getTimestamp("group_create_date");
					group.setGroupCreateDate(createTimestamp != null ? createTimestamp.toInstant() : null); // Correct: toInstant() for Instant field

					group.setGroupStatus(rs.getString("group_status"));
					return group;
				}
			}
		}
		return null;
	}

	// --- Search Methods ---

	public List<Account> searchUsers(String keyword) {
		List<Account> accounts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Search keyword for users is empty or null");
			return accounts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"username", "fullname", "email", "bio", "account_all_fields"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, 10);
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
		List<Post> posts = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Search keyword for posts is empty or null");
			return posts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_content"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, 10);
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
		List<Group> groups = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			LOGGER.warn("Search keyword for groups is empty or null");
			return groups;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"group_name", "group_description", "group_all_fields"};
			Query query = parseQuery(keyword, fields, analyzer);

			TopDocs hits = searcher.search(query, 10);
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
		List<Post> posts = new ArrayList<>();
		if (hashtagKeyword == null || hashtagKeyword.trim().isEmpty()) {
			LOGGER.warn("Search keyword for hashtags is empty or null");
			return posts;
		}

		try {
			IndexSearcher searcher = getSearcher();
			String[] fields = {"post_hashtags"};
			Query query = parseQuery(hashtagKeyword, fields, analyzer);

			TopDocs hits = searcher.search(query, 10);
			LOGGER.info("Found {} hits for post by hashtag keyword '{}'", hits.totalHits.value, hashtagKeyword);

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc);
				int postId = Integer.parseInt(doc.get("id"));
				Post post = getPostById(postId);
				if (post != null) {
					posts.add(post);
				}
			}
		} catch (IOException | ParseException | SQLException e) {
			LOGGER.error("Error executing Lucene search for posts by hashtag with keyword: {}", hashtagKeyword, e);
		}
		return posts;
	}

	public static void main(String[] args) {
		SearchDAO searchDAO = new SearchDAO();
		for(Post post: searchDAO.searchPostsByHashtag("java"))
		{
			System.out.println(post.getPostContent());
		}

		for(Group group: searchDAO.searchGroups("java"))
		{
			System.out.println(group.getGroupName());
		}
	}
}
