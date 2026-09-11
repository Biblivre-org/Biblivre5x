/*******************************************************************************
 * Este arquivo é parte do Biblivre5.
 * 
 * Biblivre5 é um software livre; você pode redistribuí-lo e/ou 
 * modificá-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (caso queira) qualquer versão posterior.
 * 
 * Este programa é distribuído na esperança de que possa ser  útil, 
 * mas SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * MERCANTIBILIDADE OU ADEQUAÇÃO PARA UM FIM PARTICULAR. Veja a
 * Licença Pública Geral GNU para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU junto
 * com este programa, Se não, veja em <http://www.gnu.org/licenses/>.
 * 
 * @author Alberto Wagner <alberto@biblivre.org.br>
 * @author Danniel Willian <danniel@biblivre.org.br>
 * * Updated by Wilerson Lucas <xmidia@gmail.com>
 ******************************************************************************/
package biblivre.administration.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;

import biblivre.administration.backup.services.CloudBackupService;
import biblivre.administration.backup.services.CloudBackupServiceRegistry;
import biblivre.core.AbstractBO;
import biblivre.core.configurations.Configurations;
import biblivre.core.file.DatabaseFile;
import biblivre.core.schemas.Schemas;
import biblivre.core.utils.Constants;
import biblivre.core.utils.DatabaseUtils;
import biblivre.core.utils.FileIOUtils;
import biblivre.core.utils.Pair;
import biblivre.core.utils.PgDumpCommand;
import biblivre.core.utils.PgDumpCommand.Format;
import biblivre.digitalmedia.DigitalMediaDAO;
import biblivre.digitalmedia.DigitalMediaDTO;
import br.org.biblivre.z3950server.utils.TextUtils;

public class BackupBO extends AbstractBO {
	private BackupDAO dao;
	private static final Map<Integer, CloudUploadStatus> CLOUD_UPLOAD_STATUS = new ConcurrentHashMap<Integer, CloudUploadStatus>();
	private static final Map<Integer, String> BACKUP_ERROR_STATUS = new ConcurrentHashMap<Integer, String>();

	public static BackupBO getInstance(String schema) {
		BackupBO bo = AbstractBO.getInstance(BackupBO.class, schema);

		if (bo.dao == null) {
			bo.dao = BackupDAO.getInstance(schema);
		}
		
		return bo;
	}
	
	public void simpleBackup() {
		BackupType backupType = BackupType.FULL;
		BackupScope backupScope = this.getBackupScope();

		LinkedList<String> list = new LinkedList<String>();
		list.add(Constants.GLOBAL_SCHEMA);

		if (this.isGlobalSchema()) {
			list.addAll(Schemas.getEnabledSchemasList());
		} else {
			list.add(this.getSchema());
		}

		Map<String, Pair<String, String>> map = new HashMap<String, Pair<String, String>>();

		for (String s : list) {
			if (Schemas.isNotLoaded(s)) {
				continue;				
			}

			String title = Configurations.getString(s, Constants.CONFIG_TITLE);
			String subtitle = Configurations.getString(s, Constants.CONFIG_SUBTITLE);
			map.put(s, new Pair<String, String>(title, subtitle));
		}

		BackupDTO dto = this.prepare(map, backupType, backupScope);
		this.backup(dto);
	}

	public BackupScope getBackupScope() {
		if (this.isGlobalSchema()) {
			return BackupScope.MULTI_SCHEMA;
		} else if (Schemas.isMultipleSchemasEnabled()) {
			return BackupScope.SINGLE_SCHEMA_FROM_MULTI_SCHEMA;
		} else {
			return BackupScope.SINGLE_SCHEMA;
		}
	}
	
	public BackupDTO prepare(Map<String, Pair<String, String>> schemas, BackupType type, BackupScope scope) {
		BackupDTO dto = new BackupDTO(schemas, type, scope);
		dto.setCurrentStep(0);
		
		int steps = 0;
		int schemasCount = dto.getSchemas().size();
		
		switch (dto.getType()) {
			case FULL:
				// schema, data e media + zip + validacao/finalizacao
				steps = (schemasCount * 3) + 1;
				break;
			case EXCLUDE_DIGITAL_MEDIA:
				// schema e data + zip + validacao/finalizacao
				steps = (schemasCount * 2) + 2;
				break;
			case DIGITAL_MEDIA_ONLY:
				// media + zip + validacao/finalizacao
				steps = schemasCount + 1;
				break;
		}
				
		dto.setSteps(steps);

		if (this.save(dto)) {
			return dto;
		} else {
			return null;
		}
	}

	public void backup(BackupDTO dto) {
		try {
			this.clearBackupErrorStatus(dto != null ? dto.getId() : null);
			this.initCloudUploadStatus(dto);
			this.createBackup(dto);
	
			if (dto.getBackup() != null) {
				BackupTester tester = BackupTester.getInstance(this.getSchema());
				boolean testPassed = tester.testBackup(dto.getBackup());
				
				if (!testPassed) {
					throw new Exception("administration.maintenance.backup.error.test_failed");
				}
				
				if (!this.move(dto)) {
					throw new Exception("administration.maintenance.backup.error.couldnt_move_backup");
				}

				dto.increaseCurrentStep();
				this.save(dto);
				this.uploadToCloudServices(dto, dto.getBackup());
			}
		} catch (Exception e) {
			if (dto != null && dto.getId() != null) {
				this.setBackupErrorStatus(dto.getId(), this.resolveBackupErrorMessage(e));
			}

			if (dto != null && dto.getBackup() != null && dto.getBackup().exists()) {
				FileUtils.deleteQuietly(dto.getBackup());
				dto.setBackup(null);
				this.save(dto);
			}

			this.logger.error("Error creating or uploading backup: " + e.getMessage(), e);
			this.clearCloudUploadStatus(dto != null ? dto.getId() : null);
		}
	}

	public CloudUploadStatus getCloudUploadStatus(Integer id) {
		return CLOUD_UPLOAD_STATUS.get(id);
	}

	public String getBackupErrorStatus(Integer id) {
		return BACKUP_ERROR_STATUS.get(id);
	}

	private void setBackupErrorStatus(Integer id, String messageKey) {
		if (id == null) {
			return;
		}

		BACKUP_ERROR_STATUS.put(id, messageKey);
	}

	private void clearBackupErrorStatus(Integer id) {
		if (id == null) {
			return;
		}

		BACKUP_ERROR_STATUS.remove(id);
	}

	private String resolveBackupErrorMessage(Exception e) {
		String message = e != null ? e.getMessage() : null;
		if (StringUtils.isNotBlank(message) && message.startsWith("administration.")) {
			return message;
		}

		return "administration.maintenance.backup.error.test_failed";
	}

	private void clearCloudUploadStatus(Integer id) {
		if (id != null) {
			CLOUD_UPLOAD_STATUS.remove(id);
		}
	}

	private void initCloudUploadStatus(BackupDTO dto) {
		if (dto == null || dto.getId() == null) {
			return;
		}

		int total = this.countConfiguredCloudServices(this.getSchema());
		if (total == 0) {
			this.clearCloudUploadStatus(dto.getId());
			return;
		}

		CLOUD_UPLOAD_STATUS.put(dto.getId(), new CloudUploadStatus(total));
	}

	private int countConfiguredCloudServices(String schema) {
		return CloudBackupServiceRegistry.getConfiguredUploadServices(schema).size();
	}

	public boolean hasConfiguredCloudServices(String schema) {
		return this.countConfiguredCloudServices(schema) > 0;
	}

	public void uploadToCloudServices(BackupDTO dto, File backupFile) {
		System.out.println("\n=== INICIANDO UPLOAD TO CLOUD SERVICES ===");
		String schema = this.getSchema();
		Integer id = dto != null ? dto.getId() : null;
		System.out.println("Schema: " + schema);
		System.out.println("Backup file: " + (backupFile != null ? backupFile.getAbsolutePath() : "null"));

		List<CloudService> services = this.buildCloudServices(schema, backupFile);
		System.out.println("Número de serviços configurados: " + services.size());
		if (services.isEmpty()) {
			System.out.println("Nenhum serviço de cloud configurado!");
			this.clearCloudUploadStatus(id);
			return;
		}

		CloudUploadStatus status = null;
		if (id != null) {
			status = CLOUD_UPLOAD_STATUS.get(id);
			if (status == null || status.getTotal() != services.size()) {
				status = new CloudUploadStatus(services.size());
				CLOUD_UPLOAD_STATUS.put(id, status);
			}
		} else {
			status = new CloudUploadStatus(services.size());
		}
		
		status.setActive(true);
		status.setComplete(false);

		for (CloudService service : services) {
			System.out.println("\nProcessando serviço: " + service.getLabel());
			status.setServiceLabel(service.getLabel());
			try {
				service.getUploader().upload();
				System.out.println("Backup enviado pelo " + service.getLabel() + " com sucesso: " + backupFile.getName());
			} catch (Exception e) {
				System.out.println("ERRO no serviço " + service.getLabel() + ": " + e.getMessage());
				e.printStackTrace();
				this.logger.error("Error uploading backup to " + service.getLabel() + ": " + e.getMessage(), e);
				status.incrementError();
			} finally {
				status.incrementCurrent();
			}
		}

		status.setActive(false);
		status.setComplete(true);
		status.setServiceLabel(null);
		System.out.println("\n=== UPLOAD TO CLOUD SERVICES CONCLUÍDO! ===");
	}

	private List<CloudService> buildCloudServices(String schema, File backupFile) {
		System.out.println("buildCloudServices chamado para schema: " + schema);
		List<CloudService> services = new ArrayList<CloudService>();

		if (backupFile == null) {
			System.out.println("backupFile é null!");
			return services;
		}

		List<CloudBackupService> configuredServices = CloudBackupServiceRegistry.getConfiguredUploadServices(schema);
		System.out.println("Serviços configurados retornados: " + configuredServices.size());
		for (CloudBackupService service : configuredServices) {
			System.out.println("Adicionando serviço: " + service.getLabel());
			services.add(new CloudService(service.getLabel(), () -> service.uploadBackup(schema, backupFile)));
		}

		return services;
	}

	private interface CloudUploader {
		void upload() throws Exception;
	}

	private static class CloudService {
		private final String label;
		private final CloudUploader uploader;

		private CloudService(String label, CloudUploader uploader) {
			this.label = label;
			this.uploader = uploader;
		}

		public String getLabel() {
			return label;
		}

		public CloudUploader getUploader() {
			return uploader;
		}
	}

	public static class CloudUploadStatus {
		private final int total;
		private int current;
		private int errorCount;
		private boolean complete;
		private boolean active;
		private String serviceLabel;

		private CloudUploadStatus(int total) {
			this.total = total;
			this.current = 0;
			this.errorCount = 0;
			this.complete = false;
			this.active = false;
		}

		public int getTotal() {
			return total;
		}

		public int getCurrent() {
			return current;
		}

		public int getErrorCount() {
			return errorCount;
		}

		public boolean isComplete() {
			return complete;
		}

		public boolean isActive() {
			return active;
		}

		public String getServiceLabel() {
			return serviceLabel;
		}

		private void incrementCurrent() {
			this.current++;
		}
		
		private void incrementError() {
			this.errorCount++;
		}

		private void setComplete(boolean complete) {
			this.complete = complete;
		}

		private void setActive(boolean active) {
			this.active = active;
		}

		private void setServiceLabel(String serviceLabel) {
			this.serviceLabel = serviceLabel;
		}
	}

	public void createBackup(BackupDTO dto) throws IOException {
		File pgdump = DatabaseUtils.getPgDump(this.getSchema());

		if (pgdump == null) {
			return;
		}
		
		File tmpDir = FileIOUtils.createTempDir();

		Map<String, Pair<String, String>> schemas = dto.getSchemas();
		BackupType type = dto.getType();

		// Writing metadata
		File meta = new File(tmpDir, "backup.meta");
		Writer writer = new FileWriterWithEncoding(meta, "UTF-8");
		writer.write(new RestoreDTO(dto).toJSONString());
		writer.flush();
		writer.close();

		for (String schema : schemas.keySet()) {
			if (type == BackupType.FULL || type == BackupType.EXCLUDE_DIGITAL_MEDIA) {
				dumpSchema(dto, tmpDir, schema);

				dumpData(dto, tmpDir, schema);
			}

			if (!schema.equals(Constants.GLOBAL_SCHEMA)) {	
				if (type == BackupType.FULL || type == BackupType.DIGITAL_MEDIA_ONLY) {
					dumpMedia(dto, tmpDir, schema);
				}
			}
		}

		File tmpZip = new File(tmpDir.getAbsolutePath() + ".b5bz");

		FileIOUtils.zipFolder(tmpDir, tmpZip);
		FileUtils.deleteQuietly(tmpDir);

		dto.increaseCurrentStep();
		this.save(dto);

		dto.setBackup(tmpZip);
	}

	public BackupDTO get(Integer id) {
		return this.dao.get(id);
	}

	public LinkedList<BackupDTO> list() {
		return this.dao.list();
	}

	public BackupDTO getLastBackup() {
		LinkedList<BackupDTO> list = this.dao.list(1);

		if (list.size() == 0) {
			return null;
		}

		return list.getFirst();
	}
	
	public boolean save(BackupDTO dto) {
		return this.dao.save(dto);
	}

	public boolean move(BackupDTO dto) {
		File destination = this.getBackupDestination();
		File backup = dto.getBackup();
		
		if (destination == null) {
			destination = backup.getParentFile();
		}

		StringBuilder sb = new StringBuilder();

		//Format: Biblivre Backup 2012-09-08 12h01m22s Full.b5bz
		Formatter formatter = new Formatter(sb);
		formatter.format("Biblivre Backup %1$tY-%1$tm-%1$td %1$tHh%1$tMm%1$tSs %2$s.b5bz", new Date(), StringUtils.capitalize(dto.getType().toString()));
		formatter.close();

		File movedBackup = new File(destination, sb.toString());

		boolean success = backup.renameTo(movedBackup);

		if (success) {
			dto.setBackup(movedBackup);
		}

		return this.save(dto);
	}

	public String getBackupPath() {
		String path = Configurations.getString(this.getSchema(), Constants.CONFIG_BACKUP_PATH);

		if (StringUtils.isBlank(path) || FileIOUtils.doesNotExists(path)) {
			File home = new File(System.getProperty("user.home"));
			File biblivre = new File(home, "Biblivre");

			if (!biblivre.exists() && home.isDirectory() && home.canWrite()) {
				biblivre.mkdir();
			}

			path = biblivre.getAbsolutePath();
		}

		return path;
	}

	public File getBackupDestination() {
		String path = this.getBackupPath();

		return FileIOUtils.getWritablePath(path);
	}

	private boolean exportDigitalMedia(String schema, File path) {
		OutputStream writer = null;
		DigitalMediaDAO dao = DigitalMediaDAO.getInstance(schema);
		List<DigitalMediaDTO> list = dao.list();

		try {
			for (DigitalMediaDTO dto : list) {
				DatabaseFile file = dao.load(dto.getId(), dto.getName());
				File destination = new File(path, dto.getId() + "_" + TextUtils.removeNonLettersOrDigits(dto.getName(), "-"));
				writer = new FileOutputStream(destination);

				file.copy(writer);

				file.close();
			}

			return true;
		} catch(Exception e) {
			this.logger.error("Error writing backup metadata: " + e.getMessage(), e);
			return false;
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private boolean dumpDatabase(ProcessBuilder pb) {
		pb.environment().put("PGDATABASE", "biblivre4");
		pb.environment().put("PGUSER", "biblivre");
		pb.environment().put("PGPASSWORD", "abracadabra");

		pb.redirectErrorStream(true);

		BufferedReader br = null;

		try {
			Process p = pb.start();

			InputStreamReader isr = new InputStreamReader(p.getInputStream());
			br = new BufferedReader(isr);
			String line;
			
			while ((line = br.readLine()) != null) {
				//There was a system.out.println here for the 'line' var, 
				//with a FIX_ME tag.  So I changed it to logger.debug().
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(line);
				}
			}

			p.waitFor();
			
			return p.exitValue() == 0;
		} catch (IOException e) {
			this.logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			this.logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
		}

		return false;
	}

	private void dumpDatabase(PgDumpCommand command) {
		this.dumpDatabase(new ProcessBuilder(command.getCommands()));
	}

	private void dump(BackupDTO dto, String schema, File backupFile,
			boolean isSchemaOnly, boolean isDataOnly,
			String excludeTablePattern, String includeTablePattern) {

		File pgdump = DatabaseUtils.getPgDump(this.getSchema());;
		InetSocketAddress defaultAddress = new InetSocketAddress(
				InetAddress.getLoopbackAddress(),
				Constants.DEFAULT_POSTGRESQL_PORT);

		Format defaultFormat = Format.PLAIN;

		this.dumpDatabase(new PgDumpCommand(pgdump, defaultAddress,
				Constants.DEFAULT_CHARSET, defaultFormat, schema, backupFile,
				isSchemaOnly , isDataOnly , excludeTablePattern,
				includeTablePattern ));

		dto.increaseCurrentStep();
		this.save(dto);
	}

	private void dumpData(BackupDTO dto, File tmpDir, String schema) {
		File dataBackup = new File(tmpDir, schema + ".data.b5b");
		boolean isSchemaOnly = false;
		boolean isDataOnly = true;
		String excludeTablePattern = schema + ".digital_media";
		String includeTablePattern = null;

		dump(dto, schema, dataBackup, isSchemaOnly, isDataOnly,
				excludeTablePattern,	includeTablePattern);
	}

	private void dumpSchema(BackupDTO dto, File tmpDir, String schema) {
		File schemaBackup = new File(tmpDir, schema + ".schema.b5b");
		boolean isSchemaOnly = true;
		boolean isDataOnly = false;
		String excludeTablePattern = null;
		String includeTablePattern = null;

		dump(dto, schema, schemaBackup, isSchemaOnly, isDataOnly,
				excludeTablePattern, includeTablePattern);
	}

	private void dumpMedia(BackupDTO dto, File tmpDir, String schema)
			throws IOException {
		File mediaBackup = new File(tmpDir, schema + ".media.b5b");
		boolean isSchemaOnly = false;
		boolean isDataOnly = true;
		String excludeTablePattern = null;
		String includeTablePattern = schema + ".digital_media";

		dump(dto, schema, mediaBackup, isSchemaOnly, isDataOnly,
				excludeTablePattern, includeTablePattern);

		File schemaBackup = new File(tmpDir, schema);
		schemaBackup.mkdir();
		this.exportDigitalMedia(schema, schemaBackup);
		this.save(dto);
	}
}
