package biblivre.administration.backup.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import biblivre.core.configurations.Configurations;
import biblivre.core.utils.Constants;

public class ProtonDriveService {
	public static void uploadBackup(String schema, File backupFile) throws IOException, InterruptedException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_PROTON_ENABLED)) {
			return;
		}

		String rclonePath = Configurations.getString(schema, Constants.CONFIG_BACKUP_PROTON_RCLONE_PATH);
		String remote = Configurations.getString(schema, Constants.CONFIG_BACKUP_PROTON_RCLONE_REMOTE);

		if (rclonePath.isEmpty() || remote.isEmpty()) {
			throw new IOException("Proton rclone path or remote is missing.");
		}

		String target = joinRemote(remote, backupFile.getName());
		runCommand(rclonePath, "copyto", backupFile.getAbsolutePath(), target);
	}

	public static File downloadBackup(String schema, String filename, File destination) throws IOException, InterruptedException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_PROTON_ENABLED)) {
			throw new IOException("Proton Drive backup is not enabled.");
		}

		String rclonePath = Configurations.getString(schema, Constants.CONFIG_BACKUP_PROTON_RCLONE_PATH);
		String remote = Configurations.getString(schema, Constants.CONFIG_BACKUP_PROTON_RCLONE_REMOTE);

		if (rclonePath.isEmpty() || remote.isEmpty()) {
			throw new IOException("Proton rclone path or remote is missing.");
		}

		String source = joinRemote(remote, filename);
		runCommand(rclonePath, "copyto", source, destination.getAbsolutePath());

		if (!destination.exists()) {
			throw new IOException("Proton Drive download failed.");
		}

		return destination;
	}

	public static List<String> listBackups(String schema) throws IOException, InterruptedException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_PROTON_ENABLED)) {
			return new ArrayList<String>();
		}

		String rclonePath = Configurations.getString(schema, Constants.CONFIG_BACKUP_PROTON_RCLONE_PATH);
		String remote = Configurations.getString(schema, Constants.CONFIG_BACKUP_PROTON_RCLONE_REMOTE);

		if (rclonePath.isEmpty() || remote.isEmpty()) {
			throw new IOException("Proton rclone path or remote is missing.");
		}

		String output = runCommandCapture(rclonePath, "lsf", "--max-depth", "1", remote);
		List<String> files = new ArrayList<String>();
		for (String line : output.split("\\r?\\n")) {
			String name = line.trim();
			if (isBackupFile(name)) {
				files.add(name);
			}
		}
		return files;
	}

	private static String joinRemote(String remote, String name) {
		if (remote.endsWith("/")) {
			return remote + name;
		}
		return remote + "/" + name;
	}

	private static void runCommand(String command, String... args) throws IOException, InterruptedException {
		String[] cmd = new String[args.length + 1];
		cmd[0] = command;
		System.arraycopy(args, 0, cmd, 1, args.length);
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		String output = readOutput(p);
		int exit = p.waitFor();
		if (exit != 0) {
			throw new IOException("Proton rclone failed: " + output);
		}
	}

	private static String runCommandCapture(String command, String... args) throws IOException, InterruptedException {
		String[] cmd = new String[args.length + 1];
		cmd[0] = command;
		System.arraycopy(args, 0, cmd, 1, args.length);
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		String output = readOutput(p);
		int exit = p.waitFor();
		if (exit != 0) {
			throw new IOException("Proton rclone failed: " + output);
		}
		return output;
	}

	private static String readOutput(Process p) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		}
		return sb.toString();
	}

	private static boolean isBackupFile(String name) {
		String lower = name.toLowerCase();
		return lower.endsWith(".b4bz") || lower.endsWith(".b5bz");
	}
}
