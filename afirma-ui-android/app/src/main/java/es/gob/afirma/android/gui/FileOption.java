/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.android.gui;

import java.io.File;
import java.util.Locale;

/** Clase que almac&eacute;n los datos b&aacute;sicos de un directorio o fichero del sistema operativo
 * @author Alberto Mart&iacute;nez */
public final class FileOption implements Comparable<FileOption> {
	private final String name;
	private final boolean directory;
	private final String path;
	private final long size;

	/** Construye una representaci&oacute;n de los datos b&aacute;sicos de un directorio o fichero.
	 * @param f Directorio o fichero. */
	public FileOption(final File f) {
		this.name = f.getName();
		this.directory = f.isDirectory();
		this.path = f.getAbsolutePath();
		if (f.isFile()) {
			this.size = f.length();
		} else {
			this.size = 0;
		}
	}

	/** Construye una representaci&oacute;n de los datos b&aacute;sicos de un directorio o fichero. Permite indicar
	 * si se desea obtener la informaci&oacute;n del directorio padre del indicado, en lugar del propio fichero.
	 * @param f Directorio o fichero.
	 * @param parent <code>true</code> si es "..", <code>false</code> en caso contrario */
	public FileOption(final File f, final boolean parent) {

		final File file;
		if (parent) {
			file = f.getParentFile();
			this.name = ".."; //$NON-NLS-1$
		}
		else {
			file = f;
			this.name = f.getName();
		}
		this.directory = file.isDirectory();
		this.path = file.getAbsolutePath();
		if (file.isFile()) {
			this.size = file.length();
		}
		else {
			this.size = 0;
		}
	}

	/** Proporciona el nombre del elemento
	 * @return Nombre del fichero */
	public String getName() {
		return this.name;
	}

	/** Indica si el fichero es un directorio.
	 * @return Devuelve {@code true} si el fichero es un directorio, {@code false} en caso contrario.
	 */
	public boolean isDirectory() {
		return this.directory;
	}

	/** Proporciona la ruta absoluta del fichero.
	 * @return Ruta del fichero */
	public String getPath() {
		return this.path;
	}

	/** Devuelve el tama&ntilde;o del fichero.
	 * @return Tama&ntilde;o del fichero. Si es un directorio, devuelve 0.
	 */
	long getSize() {
		return this.size;
	}

	@Override
	public int compareTo(final FileOption o) {
		if (this.name != null) {
			return this.name.toLowerCase(Locale.ENGLISH).compareTo(o.getName().toLowerCase());
		}
		throw new IllegalArgumentException();
	}
}
