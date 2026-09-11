package biblivre.administration.backup.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.FileList;

import biblivre.core.configurations.Configurations;
import biblivre.core.utils.Constants;

public class GoogleDriveService {
	private static final Logger LOGGER = Logger.getLogger(GoogleDriveService.class);
	private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String APPLICATION_NAME = "Biblivre";
	private static final String CREDENTIALS_RESOURCE = "credentials.json";
	private static final String DRIVE_SCOPE_FILE = "https://www.googleapis.com/auth/drive.file";
	private static final String DRIVE_SCOPE_METADATA = "https://www.googleapis.com/auth/drive.metadata.readonly";
	private static OAuthClientCredentials resourceCredentials;

	public static void uploadBackup(String schema, File backupFile) throws IOException, GeneralSecurityException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ENABLED)) {
			return;
		}

		OAuthClientCredentials credentials = resolveOAuthClientCredentials(schema);
		String refreshToken = getTrimmedConfig(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_REFRESH_TOKEN);

		if (!credentials.isComplete() || refreshToken.isEmpty()) {
			throw new IOException("Google Drive configurations are missing (Client ID, Secret, or Refresh Token).");
		}

		Drive drive = createDrive(credentials, refreshToken);

		com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
		fileMetadata.setName(backupFile.getName());

		FileContent mediaContent = new FileContent("application/octet-stream", backupFile);
		drive.files().create(fileMetadata, mediaContent)
				.setFields("id")
				.execute();
	}

	public static File downloadBackup(String schema, String filename, File destination) throws IOException, GeneralSecurityException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ENABLED)) {
			throw new IOException("Google Drive backup is not enabled.");
		}

		OAuthClientCredentials credentials = resolveOAuthClientCredentials(schema);
		String refreshToken = getTrimmedConfig(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_REFRESH_TOKEN);

		if (!credentials.isComplete() || refreshToken.isEmpty()) {
			throw new IOException("Google Drive configurations are missing (Client ID, Secret, or Refresh Token).");
		}

		Drive drive = createDrive(credentials, refreshToken);

		String query = "name = '" + filename.replace("'", "\\'") + "' and trashed = false";
		FileList list = drive.files().list()
				.setQ(query)
				.setOrderBy("modifiedTime desc")
				.setFields("files(id, name, modifiedTime)")
				.execute();

		if (list.getFiles() == null || list.getFiles().isEmpty()) {
			throw new IOException("Backup file not found on Google Drive.");
		}

		String fileId = list.getFiles().get(0).getId();

		try (OutputStream out = new FileOutputStream(destination)) {
			drive.files().get(fileId).executeMediaAndDownloadTo(out);
		}		
		
		return destination;
	}

	public static List<String> listBackups(String schema) throws IOException, GeneralSecurityException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ENABLED)) {
			return new ArrayList<String>();
		}

		OAuthClientCredentials credentials = resolveOAuthClientCredentials(schema);
		String refreshToken = getTrimmedConfig(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_REFRESH_TOKEN);

		if (!credentials.isComplete() || refreshToken.isEmpty()) {
			throw new IOException("Google Drive configurations are missing (Client ID, Secret, or Refresh Token).");
		}

		Drive drive = createDrive(credentials, refreshToken);
		String query = "(name contains '.b4bz' or name contains '.b5bz') and trashed = false";
		FileList list = drive.files().list()
				.setQ(query)
				.setOrderBy("modifiedTime desc")
				.setFields("files(name, modifiedTime)")
				.setPageSize(50)
				.execute();

		List<String> files = new ArrayList<String>();
		if (list.getFiles() != null) {
			for (com.google.api.services.drive.model.File file : list.getFiles()) {
				files.add(file.getName());
			}
		}

		return files;
	}

	public static String getAccountEmail(String schema) throws IOException, GeneralSecurityException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ENABLED)) {
			return null;
		}

		OAuthClientCredentials credentials = resolveOAuthClientCredentials(schema);
		String refreshToken = getTrimmedConfig(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_REFRESH_TOKEN);

		if (!credentials.isComplete() || refreshToken.isEmpty()) {
			throw new IOException("Google Drive configurations are missing (Client ID, Secret, or Refresh Token).");
		}

		Drive drive = createDrive(credentials, refreshToken);
		About about = drive.about().get().setFields("user(emailAddress)").execute();
		if (about == null || about.getUser() == null) {
			return null;
		}

		return about.getUser().getEmailAddress();
	}

	public static String getAccountEmailFromAccessToken(String accessToken) throws IOException {
		Drive drive = createDriveFromAccessToken(accessToken);
		About about = drive.about().get().setFields("user(emailAddress)").execute();
		if (about == null || about.getUser() == null) {
			return null;
		}

		return about.getUser().getEmailAddress();
	}

	public static boolean hasOAuthClientCredentials(String schema) {
		try {
			return resolveOAuthClientCredentials(schema).isComplete();
		} catch (IOException e) {
			return false;
		}
	}

	public static OAuthClientCredentials resolveOAuthClientCredentials(String schema) throws IOException {
		return resolveOAuthClientCredentials(
				schema,
				getTrimmedConfig(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_CLIENT_ID),
				getTrimmedConfig(schema, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_CLIENT_SECRET)
		);
	}

	public static OAuthClientCredentials resolveOAuthClientCredentials(String schema, String clientId, String clientSecret) throws IOException {
		String resolvedClientId = StringUtils.trimToEmpty(clientId);
		String resolvedClientSecret = StringUtils.trimToEmpty(clientSecret);

		if (resolvedClientId.isEmpty() || resolvedClientSecret.isEmpty()) {
			OAuthClientCredentials fromResource = getResourceCredentials();
			if (resolvedClientId.isEmpty()) {
				resolvedClientId = fromResource.getClientId();
			}
			if (resolvedClientSecret.isEmpty()) {
				resolvedClientSecret = fromResource.getClientSecret();
			}
		}

		return new OAuthClientCredentials(resolvedClientId, resolvedClientSecret);
	}

	public static String createAuthorizationUrl(OAuthClientCredentials credentials, String redirectUri, String state) {
		return createAuthorizationCodeFlow(credentials)
				.newAuthorizationUrl()
				.setRedirectUri(redirectUri)
				.setState(state)
				.set("prompt", "consent")
				.build();
	}

	public static GoogleTokenResponse exchangeAuthorizationCode(OAuthClientCredentials credentials, String code, String redirectUri) throws IOException {
		return createAuthorizationCodeFlow(credentials)
				.newTokenRequest(code)
				.setRedirectUri(redirectUri)
				.execute();
	}

	private static Drive createDrive(OAuthClientCredentials credentials, String refreshToken) throws IOException {
		TokenResponse tokenResponse = new TokenResponse().setRefreshToken(refreshToken);
		Credential credential = createAuthorizationCodeFlow(credentials).createAndStoreCredential(tokenResponse, "biblivre");

		try {
			if (!credential.refreshToken()) {
				throw new IOException("Google OAuth token refresh failed");
			}
		} catch (TokenResponseException e) {
			TokenErrorResponse details = e.getDetails();
			String error = details != null ? details.getError() : null;
			String description = details != null ? details.getErrorDescription() : null;
			String message = "Google OAuth token refresh failed";
			if (error != null && !error.trim().isEmpty()) {
				message += " (error=" + error + ")";
			}
			if (description != null && !description.trim().isEmpty()) {
				message += ": " + description;
			}
			LOGGER.error(message, e);
			throw new IOException(message, e);
		}

		return new Drive.Builder(
				HTTP_TRANSPORT,
				JSON_FACTORY,
				credential
		).setApplicationName(APPLICATION_NAME).build();
	}

	private static Drive createDriveFromAccessToken(String accessToken) {
		return new Drive.Builder(
				HTTP_TRANSPORT,
				JSON_FACTORY,
				request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
		).setApplicationName(APPLICATION_NAME).build();
	}

	private static GoogleAuthorizationCodeFlow createAuthorizationCodeFlow(OAuthClientCredentials credentials) {
		return new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT,
				JSON_FACTORY,
				credentials.getClientId(),
				credentials.getClientSecret(),
				Arrays.asList(DRIVE_SCOPE_FILE, DRIVE_SCOPE_METADATA)
		).setAccessType("offline").build();
	}

	private static synchronized OAuthClientCredentials getResourceCredentials() throws IOException {
		if (resourceCredentials != null) {
			return resourceCredentials;
		}

		InputStream input = GoogleDriveService.class.getClassLoader().getResourceAsStream(CREDENTIALS_RESOURCE);
		if (input == null) {
			resourceCredentials = new OAuthClientCredentials("", "");
			return resourceCredentials;
		}

		try {
			String json = IOUtils.toString(input, "UTF-8");
			JSONObject root = new JSONObject(json);
			JSONObject web = root.optJSONObject("web");
			JSONObject installed = root.optJSONObject("installed");
			JSONObject credentials = web != null ? web : installed;

			if (credentials == null) {
				resourceCredentials = new OAuthClientCredentials("", "");
			} else {
				resourceCredentials = new OAuthClientCredentials(
						credentials.optString("client_id", ""),
						credentials.optString("client_secret", "")
				);
			}
			return resourceCredentials;
		} catch (Exception e) {
			throw new IOException("Could not load Google OAuth credentials from " + CREDENTIALS_RESOURCE, e);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	private static String getTrimmedConfig(String schema, String key) {
		String value = Configurations.getString(schema, key);
		return value == null ? "" : value.trim();
	}

	public static class OAuthClientCredentials {
		private final String clientId;
		private final String clientSecret;

		private OAuthClientCredentials(String clientId, String clientSecret) {
			this.clientId = StringUtils.trimToEmpty(clientId);
			this.clientSecret = StringUtils.trimToEmpty(clientSecret);
		}

		public String getClientId() {
			return clientId;
		}

		public String getClientSecret() {
			return clientSecret;
		}

		public boolean isComplete() {
			return StringUtils.isNotBlank(this.clientId) && StringUtils.isNotBlank(this.clientSecret);
		}
	}
}
