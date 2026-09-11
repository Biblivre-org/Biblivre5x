package biblivre.administration.backup.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import biblivre.core.configurations.Configurations;
import biblivre.core.utils.Constants;

public class OneDriveService {

	public static void uploadBackup(String schema, File backupFile) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_ONEDRIVE_ENABLED)) {
			return;
		}

		String clientId = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_ID);
		String clientSecret = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_SECRET);
		String refreshToken = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_REFRESH_TOKEN);

		if (clientId.isEmpty() || clientSecret.isEmpty() || refreshToken.isEmpty()) {
			throw new IOException("OneDrive configurations are missing (Client ID, Secret, or Refresh Token).");
		}

		String accessToken = refreshAccessToken(clientId, clientSecret, refreshToken);
		String encodedName = URLEncoder.encode(backupFile.getName(), "UTF-8").replace("+", "%20");
		String url = "https://graph.microsoft.com/v1.0/me/drive/root:/" + encodedName + ":/content";

		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("Authorization", "Bearer " + accessToken);
		connection.setRequestProperty("Content-Type", "application/octet-stream");
		connection.setRequestMethod("PUT");
		connection.setDoOutput(true);
		connection.setFixedLengthStreamingMode(backupFile.length());

		try (InputStream input = new FileInputStream(backupFile); OutputStream output = connection.getOutputStream()) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) > 0) {
				output.write(buffer, 0, read);
			}
		}

		int status = connection.getResponseCode();
		if (status != 200 && status != 201) {
			throw new IOException("OneDrive upload failed with status " + status);
		}
	}

	public static File downloadBackup(String schema, String filename, File destination) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_ONEDRIVE_ENABLED)) {
			throw new IOException("OneDrive backup is not enabled.");
		}

		String clientId = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_ID);
		String clientSecret = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_SECRET);
		String refreshToken = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_REFRESH_TOKEN);

		if (clientId.isEmpty() || clientSecret.isEmpty() || refreshToken.isEmpty()) {
			throw new IOException("OneDrive configurations are missing (Client ID, Secret, or Refresh Token).");
		}

		String accessToken = refreshAccessToken(clientId, clientSecret, refreshToken);
		String encodedName = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
		String url = "https://graph.microsoft.com/v1.0/me/drive/root:/" + encodedName + ":/content";

		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("Authorization", "Bearer " + accessToken);
		connection.setRequestMethod("GET");

		int status = connection.getResponseCode();
		if (status != 200) {
			throw new IOException("OneDrive download failed with status " + status);
		}

		try (InputStream input = connection.getInputStream(); FileOutputStream output = new FileOutputStream(destination)) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) > 0) {
				output.write(buffer, 0, read);
			}
		}

		return destination;
	}

	public static List<String> listBackups(String schema) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_ONEDRIVE_ENABLED)) {
			return new ArrayList<String>();
		}

		String clientId = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_ID);
		String clientSecret = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_SECRET);
		String refreshToken = Configurations.getString(schema, Constants.CONFIG_BACKUP_ONEDRIVE_REFRESH_TOKEN);

		if (clientId.isEmpty() || clientSecret.isEmpty() || refreshToken.isEmpty()) {
			throw new IOException("OneDrive configurations are missing (Client ID, Secret, or Refresh Token).");
		}

		String accessToken = refreshAccessToken(clientId, clientSecret, refreshToken);
		String url = "https://graph.microsoft.com/v1.0/me/drive/root/children?$select=name,folder&$top=200";

		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("Authorization", "Bearer " + accessToken);
		connection.setRequestMethod("GET");

		int status = connection.getResponseCode();
		if (status != 200) {
			throw new IOException("OneDrive list failed with status " + status);
		}

		StringBuilder response = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		}

		JSONObject json = new JSONObject(response.toString());
		JSONArray values = json.optJSONArray("value");
		List<String> files = new ArrayList<String>();
		if (values != null) {
			for (int i = 0; i < values.length(); i++) {
				JSONObject item = values.getJSONObject(i);
				if (item.has("folder")) {
					continue;
				}
				String name = item.optString("name", "");
				if (isBackupFile(name)) {
					files.add(name);
				}
			}
		}

		return files;
	}

	private static String refreshAccessToken(String clientId, String clientSecret, String refreshToken) throws IOException {
		String tokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
		String body = "client_id=" + URLEncoder.encode(clientId, "UTF-8")
				+ "&scope=" + URLEncoder.encode("https://graph.microsoft.com/.default offline_access", "UTF-8")
				+ "&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8")
				+ "&grant_type=refresh_token"
				+ "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8");

		HttpURLConnection connection = (HttpURLConnection) new URL(tokenUrl).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setDoOutput(true);

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
			writer.write(body);
		}

		int status = connection.getResponseCode();
		if (status != 200) {
			throw new IOException("OneDrive token refresh failed with status " + status);
		}

		StringBuilder response = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		}

		JSONObject json = new JSONObject(response.toString());
		return json.getString("access_token");
	}

	private static boolean isBackupFile(String name) {
		String lower = name.toLowerCase();
		return lower.endsWith(".b4bz") || lower.endsWith(".b5bz");
	}
}
