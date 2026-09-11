package biblivre.administration.backup.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.oauth.DbxRefreshResult;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import biblivre.core.configurations.Configurations;
import biblivre.core.configurations.ConfigurationsDTO;
import biblivre.core.utils.Constants;

public class DropboxService {
	private static final Logger LOGGER = Logger.getLogger(DropboxService.class);
	private static final String APPLICATION_NAME = "Biblivre";

	public static boolean isConfigured(String schema) {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_DROPBOX_ENABLED)) {
			return false;
		}

		return hasOAuthCredentials(schema) || StringUtils.isNotBlank(getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN));
	}

	public static void uploadBackup(String schema, File backupFile) throws IOException, DbxException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_DROPBOX_ENABLED)) {
			return;
		}

		DbxClientV2 client = createClient(schema);
		logInfo("upload", "Starting Dropbox upload. schema=" + schema + ", file=" + backupFile.getName()
				+ ", size=" + backupFile.length());

		try (InputStream in = new FileInputStream(backupFile)) {
			FileMetadata metadata = client.files().uploadBuilder("/" + backupFile.getName())
					.uploadAndFinish(in);
			logInfo("upload", "Dropbox upload finished. schema=" + schema + ", file=" + metadata.getName()
					+ ", path=" + metadata.getPathDisplay() + ", id=" + metadata.getId());
		} catch (DbxException e) {
			throw wrapDropboxError("upload", e);
		} catch (RuntimeException e) {
			throw wrapDropboxError("upload", e);
		}
	}

	public static File downloadBackup(String schema, String filename, File destination) throws IOException, DbxException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_DROPBOX_ENABLED)) {
			IOException error = new IOException("Dropbox backup is not enabled.");
			logError("download", error.getMessage(), error);
			throw error;
		}

		DbxClientV2 client = createClient(schema);

		try (OutputStream out = new FileOutputStream(destination)) {
			client.files().downloadBuilder("/" + filename).download(out);
		} catch (DbxException e) {
			throw wrapDropboxError("download", e);
		} catch (RuntimeException e) {
			throw wrapDropboxError("download", e);
		}

		return destination;
	}

	public static List<String> listBackups(String schema) throws IOException, DbxException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_DROPBOX_ENABLED)) {
			return new ArrayList<String>();
		}

		DbxClientV2 client = createClient(schema);
		List<String> files = new ArrayList<String>();

		try {
			ListFolderResult result = client.files().listFolder("");
			while (true) {
				for (Metadata metadata : result.getEntries()) {
					if (metadata instanceof FileMetadata) {
						String name = metadata.getName();
						if (isBackupFile(name)) {
							files.add(name);
						}
					}
				}
				if (!result.getHasMore()) {
					break;
				}
				result = client.files().listFolderContinue(result.getCursor());
			}
		} catch (DbxException e) {
			throw wrapDropboxError("list", e);
		} catch (RuntimeException e) {
			throw wrapDropboxError("list", e);
		}

		return files;
	}

	public static OAuthClientCredentials resolveOAuthClientCredentials(String schema, String appKey, String appSecret) {
		String resolvedAppKey = StringUtils.trimToEmpty(appKey);
		String resolvedAppSecret = StringUtils.trimToEmpty(appSecret);

		if (resolvedAppKey.isEmpty()) {
			resolvedAppKey = getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_APP_KEY);
		}
		if (resolvedAppSecret.isEmpty()) {
			resolvedAppSecret = getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_APP_SECRET);
		}

		return new OAuthClientCredentials(resolvedAppKey, resolvedAppSecret);
	}

	public static String createAuthorizationUrl(OAuthClientCredentials credentials, String redirectUri, DbxSessionStore sessionStore, String state) {
		DbxWebAuth.Request request = DbxWebAuth.newRequestBuilder()
				.withRedirectUri(redirectUri, sessionStore)
				.withState(state)
				.withTokenAccessType(TokenAccessType.OFFLINE)
				.build();

		return createWebAuth(credentials).authorize(request);
	}

	public static DbxAuthFinish exchangeAuthorizationCode(OAuthClientCredentials credentials, String redirectUri,
			DbxSessionStore sessionStore, String code, String state) throws DbxException, DbxWebAuth.BadRequestException,
			DbxWebAuth.BadStateException, DbxWebAuth.CsrfException, DbxWebAuth.NotApprovedException, DbxWebAuth.ProviderException {
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("code", new String[] { code });
		params.put("state", new String[] { state });

		return createWebAuth(credentials).finishFromRedirect(redirectUri, sessionStore, params);
	}

	public static String getAccountEmailFromAccessToken(String accessToken) throws DbxException {
		DbxClientV2 client = new DbxClientV2(createRequestConfig(), accessToken);
		FullAccount account = client.users().getCurrentAccount();
		return account == null ? "" : StringUtils.trimToEmpty(account.getEmail());
	}

	private static DbxClientV2 createClient(String schema) throws IOException, DbxException {
		DbxRequestConfig config = createRequestConfig();
		String appKey = getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_APP_KEY);
		String appSecret = getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_APP_SECRET);
		String accessToken = getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN);
		String refreshToken = getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_REFRESH_TOKEN);
		Long expiresAt = getLongConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN_EXPIRES_AT);

		if (StringUtils.isNotBlank(refreshToken)) {
			if (StringUtils.isBlank(appKey) || StringUtils.isBlank(appSecret)) {
				IOException error = new IOException("Dropbox App Key and Secret Key are required when using Refresh Token.");
				logError("authentication", error.getMessage(), error);
				throw error;
			}

			String currentAccessToken = StringUtils.defaultIfBlank(accessToken, "expired");
			Long currentExpiresAt = expiresAt == null ? Long.valueOf(0L) : expiresAt;
			DbxCredential credential = new DbxCredential(currentAccessToken, currentExpiresAt, refreshToken, appKey, appSecret);
			if (StringUtils.isBlank(accessToken) || credential.aboutToExpire()) {
				refreshAndSaveAccessToken(schema, config, credential);
			}
			logInfo("authentication", "Using Dropbox OAuth refresh-token authentication for schema=" + schema + ".");
			return new DbxClientV2(config, credential);
		}

		if (StringUtils.isBlank(accessToken)) {
			IOException error = new IOException("Dropbox Access Token is missing.");
			logError("authentication", error.getMessage(), error);
			throw error;
		}

		logInfo("authentication", "Using Dropbox Access Token authentication for schema=" + schema
				+ ". Configure App Key, Secret Key and Refresh Token for persistent OAuth.");
		return new DbxClientV2(config, accessToken);
	}

	private static void refreshAndSaveAccessToken(String schema, DbxRequestConfig config, DbxCredential credential) throws DbxException {
		DbxRefreshResult refreshResult = credential.refresh(config);
		List<ConfigurationsDTO> configs = new ArrayList<ConfigurationsDTO>();
		configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN, refreshResult.getAccessToken()));
		configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN_EXPIRES_AT, String.valueOf(refreshResult.getExpiresAt())));
		Configurations.save(schema, configs, Constants.ADMIN_LOGGED_USER_ID);
		logInfo("authentication", "Dropbox OAuth access token refreshed and saved. schema=" + schema);
	}

	private static DbxWebAuth createWebAuth(OAuthClientCredentials credentials) {
		return new DbxWebAuth(createRequestConfig(), new DbxAppInfo(credentials.getAppKey(), credentials.getAppSecret()));
	}

	private static DbxRequestConfig createRequestConfig() {
		return DbxRequestConfig.newBuilder(APPLICATION_NAME).build();
	}

	private static boolean hasOAuthCredentials(String schema) {
		return StringUtils.isNotBlank(getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_APP_KEY))
				&& StringUtils.isNotBlank(getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_APP_SECRET))
				&& StringUtils.isNotBlank(getConfig(schema, Constants.CONFIG_BACKUP_DROPBOX_REFRESH_TOKEN));
	}

	private static String getConfig(String schema, String key) {
		return StringUtils.trimToEmpty(Configurations.getString(schema, key));
	}

	private static Long getLongConfig(String schema, String key) {
		String value = getConfig(schema, key);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static IOException wrapDropboxError(String operation, Exception e) {
		String message = buildDropboxErrorMessage(e);
		if (message.isEmpty()) {
			message = e.getClass().getSimpleName();
		}
		String fullMessage = APPLICATION_NAME + " Dropbox " + operation + " failed: " + message;
		logError(operation, fullMessage, e);
		return new IOException(fullMessage, e);
	}

	private static String buildDropboxErrorMessage(Exception e) {
		StringBuilder message = new StringBuilder(StringUtils.trimToEmpty(e.getMessage()));
		if (e instanceof DbxException) {
			String requestId = StringUtils.trimToEmpty(((DbxException) e).getRequestId());
			if (StringUtils.isNotBlank(requestId)) {
				if (message.length() > 0) {
					message.append(", ");
				}
				message.append("request_id=").append(requestId);
			}
		}
		return message.toString();
	}

	private static void logInfo(String operation, String message) {
		LOGGER.info("[" + APPLICATION_NAME + "][DropboxService][" + operation + "] " + message);
	}

	private static void logError(String operation, String message, Throwable error) {
		LOGGER.error("[" + APPLICATION_NAME + "][DropboxService][" + operation + "] " + message, error);
		System.err.println("[" + APPLICATION_NAME + "][DropboxService][" + operation + "] " + message);
		if (error != null) {
			error.printStackTrace(System.err);
		}
	}

	private static boolean isBackupFile(String name) {
		String lower = StringUtils.trimToEmpty(name).toLowerCase();
		return lower.endsWith(".b4bz") || lower.endsWith(".b5bz");
	}

	public static class OAuthClientCredentials {
		private final String appKey;
		private final String appSecret;

		private OAuthClientCredentials(String appKey, String appSecret) {
			this.appKey = StringUtils.trimToEmpty(appKey);
			this.appSecret = StringUtils.trimToEmpty(appSecret);
		}

		public String getAppKey() {
			return appKey;
		}

		public String getAppSecret() {
			return appSecret;
		}

		public boolean isComplete() {
			return StringUtils.isNotBlank(this.appKey) && StringUtils.isNotBlank(this.appSecret);
		}
	}
}
