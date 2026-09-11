package biblivre.administration.backup.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class PCloudService {
	public static void uploadBackup(String schema, File backupFile) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_PCLOUD_ENABLED)) {
			return;
		}

		String token = Configurations.getString(schema, Constants.CONFIG_BACKUP_PCLOUD_AUTH_TOKEN);
		String path = Configurations.getString(schema, Constants.CONFIG_BACKUP_PCLOUD_PATH);

		if (token.isEmpty()) {
			throw new IOException("pCloud auth token is missing.");
		}

		if (path.isEmpty()) {
			path = "/";
		}

		String boundary = "----BiblivrePCloudBoundary" + System.currentTimeMillis();
		String url = "https://api.pcloud.com/uploadfile?auth=" + URLEncoder.encode(token, "UTF-8") + "&path=" + URLEncoder.encode(path, "UTF-8");
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		try (OutputStream out = new BufferedOutputStream(conn.getOutputStream())) {
			out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
			out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + backupFile.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
			out.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));

			try (InputStream in = new BufferedInputStream(new FileInputStream(backupFile))) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = in.read(buffer)) > 0) {
					out.write(buffer, 0, read);
				}
			}

			out.write("\r\n".getBytes(StandardCharsets.UTF_8));
			out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
		}

		int status = conn.getResponseCode();
		if (status < 200 || status >= 300) {
			throw new IOException("pCloud upload failed with status " + status);
		}

		String body = readAll(conn.getInputStream());
		JSONObject json = new JSONObject(body);
		if (json.optInt("result", -1) != 0) {
			throw new IOException("pCloud upload failed with result " + json.optInt("result", -1));
		}
	}

	public static File downloadBackup(String schema, String filename, File destination) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_PCLOUD_ENABLED)) {
			throw new IOException("pCloud backup is not enabled.");
		}

		String token = Configurations.getString(schema, Constants.CONFIG_BACKUP_PCLOUD_AUTH_TOKEN);
		String path = Configurations.getString(schema, Constants.CONFIG_BACKUP_PCLOUD_PATH);

		if (token.isEmpty()) {
			throw new IOException("pCloud auth token is missing.");
		}

		if (path.isEmpty()) {
			path = "/";
		}

		String filePath = path.endsWith("/") ? path + filename : path + "/" + filename;
		String url = "https://api.pcloud.com/getfilelink?auth=" + URLEncoder.encode(token, "UTF-8") + "&path=" + URLEncoder.encode(filePath, "UTF-8");
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		int status = conn.getResponseCode();
		if (status < 200 || status >= 300) {
			throw new IOException("pCloud getfilelink failed with status " + status);
		}

		String body = readAll(conn.getInputStream());
		JSONObject json = new JSONObject(body);
		if (json.optInt("result", -1) != 0) {
			throw new IOException("pCloud getfilelink failed with result " + json.optInt("result", -1));
		}

		JSONArray hosts = json.optJSONArray("hosts");
		String fileLinkPath = json.optString("path", "");
		if (hosts == null || hosts.length() == 0 || fileLinkPath.isEmpty()) {
			throw new IOException("pCloud link response invalid.");
		}

		String host = hosts.getString(0);
		String downloadUrl = "https://" + host + fileLinkPath;
		HttpURLConnection downloadConn = (HttpURLConnection) new URL(downloadUrl).openConnection();
		int downloadStatus = downloadConn.getResponseCode();
		if (downloadStatus < 200 || downloadStatus >= 300) {
			throw new IOException("pCloud download failed with status " + downloadStatus);
		}

		try (InputStream in = new BufferedInputStream(downloadConn.getInputStream()); OutputStream out = new BufferedOutputStream(new FileOutputStream(destination))) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
		}

		return destination;
	}

	public static List<String> listBackups(String schema) throws IOException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_PCLOUD_ENABLED)) {
			return new ArrayList<String>();
		}

		String token = Configurations.getString(schema, Constants.CONFIG_BACKUP_PCLOUD_AUTH_TOKEN);
		String path = Configurations.getString(schema, Constants.CONFIG_BACKUP_PCLOUD_PATH);

		if (token.isEmpty()) {
			throw new IOException("pCloud auth token is missing.");
		}

		if (path.isEmpty()) {
			path = "/";
		}

		String url = "https://api.pcloud.com/listfolder?auth=" + URLEncoder.encode(token, "UTF-8") + "&path=" + URLEncoder.encode(path, "UTF-8");
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		int status = conn.getResponseCode();
		if (status < 200 || status >= 300) {
			throw new IOException("pCloud listfolder failed with status " + status);
		}

		String body = readAll(conn.getInputStream());
		JSONObject json = new JSONObject(body);
		if (json.optInt("result", -1) != 0) {
			throw new IOException("pCloud listfolder failed with result " + json.optInt("result", -1));
		}

		JSONObject metadata = json.optJSONObject("metadata");
		JSONArray contents = metadata != null ? metadata.optJSONArray("contents") : null;
		List<String> files = new ArrayList<String>();
		if (contents != null) {
			for (int i = 0; i < contents.length(); i++) {
				JSONObject entry = contents.getJSONObject(i);
				if (entry.optBoolean("isfolder", false)) {
					continue;
				}
				String name = entry.optString("name", "");
				if (isBackupFile(name)) {
					files.add(name);
				}
			}
		}

		return files;
	}

	private static String readAll(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[4096];
		int read;
		while ((read = in.read(buffer)) > 0) {
			sb.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
		}
		return sb.toString();
	}

	private static boolean isBackupFile(String name) {
		String lower = name.toLowerCase();
		return lower.endsWith(".b4bz") || lower.endsWith(".b5bz");
	}
}
