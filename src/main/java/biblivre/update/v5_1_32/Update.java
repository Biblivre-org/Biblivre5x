package biblivre.update.v5_1_32;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import biblivre.core.translations.Translations;
import biblivre.update.UpdateService;



public class Update implements UpdateService {

	public void doUpdate(Connection connection) throws SQLException {
		_addTranslations();
	}

	@Override
	public String getVersion() {
		return "5.1.32";
	}

	private void _addTranslations() throws SQLException {
		for (Map.Entry<String, Map<String, String>> entry: _TRANSLATIONS.entrySet()) {
			for (Map.Entry<String, String> entry2: entry.getValue().entrySet()) {
				String key = entry.getKey();
				String language = entry2.getKey();
				String translation = entry2.getValue();
				Translations.addOrReplaceSingleTranslation(language, key, translation);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private static final Map<String, Map<String, String>> _TRANSLATIONS = new HashMap() {{
		put("administration.configuration.title.general.document.format.pdf", new HashMap() {{
			put("pt-BR", "Configuração de formato de documento");
			put("es", "Configuración del formato del documento");
			put("en-US", "Document Format Settings");
		}});
		
		put("administration.configuration.description.general.document.format.pdf", new HashMap() {{
			put("pt-BR", "Escolha qual formato de documento será usado para impressão de carteirinhas e etiquetas.");
			put("es", "Elija qué formato de documento se utilizará para imprimir tarjetas de identificación y etiquetas.");
			put("en-US", "Choose which document format will be used for printing ID cards and labels.");
		}});
		
		put("cataloging.reservation.error.onhold", new HashMap() {{
			put("pt-BR", "Este exemplar já se encontra reservado.");
			put("es", "Este ejemplar ya está reservado.");
			put("en-US", "This item is already on hold.");
		}});	
		
		
	}};
		

	
}
	
	
