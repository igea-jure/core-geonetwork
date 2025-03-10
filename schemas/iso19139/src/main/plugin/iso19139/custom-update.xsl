<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:java="java:org.fao.geonet.util.XslUtil" exclude-result-prefixes="#all">

	<xsl:template match="gmd:MD_LegalConstraints[gmd:accessConstraints]/gmd:otherConstraints/gmx:Anchor">
        <gmx:Anchor>
        <xsl:choose>
            <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations'">
                <xsl:copy-of select="@*" />
        	    Ni omejitev
    	    </xsl:when>
            <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1a'">
                <xsl:copy-of select="@*" />
                Zaupnost postopkov javnih organov
    	    </xsl:when>
    	    <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1b'">
                <xsl:copy-of select="@*" />
                Mednarodni odnosi, javna varnost, obramba
    	    </xsl:when>
    	    <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1c'">
                <xsl:copy-of select="@*" />
                Sodni postopki
    	    </xsl:when>
    	    <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1d'">
                <xsl:copy-of select="@*" />
                Poslovna skrivnost
    	    </xsl:when>
    	    <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1e'">
                <xsl:copy-of select="@*" />
                Intelektualna lastnina
    	    </xsl:when>
    	    <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1f'">
                <xsl:copy-of select="@*" />
                Osebni podatki
    	    </xsl:when>
    	    <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1g'">
                <xsl:copy-of select="@*" />
                Zaščita osebe
    	    </xsl:when>
    	    <xsl:when test="@xlink:href = 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1h'">
                <xsl:copy-of select="@*" />
                Okolje
    	    </xsl:when>
    	    <xsl:otherwise>
    	    <xsl:copy-of select="." />
    	    </xsl:otherwise>
        </xsl:choose>
        </gmx:Anchor>
	</xsl:template>
	
	<xsl:template match="@*|node()">
	    <xsl:copy>
	        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
	</xsl:template>

</xsl:stylesheet>
