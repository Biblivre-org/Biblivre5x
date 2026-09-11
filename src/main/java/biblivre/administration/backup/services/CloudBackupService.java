package biblivre.administration.backup.services;

import java.io.File;
import java.util.List;

public interface CloudBackupService {
	String getId();

	String getLabel();

	boolean isConfigured(String schema);

	boolean supportsRemoteFiles();

	void uploadBackup(String schema, File backupFile) throws Exception;

	File downloadBackup(String schema, String filename, File destination) throws Exception;

	List<String> listBackups(String schema) throws Exception;
}
