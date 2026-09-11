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

public class MegaService {
	public static void uploadBackup(String schema, File backupFile) throws IOException, InterruptedException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_MEGA_ENABLED)) {
			return;
		}

		String cmdPath = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_CMD_PATH);
		String email = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_EMAIL);
		String password = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_PASSWORD);
		String remotePath = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_REMOTE_PATH);

		if (cmdPath.isEmpty() || email.isEmpty() || password.isEmpty()) {
			throw new IOException("MEGA command path or credentials are missing.");
		}

		if (remotePath.isEmpty()) {
			remotePath = "/";
		}

		String remoteTarget = remotePath.endsWith("/") ? remotePath + backupFile.getName() : remotePath + "/" + backupFile.getName();

		runCommand(buildCmd(cmdPath, "mega-login"), email, password);
		runCommand(buildCmd(cmdPath, "mega-put"), backupFile.getAbsolutePath(), remoteTarget);
		runCommand(buildCmd(cmdPath, "mega-logout"));
	}

	public static File downloadBackup(String schema, String filename, File destination) throws IOException, InterruptedException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_MEGA_ENABLED)) {
			throw new IOException("MEGA backup is not enabled.");
		}

		String cmdPath = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_CMD_PATH);
		String email = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_EMAIL);
		String password = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_PASSWORD);
		String remotePath = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_REMOTE_PATH);

		if (cmdPath.isEmpty() || email.isEmpty() || password.isEmpty()) {
			throw new IOException("MEGA command path or credentials are missing.");
		}

		if (remotePath.isEmpty()) {
			remotePath = "/";
		}

		String remoteTarget = remotePath.endsWith("/") ? remotePath + filename : remotePath + "/" + filename;
		File parent = destination.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}

		runCommand(buildCmd(cmdPath, "mega-login"), email, password);
		runCommand(buildCmd(cmdPath, "mega-get"), remoteTarget, parent != null ? parent.getAbsolutePath() : ".");
		runCommand(buildCmd(cmdPath, "mega-logout"));

		if (!destination.exists()) {
			throw new IOException("MEGA download failed.");
		}

		return destination;
	}

	public static List<String> listBackups(String schema) throws IOException, InterruptedException {
		if (!Configurations.getBoolean(schema, Constants.CONFIG_BACKUP_MEGA_ENABLED)) {
			return new ArrayList<String>();
		}

		String cmdPath = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_CMD_PATH);
		String email = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_EMAIL);
		String password = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_PASSWORD);
		String remotePath = Configurations.getString(schema, Constants.CONFIG_BACKUP_MEGA_REMOTE_PATH);

		if (cmdPath.isEmpty() || email.isEmpty() || password.isEmpty()) {
			throw new IOException("MEGA command path or credentials are missing.");
		}

		if (remotePath.isEmpty()) {
			remotePath = "/";
		}

		runCommand(buildCmd(cmdPath, "mega-login"), email, password);
		String output = runCommandCapture(buildCmd(cmdPath, "mega-ls"), remotePath);
		runCommand(buildCmd(cmdPath, "mega-logout"));

		List<String> files = new ArrayList<String>();
		for (String line : output.split("\\r?\\n")) {
			String name = line.trim();
			if (isBackupFile(name)) {
				files.add(name);
			}
		}

		return files;
	}

	private static String buildCmd(String cmdPath, String command) {
		File base = new File(cmdPath);
		File cmd = base.isDirectory() ? new File(base, command) : new File(base.getParentFile(), command);
		return cmd.getAbsolutePath();
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
			throw new IOException("MEGA command failed: " + output);
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
			throw new IOException("MEGA command failed: " + output);
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
