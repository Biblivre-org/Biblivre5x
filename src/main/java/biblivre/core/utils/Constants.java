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
package biblivre.core.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.function.Function;

import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import tech.units.indriya.unit.Units;

public class Constants {

	public static final int ADMIN_LOGGED_USER_ID = 0;
	public static final int DEFAULT_POSTGRESQL_PORT = 5432;
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final Charset WINDOWS_CHARSET = Charset.forName("cp1252");

	public static final String BIBLIVRE = "Biblivre";
	public static final String BIBLIVRE_VERSION = "5x";
	public static final String UPDATE_URL = "http://update.biblivre.org.br";
	public static final String DOWNLOAD_URL = "http://update.biblivre.org.br";

	public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final DateFormat DEFAULT_DATE_FORMAT_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public static final String LINE_BREAK = System.getProperty("line.separator");
	public static final float MM_UNIT = 72.0f / 25.4f;

	// Configurations
	public static final String CONFIG_DEFAULT_LANGUAGE = "general.default_language";
	public static final String CONFIG_MULTI_SCHEMA = "general.multi_schema";
	public static final String CONFIG_TITLE = "general.title";
	public static final String CONFIG_SUBTITLE = "general.subtitle";
	public static final String CONFIG_UID = "general.uid";
	public static final String CONFIG_BUSINESS_DAYS = "general.business_days";
	public static final String CONFIG_CURRENCY = "general.currency";

	public static final String CONFIG_NEW_LIBRARY = "setup.new_library";

	public static final String CONFIG_ACCESSION_NUMBER_PREFIX = "cataloging.accession_number_prefix";

	public static final String CONFIG_PGDUMP_PATH = "general.pg_dump_path";
	public static final String CONFIG_PSQL_PATH = "general.psql_path";
	public static final String CONFIG_BACKUP_PATH = "general.backup_path";
	public static final String CONFIG_DOCUMENT_FORMAT_PDF = "general.document.format.pdf";
	
	// Cloud and Email Backup Configurations
	public static final String CONFIG_BACKUP_EMAIL_ENABLED = "administration.backup.email.enabled";
	public static final String CONFIG_BACKUP_EMAIL_HOST = "administration.backup.email.host";
	public static final String CONFIG_BACKUP_EMAIL_PORT = "administration.backup.email.port";
	public static final String CONFIG_BACKUP_EMAIL_USER = "administration.backup.email.user";
	public static final String CONFIG_BACKUP_EMAIL_PASSWORD = "administration.backup.email.password";
	public static final String CONFIG_BACKUP_EMAIL_FROM = "administration.backup.email.from";
	public static final String CONFIG_BACKUP_EMAIL_TO = "administration.backup.email.to";
	public static final String CONFIG_BACKUP_EMAIL_SSL = "administration.backup.email.ssl";

	public static final String CONFIG_BACKUP_GOOGLE_DRIVE_ENABLED = "administration.backup.google_drive.enabled";
	public static final String CONFIG_BACKUP_GOOGLE_DRIVE_CLIENT_ID = "administration.backup.google_drive.client_id";
	public static final String CONFIG_BACKUP_GOOGLE_DRIVE_CLIENT_SECRET = "administration.backup.google_drive.client_secret";
	public static final String CONFIG_BACKUP_GOOGLE_DRIVE_REFRESH_TOKEN = "administration.backup.google_drive.refresh_token";
	public static final String CONFIG_BACKUP_GOOGLE_DRIVE_ACCOUNT_EMAIL = "administration.backup.google_drive.account_email";

	public static final String CONFIG_BACKUP_ONEDRIVE_ENABLED = "administration.backup.onedrive.enabled";
	public static final String CONFIG_BACKUP_ONEDRIVE_CLIENT_ID = "administration.backup.onedrive.client_id";
	public static final String CONFIG_BACKUP_ONEDRIVE_CLIENT_SECRET = "administration.backup.onedrive.client_secret";
	public static final String CONFIG_BACKUP_ONEDRIVE_REFRESH_TOKEN = "administration.backup.onedrive.refresh_token";

	public static final String CONFIG_BACKUP_DROPBOX_ENABLED = "administration.backup.dropbox.enabled";
	public static final String CONFIG_BACKUP_DROPBOX_APP_KEY = "administration.backup.dropbox.app_key";
	public static final String CONFIG_BACKUP_DROPBOX_APP_SECRET = "administration.backup.dropbox.app_secret";
	public static final String CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN = "administration.backup.dropbox.access_token";
	public static final String CONFIG_BACKUP_DROPBOX_REFRESH_TOKEN = "administration.backup.dropbox.refresh_token";
	public static final String CONFIG_BACKUP_DROPBOX_ACCESS_TOKEN_EXPIRES_AT = "administration.backup.dropbox.access_token_expires_at";
	public static final String CONFIG_BACKUP_DROPBOX_ACCOUNT_EMAIL = "administration.backup.dropbox.account_email";
	public static final String CONFIG_BACKUP_BOX_ENABLED = "administration.backup.box.enabled";
	public static final String CONFIG_BACKUP_BOX_CLIENT_ID = "administration.backup.box.client_id";
	public static final String CONFIG_BACKUP_BOX_CLIENT_SECRET = "administration.backup.box.client_secret";
	public static final String CONFIG_BACKUP_BOX_ACCESS_TOKEN = "administration.backup.box.access_token";
	public static final String CONFIG_BACKUP_BOX_REFRESH_TOKEN = "administration.backup.box.refresh_token";
	public static final String CONFIG_BACKUP_BOX_FOLDER_ID = "administration.backup.box.folder_id";
	public static final String CONFIG_BACKUP_PCLOUD_ENABLED = "administration.backup.pcloud.enabled";
	public static final String CONFIG_BACKUP_PCLOUD_AUTH_TOKEN = "administration.backup.pcloud.auth_token";
	public static final String CONFIG_BACKUP_PCLOUD_PATH = "administration.backup.pcloud.path";
	public static final String CONFIG_BACKUP_MEGA_ENABLED = "administration.backup.mega.enabled";
	public static final String CONFIG_BACKUP_MEGA_CMD_PATH = "administration.backup.mega.cmd_path";
	public static final String CONFIG_BACKUP_MEGA_EMAIL = "administration.backup.mega.email";
	public static final String CONFIG_BACKUP_MEGA_PASSWORD = "administration.backup.mega.password";
	public static final String CONFIG_BACKUP_MEGA_REMOTE_PATH = "administration.backup.mega.remote_path";
	public static final String CONFIG_BACKUP_PROTON_ENABLED = "administration.backup.proton.enabled";
	public static final String CONFIG_BACKUP_PROTON_RCLONE_PATH = "administration.backup.proton.rclone_path";
	public static final String CONFIG_BACKUP_PROTON_RCLONE_REMOTE = "administration.backup.proton.rclone_remote";
	// /
	
	public static final String CONFIG_SEARCH_RESULTS_PER_PAGE = "search.results_per_page";
	public static final String CONFIG_SEARCH_RESULT_LIMIT = "search.result_limit";

	public static final String CONFIG_Z3950_RESULT_LIMIT = "search.distributed_search_limit";
	public static final String CONFIG_Z3950_SERVER_ACTIVE = "administration.z3950.server.active";

	public static final String CONFIG_LENDING_PRINTER_TYPE = "circulation.lending_receipt.printer.type";

	// Translations
	public static final String TRANSLATION_RECORD_TAB_FIELD_LABEL = "cataloging.tab.record.custom.field_label.";
	public static final String TRANSLATION_INDEXING_GROUP = "cataloging.custom.indexing_group.";
	public static final String TRANSLATION_USER_FIELD = "circulation.custom.user_field.";
	public static final String TRANSLATION_FORMAT_DATE = "format.date";
	public static final String TRANSLATION_FORMAT_DATETIME = "format.datetime";

	// Media server
	public static final int DEFAULT_BUFFER_SIZE = 10240; // 10 KB
	public static final long DEFAULT_EXPIRE_TIME = 9676800000L; // 16 weeks
	public static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

	public static final String GLOBAL_SCHEMA = "global";

	// The constants below should not be final
	public static String SINGLE_SCHEMA = "single";
	public static boolean REINDEXING = false;

	public static final Unit<Length> USER_UNIT = MetricPrefix.CENTI(Units.METRE).multiply(2.54).divide(72);
	public static final Function<Quantity<Length>, Float> FROM_CM = q -> q.to(USER_UNIT).getValue().floatValue();
}
