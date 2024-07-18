<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" omit-xml-declaration="yes" />
	<!-- directb2sReelSoundsONOFF.xsl xslt transformation to add empty sound attributes for directb2s files -->

	<!-- Identity template: copy all nodes and attributes as-is -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="Scoret/@*[starts-with(name(), 'Sound') and @* ='']" >
		<xsl:message>
This does not work yet
		</xsl:message>
	</xsl:template>

	<!-- Match the "Score" node and add the "Sound1-6" empty attributes -->
	<xsl:template match="Score[not(@Sound1)]">
		<xsl:message>
Adding the Sound -<xsl:value-of select="@Digits"/> attributes to Score ID="<xsl:value-of select="@ID"/>"
		</xsl:message>

		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="Sound1"></xsl:attribute>
			<xsl:if test="@Digits &gt; 1">
				<xsl:attribute name="Sound2"></xsl:attribute>
			</xsl:if>
			<xsl:if test="@Digits &gt; 2">
				<xsl:attribute name="Sound3"></xsl:attribute>
			</xsl:if>
			<xsl:if test="@Digits &gt; 3">
				<xsl:attribute name="Sound4"></xsl:attribute>
			</xsl:if>
			<xsl:if test="@Digits &gt; 4">
				<xsl:attribute name="Sound5"></xsl:attribute>
			</xsl:if>
			<xsl:if test="@Digits &gt; 5">
				<xsl:attribute name="Sound6"></xsl:attribute>
			</xsl:if>
			<xsl:if test="@Digits &gt; 6">
				<xsl:attribute name="Sound7"></xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Match the "Score" with a "Sound1" node error out -->
	<xsl:template match="Score[(@Sound1)]">
		<xsl:message terminate="yes">
The sound attribute(s) already exists
		</xsl:message>
	</xsl:template>

</xsl:stylesheet>
