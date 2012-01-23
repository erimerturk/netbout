<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:nb="http://www.netbout.com"
    version="2.0" exclude-result-prefixes="xs">

    <xsl:template match="stage" mode="head">
        <!-- nothing -->
    </xsl:template>

    <xsl:template match="stage">
        <ul>
            <xsl:for-each select="data/docs/doc">
                <li>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="link[@rel='load']/@href"/>
                        </xsl:attribute>
                        <xsl:value-of select="title"/>
                    </a>
                    <xsl:value-of select="type"/>
                    <xsl:value-of select="author"/>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="link[@rel='unshare']/@href"/>
                        </xsl:attribute>
                        <xsl:text>unshare</xsl:text>
                    </a>
                </li>
            </xsl:for-each>
        </ul>
        <form method="post">
            <xsl:attribute name="action">
                <xsl:value-of select="$stage-home-uri"/>
            </xsl:attribute>
            <input name="uri" size="40" maxlength="500"/>
            <input value="Share" type="submit"/>
        </form>
    </xsl:template>

</xsl:stylesheet>
