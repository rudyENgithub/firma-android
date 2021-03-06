/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.android;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

import es.gob.afirma.R;
import es.gob.afirma.android.ReadLocalFileTask.ReadLocalFileListener;
import es.gob.afirma.android.crypto.MSCBadPinException;
import es.gob.afirma.android.crypto.SignResult;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSignerFactory;

/** Esta actividad permite firmar un fichero local. La firma se guarda en un fichero <i>.csig</i>.
 * Esta clase tiene mucho c&oacute;digo duplicado de la clase <code>LocalSignResultActivity</code>.
 * Hay crear una nueva clase con los m&eacute;todos duplicados.
 * @author Astrid Idoate Gil. */
public final class LocalSignResultActivity extends SignFragmentActivity {

	private final static String EXTRA_RESOURCE_TITLE = "es.gob.afirma.android.title"; //$NON-NLS-1$
	private final static String EXTRA_RESOURCE_EXCLUDE_DIRS = "es.gob.afirma.android.excludedDirs"; //$NON-NLS-1$

	/** C&oacute;digo de solicitud de carga de fichero. */
	private final static int REQUEST_CODE_SELECT_FILE = 103;

	private static final String DEFAULT_SIGNATURE_ALGORITHM = "SHA1withRSA"; //$NON-NLS-1$

	private static final String PDF_FILE_SUFFIX = ".pdf"; //$NON-NLS-1$

	private final static String ES_GOB_AFIRMA = "es.gob.afirma"; //$NON-NLS-1$

	private final static String SAVE_INSTANCE_KEY_TITLE_VISIBILITY = "titleVisibility"; //$NON-NLS-1$
	private final static String SAVE_INSTANCE_KEY_OK_RESULT_VISIBILITY = "okVisibility"; //$NON-NLS-1$
	private final static String SAVE_INSTANCE_KEY_ERROR_RESULT_VISIBILITY ="errorVisibility"; //$NON-NLS-1$
	private final static String SAVE_INSTANCE_KEY_OK_TEXT = "okMessage"; //$NON-NLS-1$
	private final static String SAVE_INSTANCE_KEY_ERROR_TEXT = "errorMessage"; //$NON-NLS-1$

	String fileName; //Nombre del fichero seleccionado

	private String format = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signed_file);

		if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_INSTANCE_KEY_TITLE_VISIBILITY) && savedInstanceState.getBoolean(SAVE_INSTANCE_KEY_TITLE_VISIBILITY)) {
			findViewById(R.id.signedfile_title).setVisibility(View.VISIBLE);

			((TextView) findViewById(R.id.tv_signedfile_ko)).setText(savedInstanceState.getString(SAVE_INSTANCE_KEY_ERROR_TEXT));

			findViewById(R.id.signedfile_error).setVisibility(
					savedInstanceState.getBoolean(SAVE_INSTANCE_KEY_ERROR_RESULT_VISIBILITY) ? View.VISIBLE : View.INVISIBLE);

			((TextView) findViewById(R.id.filestorage_path)).setText(savedInstanceState.getString(SAVE_INSTANCE_KEY_OK_TEXT));

			findViewById(R.id.signedfile_correct).setVisibility(
					savedInstanceState.getBoolean(SAVE_INSTANCE_KEY_OK_RESULT_VISIBILITY) ? View.VISIBLE : View.INVISIBLE);
		}
		else {
			// Elegimos un fichero del directorio
			final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setClass(this, FileChooserActivity.class);
			intent.putExtra(EXTRA_RESOURCE_TITLE, getString(R.string.title_activity_choose_sign_file));
			intent.putExtra(EXTRA_RESOURCE_EXCLUDE_DIRS, FileSystemConstants.COMMON_EXCLUDED_DIRS); //$NON-NLS-1$
			startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
		}

	}

	@Override
	  public void onStop() {
	    super.onStop();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		// El usuario ha seleccionado un fichero
		if (requestCode == REQUEST_CODE_SELECT_FILE) {

			if (resultCode == RESULT_OK) {
				this.fileName = data.getStringExtra(FileChooserActivity.RESULT_DATA_STRING_FILENAME);

				new ReadLocalFileTask(
						new ReadLocalFileListener() {
							@Override
							public void setData(final Object readedData) {
								if (readedData instanceof OutOfMemoryError) {
									showErrorMessage(
											getString(R.string.file_read_out_of_memory)
									);
								} else if (readedData instanceof Exception) {
									showErrorMessage(
											getString(R.string.error_loading_selected_file, LocalSignResultActivity.this.fileName)
									);
								} else {
									LocalSignResultActivity.this.format = LocalSignResultActivity.this.fileName.toLowerCase(Locale.ENGLISH)
											.endsWith(PDF_FILE_SUFFIX) ?
											AOSignConstants.SIGN_FORMAT_PADES :
											AOSignConstants.SIGN_FORMAT_CADES;
									sign("SIGN", (byte[]) readedData, format, DEFAULT_SIGNATURE_ALGORITHM, null);
								}
							}
						}
				).execute(this.fileName);
			}
			else if (resultCode == RESULT_CANCELED) {
				finish();
				return;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	//Guarda los datos en un directorio del dispositivo y muestra por pantalla al usuario la informacion indicando donse se ha almacenado el fichero
	private void saveData(final SignResult signature){

		// Comprobamos que tenemos permisos de lectura sobre el directorio en el que se encuentra el fichero origen
		boolean originalDirectory;
		final String outDirectory;
		if (new File(this.fileName).getParentFile().canWrite()) {
			Logger.d(ES_GOB_AFIRMA, "La firma se guardara en el directorio del fichero de entrada"); //$NON-NLS-1$
			outDirectory = new File(this.fileName).getParent();
			originalDirectory = true;
		}
		else if (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).exists() && Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canWrite()) {
			Logger.d(ES_GOB_AFIRMA, "La firma se guardara en el directorio de descargas"); //$NON-NLS-1$
			outDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
			originalDirectory = false;
		}
		else {
			Logger.w(ES_GOB_AFIRMA, "No se ha encontrado donde guardar la firma generada"); //$NON-NLS-1$
			showErrorMessage(getString(R.string.error_no_device_to_store));
			return;
		}

		String inText = null;
		if (AOSignConstants.SIGN_FORMAT_PADES.equals(this.format)) {
			inText = "_signed"; //$NON-NLS-1$
		}

		int i = 0;
		final String signatureFilename = AOSignerFactory.getSigner(this.format).getSignedName(new File(this.fileName).getName(), inText);
		String finalSignatureFilename = signatureFilename;
		while (new File(outDirectory, finalSignatureFilename).exists()) {
			finalSignatureFilename = buildName(signatureFilename, ++i);
		}

		try {
			final FileOutputStream fos = new FileOutputStream(new File(outDirectory, finalSignatureFilename));
			fos.write(signature.getSignature());
			fos.flush();
			fos.close();
		}
		catch (final Exception e) {
			showErrorMessage(getString(R.string.error_saving_signature));
			Logger.e(ES_GOB_AFIRMA, "Error guardando la firma: " + e); //$NON-NLS-1$
			return;
		}

		showSuccessMessage(finalSignatureFilename, originalDirectory);

		// Refrescamos el directorio para permitir acceder al fichero
		try {
			MediaScannerConnection.scanFile(
				this,
				new String[] { new File(outDirectory, finalSignatureFilename).toString(),
						new File(outDirectory).toString()},
				null,
				null
			);
		}
		catch(final Exception e) {
			Logger.w(ES_GOB_AFIRMA, "Error refrescando el MediaScanner: " + e); //$NON-NLS-1$
		}
	}

	/** Muestra los elementos de pantalla informando de un error ocurrido durante la operaci&oacute;n de
	 * firma.
	 * @param message Mensaje que describe el error producido. */
	private void showErrorMessage(final String message) {

		// Ya cerrados los dialogos modales, mostramos el titulo de la pantalla
		final TextView tvTitle = findViewById(R.id.signedfile_title);
		tvTitle.setVisibility(View.VISIBLE);

		final RelativeLayout rl = findViewById(R.id.signedfile_error);
		rl.setVisibility(View.VISIBLE);

	}

	/** Muestra los elementos de pantalla informando de que la firma se ha generado correctamente y
	 * donde se ha almacenado.
	 * @param filename Nombre del fichero almacenado.
	 * @param originalDirectory Directorio donde estaba originalmente el fichero que se firm&oacute;. */
	private void showSuccessMessage(final String filename, final boolean originalDirectory) {

		// Ya cerrados los dialogos modales, mostramos el titulo de la pantalla
		final TextView tvTitle = findViewById(R.id.signedfile_title);
		tvTitle.setVisibility(View.VISIBLE);

		//activo los elementos de la interfaz que corresponden a la firma correcta de un fichero
		final TextView tv_sf= findViewById(R.id.filestorage_path);
		tv_sf.setText(getString(originalDirectory ?
				R.string.signedfile_original_location :
					R.string.signedfile_downloads_location, filename));

		final RelativeLayout rl = findViewById(R.id.signedfile_correct);
		rl.setVisibility(View.VISIBLE);
	}

	@Override
	public void onSigningSuccess(final SignResult signature) {
		saveData(signature);
	}

	/** Construye un nombre apropiado para un fichero de firma en base a un nombre base
	 * y un &iacute;ndice.
	 * @param originalName Nombre base del fichero.
	 * @param index &Iacute;ndice.
	 * @return Nombre apropiado para el fichero de firma. */
	private static String buildName(final String originalName, final int index) {

		String indexSuffix = ""; //$NON-NLS-1$
		if (index > 0) {
			indexSuffix = "(" + index + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		final int dotPos = originalName.lastIndexOf('.');
		if (dotPos == -1) {
			return originalName + indexSuffix;
		}
		return originalName.substring(0, dotPos) + indexSuffix + originalName.substring(dotPos);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(SAVE_INSTANCE_KEY_TITLE_VISIBILITY,
				findViewById(R.id.signedfile_title).getVisibility() == View.VISIBLE);

		outState.putString(SAVE_INSTANCE_KEY_OK_TEXT,
				((TextView) findViewById(R.id.filestorage_path)).getText().toString());

		outState.putBoolean(SAVE_INSTANCE_KEY_OK_RESULT_VISIBILITY,
				findViewById(R.id.signedfile_correct).getVisibility() == View.VISIBLE);

		outState.putString(SAVE_INSTANCE_KEY_ERROR_TEXT,
				((TextView) findViewById(R.id.tv_signedfile_ko)).getText().toString());

		outState.putBoolean(SAVE_INSTANCE_KEY_ERROR_RESULT_VISIBILITY,
				findViewById(R.id.signedfile_error).getVisibility() == View.VISIBLE);
	}

	@Override
	protected void onSigningError(KeyStoreOperation op, String msg, Throwable t) {

		if (KeyStoreOperation.SELECT_CERTIFICATE == op && t instanceof PendingIntent.CanceledException) {
			Logger.w(ES_GOB_AFIRMA, "Operacion de seleccion de certificados cancelada por el usuario");
			finish();
		}
		else {
			if (KeyStoreOperation.SIGN == op) {
				if (t instanceof MSCBadPinException) {
					showErrorMessage(getString(R.string.error_msc_pin));
				}
				else {
					showErrorMessage(getString(R.string.error_signing));
				}
			}
			else {
				showErrorMessage(msg);
			}

			// Ya cerrados los dialogos modales, mostramos el titulo de la pantalla
			final TextView tvTitle = findViewById(R.id.signedfile_title);
			tvTitle.setVisibility(View.VISIBLE);

			final RelativeLayout rl = findViewById(R.id.signedfile_error);
			rl.setVisibility(View.VISIBLE);
			Logger.e(ES_GOB_AFIRMA, "Error durante la firma: " + t);
		}
	}
}

