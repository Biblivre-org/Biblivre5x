package biblivre.administration.backup.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.box.sdkgen.box.errors.BoxAPIError;
import com.box.sdkgen.box.developertokenauth.BoxDeveloperTokenAuth;
import com.box.sdkgen.box.errors.BoxSDKError;
import com.box.sdkgen.box.errors.ResponseInfo;
import com.box.sdkgen.box.oauth.BoxOAuth;
import com.box.sdkgen.box.oauth.OAuthConfig;
import com.box.sdkgen.box.tokenstorage.TokenStorage;
import com.box.sdkgen.client.BoxClient;
import com.box.sdkgen.managers.folders.GetFolderItemsQueryParams;
import com.box.sdkgen.managers.uploads.UploadFileRequestBody;
import com.box.sdkgen.managers.uploads.UploadFileRequestBodyAttributesField;
import com.box.sdkgen.managers.uploads.UploadFileRequestBodyAttributesParentField;
import com.box.sdkgen.schemas.accesstoken.AccessToken;
import com.box.sdkgen.schemas.filefull.FileFull;
import com.box.sdkgen.schemas.files.Files;
import com.box.sdkgen.schemas.item.Item;
import com.box.sdkgen.schemas.items.Items;

import biblivre.core.configurations.Configurations;
import biblivre.core.configurations.ConfigurationsDTO;
import biblivre.core.utils.Constants;

public class BoxService {
	private static final Logger LOGGER = Logger.getLogger(BoxService.class);
	private static final String APPLICATION_NAME = "Biblivre";

	public static boolean isConfigured(String schema) {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_BOX_ENABLED)) {
			return false;
		}

		return hasOAuthCredentials(schema) || hasDeveloperToken(schema);
	}

	public static void uploadBackup(String schema, File backupFile) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_BOX_ENABLED)) {
			return;
		}

		BoxClient client = createClient(schema);
		String folderId = getFolderId(schema);

		logInfo("upload", "Starting Box upload. schema=" + schema + ", folder_id=" + folderId
				+ ", file=" + backupFile.getName() + ", size=" + backupFile.length());

		try (FileInputStream input = new FileInputStream(backupFile)) {
			UploadFileRequestBodyAttributesField attributes = new UploadFileRequestBodyAttributesField(
					backupFile.getName(),
					new UploadFileRequestBodyAttributesParentField(folderId)
			);
			UploadFileRequestBody requestBody = new UploadFileRequestBody.Builder(attributes, input)
					.fileFileName(backupFile.getName())
					.fileContentType("application/octet-stream")
					.build();
			Files uploadedFiles = client.getUploads().uploadFile(requestBody);
			logUploadSuccess(schema, folderId, backupFile, uploadedFiles);
		} catch (BoxSDKError e) {
			throw wrapBoxError("upload", e);
		} catch (RuntimeException e) {
			throw wrapBoxError("upload", e);
		}
	}

	public static File downloadBackup(String schema, String filename, File destination) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_BOX_ENABLED)) {
			IOException error = new IOException("Box backup is not enabled.");
			logError("download", error.getMessage(), error);
			throw error;
		}

		BoxClient client = createClient(schema);
		String folderId = getFolderId(schema);
		String fileId = findFileId(client, folderId, filename);

		if (fileId == null) {
			IOException error = new IOException("Backup file not found on Box.");
			logError("download", error.getMessage(), error);
			throw error;
		}

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destination))) {
			client.getDownloads().downloadFileToOutputStream(fileId, out);
		} catch (BoxSDKError e) {
			throw wrapBoxError("download", e);
		} catch (RuntimeException e) {
			throw wrapBoxError("download", e);
		}

		return destination;
	}

	public static List<String> listBackups(String schema) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_BOX_ENABLED)) {
			return new ArrayList<String>();
		}

		BoxClient client = createClient(schema);
		String folderId = getFolderId(schema);

		List<String> files = new ArrayList<String>();
		for (Item item : listFolderItems(client, folderId)) {
			String name = item.getName();
			if (isFile(item) && isBackupFile(name)) {
				files.add(name);
			}
		}

		return files;
	}

	private static BoxClient createClient(String schema) throws IOException {
		String clientId = getConfig(schema, Constants.CONFIG_BACKUP_BOX_CLIENT_ID);
		String clientSecret = getConfig(schema, Constants.CONFIG_BACKUP_BOX_CLIENT_SECRET);
		String accessToken = getConfig(schema, Constants.CONFIG_BACKUP_BOX_ACCESS_TOKEN);
		String refreshToken = getConfig(schema, Constants.CONFIG_BACKUP_BOX_REFRESH_TOKEN);

		if (isOAuthConfigured(clientId, clientSecret, refreshToken)) {
			return createOAuthClient(schema, clientId, clientSecret, accessToken, refreshToken);
		}

		if (accessToken.isEmpty()) {
			IOException error = new IOException("Box authentication is missing. Configure OAuth client_id/client_secret/refresh_token or a Developer Token.");
			logError("authentication", error.getMessage(), error);
			throw error;
		}

		// Developer Token is recommended by the Box SDK only for development/testing.
		logInfo("authentication", "Using Box Developer Token authentication for schema=" + schema
				+ ". Developer Tokens are short-lived and intended for development/testing.");
		return new BoxClient(new BoxDeveloperTokenAuth(accessToken));
	}

	private static BoxClient createOAuthClient(String schema, String clientId, String clientSecret, String accessToken, String refreshToken) throws IOException {
		BoxConfigurationTokenStorage tokenStorage = new BoxConfigurationTokenStorage(schema, accessToken, refreshToken);
		OAuthConfig oauthConfig = new OAuthConfig.Builder(clientId, clientSecret)
				.tokenStorage(tokenStorage)
				.build();
		BoxOAuth auth = new BoxOAuth(oauthConfig);

		try {
			auth.refreshToken();
			logInfo("authentication", "Using Box OAuth authentication for schema=" + schema + ".");
			return new BoxClient(auth);
		} catch (BoxSDKError e) {
			throw wrapBoxError("authentication", e);
		} catch (RuntimeException e) {
			throw wrapBoxError("authentication", e);
		}
	}

	private static boolean hasDeveloperToken(String schema) {
		return StringUtils.isNotBlank(getConfig(schema, Constants.CONFIG_BACKUP_BOX_ACCESS_TOKEN));
	}

	private static boolean hasOAuthCredentials(String schema) {
		return isOAuthConfigured(
				getConfig(schema, Constants.CONFIG_BACKUP_BOX_CLIENT_ID),
				getConfig(schema, Constants.CONFIG_BACKUP_BOX_CLIENT_SECRET),
				getConfig(schema, Constants.CONFIG_BACKUP_BOX_REFRESH_TOKEN)
		);
	}

	private static boolean isOAuthConfigured(String clientId, String clientSecret, String refreshToken) {
		return StringUtils.isNotBlank(clientId)
				&& StringUtils.isNotBlank(clientSecret)
				&& StringUtils.isNotBlank(refreshToken);
	}

	private static String getConfig(String schema, String key) {
		return StringUtils.trimToEmpty(Configurations.getString(schema, key));
	}

	private static String getFolderId(String schema) {
		String folderId = StringUtils.trimToEmpty(Configurations.getString(schema, Constants.CONFIG_BACKUP_BOX_FOLDER_ID));
		return folderId.isEmpty() ? "0" : folderId;
	}

	private static String findFileId(BoxClient client, String folderId, String filename) throws IOException {
		for (Item item : listFolderItems(client, folderId)) {
			if (isFile(item) && StringUtils.equals(item.getName(), filename)) {
				return item.getId();
			}
		}
		return null;
	}

	private static List<Item> listFolderItems(BoxClient client, String folderId) throws IOException {
		List<Item> result = new ArrayList<Item>();
		String marker = null;

		try {
			do {
				GetFolderItemsQueryParams.Builder queryBuilder = new GetFolderItemsQueryParams.Builder()
						.usemarker(true)
						.limit(1000L)
						.fields(Arrays.asList("id", "name", "type"));
				if (StringUtils.isNotBlank(marker)) {
					queryBuilder.marker(marker);
				}

				Items items = client.getFolders().getFolderItems(folderId, queryBuilder.build());
				if (items.getEntries() != null) {
					result.addAll(items.getEntries());
				}
				marker = StringUtils.trimToNull(items.getNextMarker());
			} while (marker != null);
		} catch (BoxSDKError e) {
			throw wrapBoxError("list", e);
		} catch (RuntimeException e) {
			throw wrapBoxError("list", e);
		}

		return result;
	}

	private static IOException wrapBoxError(String operation, RuntimeException e) {
		String message = buildBoxErrorMessage(e);
		if (message.isEmpty()) {
			message = e.getClass().getSimpleName();
		}
		String fullMessage = APPLICATION_NAME + " Box " + operation + " failed: " + message;
		logError(operation, fullMessage, e);
		return new IOException(fullMessage, e);
	}

	private static String buildBoxErrorMessage(RuntimeException e) {
		if (e instanceof BoxAPIError) {
			BoxAPIError apiError = (BoxAPIError) e;
			ResponseInfo responseInfo = apiError.getResponseInfo();
			if (responseInfo == null) {
				return StringUtils.trimToEmpty(apiError.getMessage());
			}

			StringBuilder message = new StringBuilder();
			message.append("status=").append(responseInfo.getStatusCode());
			appendIfNotBlank(message, "code", responseInfo.getCode());
			appendIfNotBlank(message, "request_id", responseInfo.getRequestId());
			appendIfNotBlank(message, "help_url", responseInfo.getHelpUrl());
			appendIfNotBlank(message, "response", StringUtils.abbreviate(responseInfo.getRawBody(), 1000));
			return message.toString();
		}

		return StringUtils.trimToEmpty(e.getMessage());
	}

	private static void appendIfNotBlank(StringBuilder message, String key, String value) {
		if (StringUtils.isNotBlank(value)) {
			message.append(", ").append(key).append("=").append(value);
		}
	}

	private static void logUploadSuccess(String schema, String folderId, File backupFile, Files uploadedFiles) {
		String fileId = "";
		if (uploadedFiles != null && uploadedFiles.getEntries() != null && !uploadedFiles.getEntries().isEmpty()) {
			FileFull uploaded = uploadedFiles.getEntries().get(0);
			if (uploaded != null) {
				fileId = StringUtils.trimToEmpty(uploaded.getId());
			}
		}

		String message = "Box upload finished. schema=" + schema + ", folder_id=" + folderId
				+ ", file=" + backupFile.getName() + ", size=" + backupFile.length();
		if (StringUtils.isNotBlank(fileId)) {
			message += ", box_file_id=" + fileId;
		}
		logInfo("upload", message);
	}

	private static void logInfo(String operation, String message) {
		LOGGER.info("[" + APPLICATION_NAME + "][BoxService][" + operation + "] " + message);
	}

	private static void logError(String operation, String message, Throwable error) {
		LOGGER.error("[" + APPLICATION_NAME + "][BoxService][" + operation + "] " + message, error);
		System.err.println("[" + APPLICATION_NAME + "][BoxService][" + operation + "] " + message);
		if (error != null) {
			error.printStackTrace(System.err);
		}
	}

	private static boolean isFile(Item item) {
		return item != null && item.isFileFull() && "file".equals(item.getType());
	}

	private static boolean isBackupFile(String name) {
		String lower = StringUtils.trimToEmpty(name).toLowerCase();
		return lower.endsWith(".b4bz") || lower.endsWith(".b5bz");
	}

	private static class BoxConfigurationTokenStorage implements TokenStorage {
		private final String schema;
		private AccessToken token;

		private BoxConfigurationTokenStorage(String schema, String accessToken, String refreshToken) {
			this.schema = schema;
			this.token = new AccessToken.Builder()
					.accessToken(StringUtils.trimToNull(accessToken))
					.refreshToken(StringUtils.trimToNull(refreshToken))
					.build();
		}

		@Override
		public void store(AccessToken token) {
			this.token = token;
			if (token == null) {
				return;
			}

			List<ConfigurationsDTO> configs = new ArrayList<ConfigurationsDTO>();
			if (StringUtils.isNotBlank(token.getAccessToken())) {
				configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_BOX_ACCESS_TOKEN, token.getAccessToken()));
			}
			if (StringUtils.isNotBlank(token.getRefreshToken())) {
				configs.add(new ConfigurationsDTO(Constants.CONFIG_BACKUP_BOX_REFRESH_TOKEN, token.getRefreshToken()));
			}

			if (!configs.isEmpty()) {
				Configurations.save(this.schema, configs, Constants.ADMIN_LOGGED_USER_ID);
				logInfo("authentication", "Box OAuth token refreshed and saved. schema=" + this.schema);
			}
		}

		@Override
		public AccessToken get() {
			return this.token;
		}

		@Override
		public void clear() {
			this.token = null;
		}
	}
}
