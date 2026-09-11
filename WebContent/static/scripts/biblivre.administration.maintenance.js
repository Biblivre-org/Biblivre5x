/**
 *  Este arquivo é parte do Biblivre5.
 *  
 *  Biblivre5 é um software livre; você pode redistribuí-lo e/ou 
 *  modificá-lo dentro dos termos da Licença Pública Geral GNU como 
 *  publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 *  Licença, ou (caso queira) qualquer versão posterior.
 *  
 *  Este programa é distribuído na esperança de que possa ser  útil, 
 *  mas SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 *  MERCANTIBILIDADE OU ADEQUAÇÃO PARA UM FIM PARTICULAR. Veja a
 *  Licença Pública Geral GNU para maiores detalhes.
 *  
 *  Você deve ter recebido uma cópia da Licença Pública Geral GNU junto
 *  com este programa, Se não, veja em <http://www.gnu.org/licenses/>.
 * 
 *  @author Alberto Wagner <alberto@biblivre.org.br>
 *  @author Danniel Willian <danniel@biblivre.org.br>
 * 
 */
var Administration = Administration || {};

Administration.backup = {};
Administration.reindex = {};
Administration.reinstall = {};

$(document).ready(function() {
	
	var div = $('#last_backups_list');
	if (div.size() > 0) {
		div.setTemplateElement('last_backups_list_template');
	}

	$(document).on('click', '#last_backups_list a.backup', function(e) {
		e.preventDefault();
		Administration.backup.triggerDownload($(this).attr('href'));
	});
	
	Administration.backup.list();
});

//BACKUP

Administration.backup.selectedId = null;
Administration.backup.downloadFrameId = 'backup_download_iframe';

Administration.backup.list = function(id) {
	$('#last_backups_list').empty();

	$.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		loadingHolder: '#last_backups_list',
		data: {
			controller: 'json',
			module: 'administration.backup',
			action: 'list'
		},
		success: function(response) {
			$('#last_backups_list').processTemplate(response);
			if (id) {
				Administration.backup.download(id, 2000);
			}
		}
	});
};

Administration.backup.showAll = function(el) {
	var obj = $(el);
	
	obj.siblings('.hidden_backup').removeClass('hidden_backup');
	obj.remove();
};

Administration.backup.submit = function(type) {
	Administration.backup.showPopupProgress();

	var schemas = [];
	
	$('#multischema :checkbox:checked[name="library"]').each(function() {
		schemas.push($(this).val());
	});
	
	$.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.backup',
			action: 'prepare',
			schemas: schemas.join(),
			type: type
		},
		success: function(response) {
			if (!response.success) {
				Administration.backup.cancel();
				Core.msg(response);
				return;
			}

			$('.system_warning_backup').remove();
			
			Administration.backup.selectedId = response.id;
			Administration.backup.progress(100);

			$.ajax({
				url: window.location.pathname,
				type: 'POST',
				dataType: 'json',
				data: {
					controller: 'json',
					module: 'administration.backup',
					action: 'backup',
					id: Administration.backup.selectedId
				},
				success: function(response) {
					if (!response.success) {
						Administration.backup.cancel();
						Core.msg(response);
						return;
					}
				}
			}).fail(function() {
				Administration.backup.cancel();
			});
		}
	}).fail(function() {
		Administration.backup.cancel();
	});
};

Administration.backup.ensureBackupPopup = function() {
	var popup = $('#backup_popup');
	if (popup.length > 0) {
		return popup;
	}

	popup = $('<div id="backup_popup" class="popup"></div>');

	$('<div class="close"></div>')
		.text(_('common.close'))
		.appendTo(popup)
		.click(Administration.backup.cancel);

	var fieldset = $('<fieldset class="backup"></fieldset>').appendTo(popup);
	$('<legend></legend>').text(_('administration.maintenance.backup.title')).appendTo(fieldset);

	var progress = $('<div class="progress"></div>').appendTo(fieldset);
	$('<div class="progress_text"></div>').text(_('common.wait')).appendTo(progress);
	var progressBar = $('<div class="progress_bar"></div>').appendTo(progress);
	var progressOuter = $('<div class="progress_bar_outer"></div>').appendTo(progressBar);
	$('<div class="progress_bar_inner"></div>').appendTo(progressOuter);

	popup.appendTo('body');
	return popup;
};

Administration.backup.ensureCloudPopup = function() {
	var popup = $('#cloud_backup_popup');
	if (popup.length > 0) {
		return popup;
	}

	popup = $('<div id="cloud_backup_popup" class="popup"></div>');

	$('<div class="close"></div>')
		.text(_('common.close'))
		.appendTo(popup)
		.click(Administration.backup.cancelCloudProgress);

	var fieldset = $('<fieldset class="backup"></fieldset>').appendTo(popup);
	var legend = $('<legend></legend>').appendTo(fieldset);
	legend.append(document.createTextNode(_('administration.maintenance.backup.title')));
	legend.append('<span class="cloud_service_label"></span>');

	var progress = $('<div class="progress"></div>').appendTo(fieldset);
	$('<div class="progress_text"></div>').text(_('common.wait')).appendTo(progress);
	var progressBar = $('<div class="progress_bar"></div>').appendTo(progress);
	var progressOuter = $('<div class="progress_bar_outer"></div>').appendTo(progressBar);
	$('<div class="progress_bar_inner"></div>').appendTo(progressOuter);

	popup.appendTo('body');
	return popup;
};

Administration.backup.showPopupProgress = function() {
	Core.showOverlay();

	Administration.backup.ensureBackupPopup()
		.appendTo('body')
		.show()
		.center();
	
	$('#backup_popup .progress').progressbar(); 
};

Administration.backup.ensurePopupProgress = function() {
	var popup = Administration.backup.ensureBackupPopup();
	if (popup.length === 0) {
		return;
	}

	if (!popup.is(':visible')) {
		Core.showOverlay();
		popup
			.appendTo('body')
			.show()
			.center();
	}

	popup.find('.progress').progressbar();
};

Administration.backup.progressTimeout = null;
Administration.backup.progressXHR = null;
Administration.backup.cloudProgressTimeout = null;
Administration.backup.cloudProgressXHR = null;
Administration.backup.cloudSelectedId = null;
Administration.backup.cloudLastProgressData = null;
Administration.backup.cloudFakeProgressTimer = null;
Administration.backup.cloudFakeProgressValue = 0;
Administration.backup.progress = function(delay) {
	if (Administration.backup.selectedId == null) {
		return;
	} 
	
	if (delay) {
		Administration.backup.progressTimeout = setTimeout(Administration.backup.progress, delay);
		return;
	}
	
	Administration.backup.progressXHR = $.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.backup',
			action: 'progress',
			id: Administration.backup.selectedId
		},
		success: function(response) {
			if (!response.success) {
				Administration.backup.cancel();
				Core.msg(response);
				return;
			}

			Administration.backup.ensurePopupProgress();
			$('#backup_popup .progress').progressbar(response);

			if (response.complete) {
				var id = Administration.backup.selectedId;

				Administration.backup.cancel();
				Administration.backup.list(id);

				Core.msg(_('administration.maintenance.backup.auto_download'), 'success');								

				if (response.cloud_enabled) {
					setTimeout(function() {
						Administration.backup.startCloudProgress(id);
					}, 200);
				}
			}
		},
		error: function() {
			Administration.backup.cancel();
		},
		complete: function() {
			if (Administration.backup.selectedId != null) {
				Administration.backup.progress(500);
			}
		}
	});
};

Administration.backup.download = function(id, delay) {
	if (delay) {
		$('#last_backups_list a[rel=' + id + '] .backup_never_downloaded').text(_('administration.maintenance.backup.auto_download'));

		setTimeout(function() {
			Administration.backup.download(id);	
		}, delay);

		return;
	}

	Administration.backup.triggerDownload($('#last_backups_list a[rel=' + id + ']').attr('href'));
};

Administration.backup.triggerDownload = function(url) {
	if (!url) {
		return;
	}

	var iframe = $('#' + Administration.backup.downloadFrameId);
	if (iframe.size() === 0) {
		iframe = $('<iframe />', {
			id: Administration.backup.downloadFrameId,
			name: Administration.backup.downloadFrameId
		}).css('display', 'none').appendTo('body');
	}

	var separator = url.indexOf('?') >= 0 ? '&' : '?';
	iframe.attr('src', url + separator + '_download=' + new Date().getTime());
};

Administration.backup.cancel = function(base) {
	Administration.backup.selectedId = null;

	clearTimeout(Administration.backup.progressTimeout);
	if (Administration.backup.progressXHR) {
		Administration.backup.progressXHR.abort();
		Administration.backup.progressXHR = null;
	}
	
	Core.hideOverlay();
	$('#backup_popup').hide();
};

Administration.backup.showCloudPopupProgress = function() {
	Core.showOverlay();

	Administration.backup.ensureCloudPopup()
		.appendTo('body')
		.show()
		.center();
	
	$('#cloud_backup_popup .progress').progressbar(); 
};

Administration.backup.startCloudProgress = function(id) {
	Administration.backup.cloudSelectedId = id;
	Administration.backup.cloudLastProgressData = null;
	Administration.backup.cloudFakeProgressValue = 0;
	Administration.backup.showCloudPopupProgress();
	Administration.backup.cloudProgress(100);
};

Administration.backup.cloudProgress = function(delay) {
	if (Administration.backup.cloudSelectedId == null) {
		return;
	}

	if (delay) {
		Administration.backup.cloudProgressTimeout = setTimeout(Administration.backup.cloudProgress, delay);
		return;
	}

	Administration.backup.cloudProgressXHR = $.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.backup',
			action: 'cloud_progress',
			id: Administration.backup.cloudSelectedId
		},
		success: function(response) {
			if (!response.success) {
				Administration.backup.cancelCloudProgress();
				Core.msg(_('administration.maintenance.backup.cloud_upload.error'), 'error');
				return;
			}

			if (!response.total) {
				Administration.backup.cancelCloudProgress();
				Core.msg(_('administration.maintenance.backup.cloud_upload.error'), 'error');
				return;
			}

			var label = response.service_label || '';
			if (label) {
				label = ' (' + label + ')';
			}
			$('#cloud_backup_popup .cloud_service_label').text(label);

			// Detecta mudança para novo serviço
			var serviceChanged = false;
			if (Administration.backup.cloudLastProgressData) {
				if (Administration.backup.cloudLastProgressData.current !== response.current) {
					serviceChanged = true;
				}
			}

			Administration.backup.cloudLastProgressData = $.extend({}, response);

			// Se mudou de serviço, reseta o fake progress
			if (serviceChanged) {
				Administration.backup.cloudFakeProgressValue = 0;
			}

			Administration.backup.updateCloudProgressBar(response);

			if (response.complete) {
				// Completa a barra com animação
				$('#cloud_backup_popup .progress').progressbar({
					current: response.total,
					total: response.total,
					animate: true
				});

				setTimeout(function() {
					var errorCount = parseInt(response.error_count, 10) || 0;
					var messageKey = errorCount > 0
						? 'administration.maintenance.backup.cloud_upload.error'
						: 'administration.maintenance.backup.cloud_upload.success';
					var level = errorCount > 0 ? 'error' : 'success';
					Administration.backup.cancelCloudProgress();
					Core.msg(_(messageKey), level);
				}, 600);
				return;
			}
		},
		error: function(xhr, status) {
			if (status === 'abort' || Administration.backup.cloudSelectedId == null) {
				return;
			}
			
			Administration.backup.cancelCloudProgress();
			Core.msg(_('administration.maintenance.backup.cloud_upload.error'), 'error');
		},
		complete: function() {
			if (Administration.backup.cloudSelectedId != null) {
				Administration.backup.cloudProgress(500);
			}
		}
	});
};

Administration.backup.updateCloudProgressBar = function(data) {
	var baseProgress = 100 * data.current / data.total;
	var fakeProgress = 0;

	// Aumenta fake progress gradualmente (até 80% do passo atual)
	if (data.current < data.total) {
		Administration.backup.cloudFakeProgressValue = Math.min(
			Administration.backup.cloudFakeProgressValue + 0.04,
			0.8
		);
		fakeProgress = Administration.backup.cloudFakeProgressValue * (100 / data.total);
	}

	var totalProgress = baseProgress + fakeProgress;
	var totalProgress = Math.min(totalProgress, 100);

	// Calcula valores para exibição
	var displayCurrent = Math.floor(totalProgress * data.total / 100);
	var displayTotal = data.total;
	var displayProgress = totalProgress;

	// Atualiza o texto e a barra
	var secondary = '';
	$('#cloud_backup_popup .progress_text').text(
		secondary + _f(displayCurrent) + ' / ' + _f(displayTotal) + ' (' + displayProgress.toFixed(1) + '%)'
	);

	var $bar = $('#cloud_backup_popup .progress_bar_inner');
	$bar.stop().animate({ width: displayProgress + '%' }, 400, 'swing');
};

Administration.backup.cancelCloudProgress = function() {
	Administration.backup.cloudSelectedId = null;
	Administration.backup.cloudLastProgressData = null;
	Administration.backup.cloudFakeProgressValue = 0;

	clearTimeout(Administration.backup.cloudFakeProgressTimer);
	clearTimeout(Administration.backup.cloudProgressTimeout);
	if (Administration.backup.cloudProgressXHR) {
		Administration.backup.cloudProgressXHR.abort();
		Administration.backup.cloudProgressXHR = null;
	}

	Core.hideOverlay();
	$('#cloud_backup_popup').hide();
};


// REINDEX

Administration.reindex.selectedType = null;
Administration.reindex.confirm = function(type) {
	Administration.reindex.selectedType = type;

	Core.showOverlay();
	Administration.reindex.showPopupButtons();

	var popup = $('#reindex_popup');
	
	popup
		.appendTo('body')
		.show()
		.center()
		.height(popup.height());
	
};

Administration.reindex.showPopupButtons = function() {
	$('#reindex_popup .progress').hide();
	$('#reindex_popup .confirm, #reindex_popup .buttons').show();
	$('#reindex_popup .close').css('visibility', 'visible');
};

Administration.reindex.showPopupProgress = function() {
	var confirm = $('#reindex_popup .confirm, #reindex_popup .buttons');
	var progress = $('#reindex_popup .progress'); 
	var close = $('#reindex_popup .close');

	progress
		.progressbar()
		.show();
	
	confirm.hide();
	
	close.css('visibility', 'hidden');
};

Administration.reindex.submit = function() {
	$.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.indexing',
			action: 'reindex',
			record_type: Administration.reindex.selectedType
		},
		success: function(response) {
			if (!response.success) {
				Administration.reindex.cancel();
				Core.msg(response);
				return;
			}
		}
	});
	
	$('.system_warning_reindex').remove();

	Administration.reindex.showPopupProgress();
	Administration.reindex.progress(1000);
};

Administration.reindex.progressTimeout = null;
Administration.reindex.progressXHR = null;
Administration.reindex.progress = function(delay) {
	if (!Administration.reindex.selectedType) {
		return;
	}

	if (delay) {
		Administration.reindex.progressTimeout = setTimeout(Administration.reindex.progress, delay);
		return;
	}
	
	Administration.reindex.progressXHR = $.ajax({
		url: window.location.pathname,
		type: 'POST',
		dataType: 'json',
		data: {
			controller: 'json',
			module: 'administration.indexing',
			action: 'progress',
			record_type: Administration.reindex.selectedType
		},
		success: function(response) {
			if (!response.success) {
				Administration.reindex.cancel();
				Core.msg(response);
				return;
			}

			$('#reindex_popup .progress').progressbar(response);

			if (response.complete) {
				Administration.reindex.cancel();
				Core.msg(_('administration.maintenance.reindex.success'), 'success');
				return;
			}
		},
		error: function() {
			Administration.reindex.cancel();
		},
		complete: function() {
			if (Administration.reindex.selectedType) {
				Administration.reindex.progress(500);
			}
		}
	});
};

Administration.reindex.cancel = function(base) {
	Administration.reindex.selectedType = null;

	clearTimeout(Administration.reindex.progressTimeout);
	if (Administration.reindex.progressXHR) {
		Administration.reindex.progressXHR.abort();
		Administration.reindex.progressXHR = null;
	}
	
	Core.hideOverlay();
	$('#reindex_popup').hide();
};


Administration.reinstall.confirm = function() {
	Core.popup({
		title: _('administration.maintenance.reinstall.confirm.title'),
		description: _('administration.maintenance.reinstall.confirm.description'),
		confirm: _('administration.maintenance.reinstall.confirm.question'),
		okText: _('common.yes'),
		cancelText: _('common.no'),
		okHandler: function() {
			window.location.href = window.location.pathname + '?force_setup=true';
		},
		cancelHandler: $.proxy($.noop, this)
	});
};
