package biblivre.administration.backup.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import biblivre.core.configurations.Configurations;
import biblivre.core.utils.Constants;

public final class CloudBackupServiceRegistry {
	private static final List<CloudBackupService> SERVICES = Collections.unmodifiableList(Arrays.<CloudBackupService>asList(
			new AbstractCloudBackupService("email", "E-mail", false, Constants.CONFIG_BACKUP_EMAIL_ENABLED,
					Constants.CONFIG_BACKUP_EMAIL_HOST,
					Constants.CONFIG_BACKUP_EMAIL_PORT,
					Constants.CONFIG_BACKUP_EMAIL_USER,
					Constants.CONFIG_BACKUP_EMAIL_PASSWORD,
					Constants.CONFIG_BACKUP_EMAIL_FROM,
					Constants.CONFIG_BACKUP_EMAIL_TO) {
				@Override
				public void uploadBackup(String schema, File backupFile) throws Exception {
					EmailService.sendBackup(schema, backupFile);
				}
			},
			new AbstractCloudBackupService("google_drive", "Google Drive", true, Constants.CONFIG_BACKUP_GOOGLE_DRIVE_ENABLED,
					Constants.CONFIG_BACKUP_GOOGLE_DRIVE_REFRESH_TOKEN) {
				@Override
				public boolean isConfigured(String schema) {
					return super.isConfigured(schema) && GoogleDriveService.hasOAuthClientCredentials(schema);
				}

				@Override
				public void uploadBackup(String schema, File backupFile) throws Exception {
					GoogleDriveService.uploadBackup(schema, backupFile);
				}

				@Override
				public File downloadBackup(String schema, String filename, File destination) throws Exception {
					return GoogleDriveService.downloadBackup(schema, filename, destination);
				}

				@Override
				public List<String> listBackups(String schema) throws Exception {
					return GoogleDriveService.listBackups(schema);
				}
			},
			new AbstractCloudBackupService("onedrive", "OneDrive", true, Constants.CONFIG_BACKUP_ONEDRIVE_ENABLED,
					Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_ID,
					Constants.CONFIG_BACKUP_ONEDRIVE_CLIENT_SECRET,
					Constants.CONFIG_BACKUP_ONEDRIVE_REFRESH_TOKEN) {
				@Override
				public void uploadBackup(String schema, File backupFile) throws Exception {
					OneDriveService.uploadBackup(schema, backupFile);
				}

				@Override
				public File downloadBackup(String schema, String filename, File destination) throws Exception {
					return OneDriveService.downloadBackup(schema, filename, destination);
				}

				@Override
				public List<String> listBackups(String schema) throws Exception {
					return OneDriveService.listBackups(schema);
				}
			},
			new AbstractCloudBackupService("dropbox", "Dropbox", true, Constants.CONFIG_BACKUP_DROPBOX_ENABLED) {
				@Override
				public boolean isConfigured(String schema) {
					return DropboxService.isConfigured(schema);
				}

				@Override
				public void uploadBackup(String schema, File backupFile) throws Exception {
					DropboxService.uploadBackup(schema, backupFile);
				}

				@Override
				public File downloadBackup(String schema, String filename, File destination) throws Exception {
					return DropboxService.downloadBackup(schema, filename, destination);
				}

				@Override
				public List<String> listBackups(String schema) throws Exception {
					return DropboxService.listBackups(schema);
				}
			},
			new AbstractCloudBackupService("box", "Box", true, Constants.CONFIG_BACKUP_BOX_ENABLED) {
				@Override
				public boolean isConfigured(String schema) {
					return BoxService.isConfigured(schema);
				}

				@Override
				public void uploadBackup(String schema, File backupFile) throws Exception {
					BoxService.uploadBackup(schema, backupFile);
				}

				@Override
				public File downloadBackup(String schema, String filename, File destination) throws Exception {
					return BoxService.downloadBackup(schema, filename, destination);
				}

				@Override
				public List<String> listBackups(String schema) throws Exception {
					return BoxService.listBackups(schema);
				}
			},
			new AbstractCloudBackupService("pcloud", "pCloud", true, Constants.CONFIG_BACKUP_PCLOUD_ENABLED,
					Constants.CONFIG_BACKUP_PCLOUD_AUTH_TOKEN) {
				@Override
				public void uploadBackup(String schema, File backupFile) throws Exception {
					PCloudService.uploadBackup(schema, backupFile);
				}

				@Override
				public File downloadBackup(String schema, String filename, File destination) throws Exception {
					return PCloudService.downloadBackup(schema, filename, destination);
				}

				@Override
				public List<String> listBackups(String schema) throws Exception {
					return PCloudService.listBackups(schema);
				}
			},
			new AbstractCloudBackupService("mega", "MEGA", true, Constants.CONFIG_BACKUP_MEGA_ENABLED,
					Constants.CONFIG_BACKUP_MEGA_CMD_PATH,
					Constants.CONFIG_BACKUP_MEGA_EMAIL,
					Constants.CONFIG_BACKUP_MEGA_PASSWORD) {
				@Override
				public void uploadBackup(String schema, File backupFile) throws Exception {
					MegaService.uploadBackup(schema, backupFile);
				}

				@Override
				public File downloadBackup(String schema, String filename, File destination) throws Exception {
					return MegaService.downloadBackup(schema, filename, destination);
				}

				@Override
				public List<String> listBackups(String schema) throws Exception {
					return MegaService.listBackups(schema);
				}
			},
			new AbstractCloudBackupService("proton", "Proton Drive", true, Constants.CONFIG_BACKUP_PROTON_ENABLED,
					Constants.CONFIG_BACKUP_PROTON_RCLONE_PATH,
					Constants.CONFIG_BACKUP_PROTON_RCLONE_REMOTE) {
				@Override
				public void uploadBackup(String schema, File backupFile) throws Exception {
					ProtonDriveService.uploadBackup(schema, backupFile);
				}

				@Override
				public File downloadBackup(String schema, String filename, File destination) throws Exception {
					return ProtonDriveService.downloadBackup(schema, filename, destination);
				}

				@Override
				public List<String> listBackups(String schema) throws Exception {
					return ProtonDriveService.listBackups(schema);
				}
			}
	));

	private CloudBackupServiceRegistry() {
	}

	public static List<CloudBackupService> getServices() {
		return SERVICES;
	}

	public static List<CloudBackupService> getConfiguredUploadServices(String schema) {
		//System.out.println("\ngetConfiguredUploadServices chamado para schema: " + schema);
		List<CloudBackupService> configured = new ArrayList<CloudBackupService>();
		for (CloudBackupService service : SERVICES) {
			if (service.isConfigured(schema)) {
				System.out.println("  Serviço adicionado: " + service.getLabel());
				configured.add(service);
			}
		}
		System.out.println("Total de serviços configurados: " + configured.size());
		return configured;
	}

	public static List<CloudBackupService> getConfiguredRemoteFileServices(String schema) {
		List<CloudBackupService> configured = new ArrayList<CloudBackupService>();
		for (CloudBackupService service : SERVICES) {
			if (service.supportsRemoteFiles() && service.isConfigured(schema)) {
				configured.add(service);
			}
		}
		return configured;
	}

	public static CloudBackupService getService(String serviceId) {
		String normalized = normalizeServiceId(serviceId);
		for (CloudBackupService service : SERVICES) {
			if (service.getId().equals(normalized)) {
				return service;
			}
		}
		return null;
	}

	public static String normalizeServiceId(String serviceId) {
		String normalized = StringUtils.trimToEmpty(serviceId).toLowerCase();

		if ("googledrive".equals(normalized) || "google".equals(normalized) || "gdrive".equals(normalized)) {
			return "google_drive";
		}
		if ("one_drive".equals(normalized) || "msdrive".equals(normalized) || "ms-onedrive".equals(normalized)) {
			return "onedrive";
		}
		if ("pc".equals(normalized) || "p-cloud".equals(normalized) || "p_cloud".equals(normalized)) {
			return "pcloud";
		}
		if ("protondrive".equals(normalized) || "proton_drive".equals(normalized)) {
			return "proton";
		}

		return normalized;
	}

	private abstract static class AbstractCloudBackupService implements CloudBackupService {
		private final String id;
		private final String label;
		private final boolean supportsRemoteFiles;
		private final String enabledKey;
		private final String[] requiredKeys;

		private AbstractCloudBackupService(String id, String label, boolean supportsRemoteFiles, String enabledKey, String... requiredKeys) {
			this.id = id;
			this.label = label;
			this.supportsRemoteFiles = supportsRemoteFiles;
			this.enabledKey = enabledKey;
			this.requiredKeys = requiredKeys;
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public String getLabel() {
			return this.label;
		}

		@Override
		public boolean supportsRemoteFiles() {
			return this.supportsRemoteFiles;
		}

		@Override
		public boolean isConfigured(String schema) {
			System.out.println("=== Verificando configuração do serviço: " + this.label + " (schema: " + schema + ")");
			//System.out.println("  enabledKey: " + this.enabledKey);
			
			boolean enabled = Configurations.getBoolean(schema, this.enabledKey);
			
			if (!enabled) {
				System.out.println("  Serviço DESABILITADO!");
				return false;
			}else
				System.out.println("  Habilitado: " + enabled);

			//System.out.println("  Verificando requiredKeys:");
			
			for (String key : this.requiredKeys) {
				String value = Configurations.getString(schema, key);
				boolean blank = StringUtils.isBlank(value);
				//System.out.println("    " + key + " = \"" + value + "\" (vazio? " + blank + ")");
				if (blank) {
					System.out.println("  Campo obrigatório VAZIO: " + key);
					return false;
				}
			}

			System.out.println("  Serviço CONFIGURADO com sucesso!");
			return true;
		}

		@Override
		public File downloadBackup(String schema, String filename, File destination) throws Exception {
			throw new UnsupportedOperationException(this.label + " does not support remote backup downloads.");
		}

		@Override
		public List<String> listBackups(String schema) throws Exception {
			return new ArrayList<String>();
		}
	}
}
