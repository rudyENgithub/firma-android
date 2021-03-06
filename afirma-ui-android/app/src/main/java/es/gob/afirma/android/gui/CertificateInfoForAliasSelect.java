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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Clase que contiene la informaci&oacute;n del certificado.
 * Nombre com&uacute;n, fecha comienzo validez y fecha expiraci&oacute;n.
 * @author Astrid Idoate
 *
 */
public final class CertificateInfoForAliasSelect implements Serializable {

	/** Serial ID. */
	private static final long serialVersionUID = 1L;

	private String commonName;
	private Date notAfterDate;
	private Date notBeforeDate;
	private String alias;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("ES")); //$NON-NLS-1$ //$NON-NLS-2$
	private String issuer;

	/**
	 * @param commonName Nombre com&uacute;n del certificado.
	 * @param notBeforeDate Fecha en la que comienza la validez del certificado.
	 * @param notAfterDate Fecha de expiraci&oacute;n del certificado.
	 * @param alias Alias del certificado.
	 * @param issuer Emisor del certificado.
	 */
	public CertificateInfoForAliasSelect(final String commonName,final Date notBeforeDate, final Date notAfterDate, final String alias, final String issuer) {
		this.commonName = commonName;
		this.notBeforeDate = notBeforeDate;
		this.notAfterDate = notAfterDate;
		this.alias = alias;
		this.issuer = issuer;
	}

	/** Devuelve la fecha de expiraci&oacute;n del certificado.
	 * @return notAfterDate fecha de expiraci&oacute;n del certificado.
	 */
	String getNotAfterDate(){
		return this.sdf.format(this.notAfterDate);
	}

	/** Devuelve la fecha de comienzo de validez del certificado.
	 * @return notBeforeDate fecha en la que comienza la validez del certificado.
	 */
	String getNotBeforeDate(){
		return this.sdf.format(this.notBeforeDate);
	}

	/** Devuelve el nombre com&uacute;n del certificado
	 * @return commonName nombre com&uacute;n del certificado.
	 */
	String getCommonName(){
		return this.commonName;
	}

	/** Devuelve el alias del certificado.
	 * @return alias Alias del certificado.
	 */
	String getAlias(){
		return this.alias;
	}

	/** Devuelve el nombre del emisor del certificado.
	 * @return issuer Emisor del certificado.
	 */
	String getIssuer(){
		return this.issuer;
	}

}
