<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">Schematron validation / IJZ metadata profile recommendations</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>    

    <!-- =============================================================
    GeoNetwork schematron rules:
    ============================================================= -->
    <!-- #14, jure, 20180521 -->
    <sch:pattern>
      
      <sch:title>$loc/strings/ijzDatasets</sch:title>
      
      <!-- Dataset and series only -->
      <sch:rule context="//gmd:MD_DataIdentification[
                        normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'series'
                        or normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'dataset'
                        or normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = '']|
                        //*[@gco:isoType='gmd:MD_DataIdentification' and (
                        normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'series'
                        or normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'dataset'
                        or normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = '')]">
        
        <sch:let name="countCreationDate"
          value="count(gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='creation'])"/>

        <sch:assert test="$countCreationDate > 0">
            <sch:value-of select="$loc/strings/alert.M1001.dateOfCreation"/>
        </sch:assert>
        <sch:report test="$countCreationDate > 0">
            <sch:value-of select="$loc/strings/report.M1001.dateOfCreation"/>
        </sch:report>
        
      </sch:rule>
    </sch:pattern>
</sch:schema>
