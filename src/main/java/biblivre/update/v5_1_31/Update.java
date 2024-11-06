package biblivre.update.v5_1_31;

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
		return "5.1.31";
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
		put("administration.setup.restore.form", new HashMap() {{
			put("pt-BR", "Restaurar Formulário Catalográfico");
			put("es", "Restaurar Formulario de Catalogación");
			put("en-US", "Restore Cataloging Form");
		}});
		
		put("administration.setup.restore.form.description", new HashMap() {{
			put("pt-BR", "O formulário catalográfico será restaurado ao seu estado original, portanto, caso haja customização do formulário, ela será substituída.");
			put("es", "El formulario del catálogo será restaurado a su estado original, por lo que si hay alguna personalización del formulario, será reemplazado.");
			put("en-US", "The cataloging form will be restored to its original state, so if there is any customization of the form, it will be replaced.");
		}});
		
		put("administration.setup.restore.form.button", new HashMap() {{
			put("pt-BR", "Restaurar todos os campos de formulário");
			put("es", "Restaurar todos los campos del formulario");
			put("en-US", "Restore all form fields");
		}});
		
		put("administration.setup.restore.form.confirm", new HashMap() {{
			put("pt-BR", "Deseja restaurar formulário catalográfico ?");
			put("es", "¿Quieres restaurar el formulario del catálogo?");
			put("en-US", "Do you want to restore catalog form ?");
		}});
		
		put("menu.help_manual4", new HashMap() {{
			put("pt-BR", "Manual do Biblivre 4");
			put("es", "Manual del Biblivre 4");
			put("en-US", "Biblivre Manual 4");
		}});
		
		put("menu.help_manual5", new HashMap() {{
			put("pt-BR", "Manual do Biblivre 5");
			put("es", "Manual del Biblivre 5");
			put("en-US", "Biblivre Manual 5");
		}});		
		
		put("multi_schema.manage.new_schema.button.back", new HashMap() {{
			put("pt-BR", "Voltar");
			put("es", "Para volver");
			put("en-US", "To go back");
		}});
		
		
		
	}};
	
	
}
