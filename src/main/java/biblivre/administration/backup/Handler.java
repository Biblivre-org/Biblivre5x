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
 ******************************************************************************/
package biblivre.administration.backup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import biblivre.core.AbstractHandler;
import biblivre.core.ExtendedRequest;
import biblivre.core.ExtendedResponse;
import biblivre.core.configurations.Configurations;
import biblivre.core.enums.ActionResult;
import biblivre.core.file.DiskFile;
import biblivre.core.schemas.Schemas;
import biblivre.core.utils.Constants;
import biblivre.core.utils.Pair;

public class Handler extends AbstractHandler {

	public void prepare(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		String schemas = request.getString("schemas");
		String type = request.getString("type");

		BackupType backupType = BackupType.fromString(type);
		if (backupType == null) {
			this.setMessage(ActionResult.ERROR, "administration.maintenance.backup.error.invalid_backup_type");
			return;
		}
		
		BackupBO bo = BackupBO.getInstance(schema);
		BackupScope backupScope = bo.getBackupScope();
		
		LinkedList<String> list = new LinkedList<String>();
		list.add(Constants.GLOBAL_SCHEMA);

		if (request.isGlobalSchema()) {
			list.addAll(Arrays.asList(StringUtils.split(schemas, ",")));
			
			if (list.size() == 2) {
				// Only one schema is being backuped. We can say that the backupScope is:
				backupScope = BackupScope.SINGLE_SCHEMA_FROM_MULTI_SCHEMA;
			}
		} else {
			list.add(schema);
		}

		Map<String, Pair<String, String>> map = new HashMap<String, Pair<String, String>>();

		for (String s : list) {
			if (Schemas.isNotLoaded(s)) {
				this.setMessage(ActionResult.ERROR, "administration.maintenance.backup.error.invalid_schema");
				return;				
			}

			String title = Configurations.getString(s, Constants.CONFIG_TITLE);
			String subtitle = Configurations.getString(s, Constants.CONFIG_SUBTITLE);
			map.put(s, new Pair<String, String>(title, subtitle));
		}
		
		BackupDTO dto = bo.prepare(map, backupType, backupScope);

		try {
			this.json.put("success", true);
			this.json.put("id", dto.getId());
		} catch (JSONException e) {}
	}
	
	//http://localhost:8080/Biblivre5/?controller=json&module=administration.backup&action=backup
	public void backup(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		Integer id = request.getInteger("id");

		final BackupBO bo = BackupBO.getInstance(schema);
		final BackupDTO dto = bo.get(id);

		if (dto == null) {
			this.setMessage(ActionResult.ERROR, "error.invalid_parameters");
			return;
		}

		request.setSessionAttribute(schema, "system_warning_backup", false);
		
		new Thread(() -> {
			bo.backup(dto);
			//request.setSessionAttribute(schema, "system_warning_backup", false);
			
		}).start();
		
		try {
			this.json.put("success", true);
		} catch (JSONException e) {}
	}

	// http://localhost:8080/Biblivre5/?controller=download&module=administration.backup&action=download&id=1
	public void download(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		Integer id = request.getInteger("id");

		final BackupBO bo = BackupBO.getInstance(schema);
		final BackupDTO dto = bo.get(id);

		if (dto == null || dto.getBackup() == null || !dto.getBackup().exists()) {
			this.setReturnCode(404);
			return;
		}

		DiskFile diskFile = new DiskFile(dto.getBackup(), "application/zip");

		this.setFile(diskFile);

		this.setCallback(
			() -> {
				dto.setDownloaded(true);
				bo.save(dto);
			}
		);
	}
	
	public void progress(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		Integer id = request.getInteger("id");

		final BackupBO bo = BackupBO.getInstance(schema);
		final BackupDTO dto = bo.get(id);

		if (dto == null) {
			this.setMessage(ActionResult.ERROR, "error.invalid_parameters");
			return;
		}

		String backupError = bo.getBackupErrorStatus(id);
		if (StringUtils.isNotBlank(backupError)) {
			this.setMessage(ActionResult.ERROR, backupError);
			return;
		}

		try {
			this.json.put("success", true);
			this.json.put("current", dto.getCurrentStep());
			this.json.put("total", dto.getSteps());
			this.json.put("complete", dto.getCurrentStep() == dto.getSteps());
			this.json.put("cloud_enabled", bo.hasConfiguredCloudServices(schema));
		} catch (JSONException e) {}
	}

	public void cloudProgress(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();
		Integer id = request.getInteger("id");

		BackupBO bo = BackupBO.getInstance(schema);
		BackupBO.CloudUploadStatus status = bo.getCloudUploadStatus(id);

		try {
			this.json.put("success", true);
			if (status == null) {
				this.json.put("current", 0);
				this.json.put("total", 0);
				this.json.put("complete", true);
				this.json.put("active", false);
				this.json.put("service_label", "");
				this.json.put("error_count", 0);
			} else {
				this.json.put("current", status.getCurrent());
				this.json.put("total", status.getTotal());
				this.json.put("complete", status.isComplete());
				this.json.put("active", status.isActive());
				this.json.put("service_label", status.getServiceLabel());
				this.json.put("error_count", status.getErrorCount());
			}
		} catch (JSONException e) {}
	}

	public void cloudprogress(ExtendedRequest request, ExtendedResponse response) {
		this.cloudProgress(request, response);
	}
	
	
	// http://localhost:8080/Biblivre5/?controller=json&module=administration.backup&action=list
	public void list(ExtendedRequest request, ExtendedResponse response) {
		String schema = request.getSchema();

		BackupBO bo = BackupBO.getInstance(schema);

		try {
			this.json.put("success", true);

			for (BackupDTO dto : bo.list()) {
				this.json.append("backups", dto.toJSONObject());
			}
		} catch (JSONException e) {}
	}
}
