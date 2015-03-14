<?xml version="1.0"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xslt"
>

  <!-- Note: the output is only indented when using xalan, as I could not find a general way to cause this to happen.  -->
  <xsl:output method="xml" indent="yes" xalan:indent-amount="2"/>

  <xsl:key name="depend-names" match="depend/@name" use="."/>
  <xsl:template name="distinct2">
    <xsl:param name="elements"/>
    <xsl:for-each select="$elements/@name[generate-id() = generate-id(key('depend-names',.))]">
      <xsl:value-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="make-project-name-attribute-list">
    <!-- Turns the $elements list into a separated string with $prefix prepended and $postfix appended to each element. -->
    <xsl:param name="elements"/>
    <xsl:param name="prefix"/>
    <xsl:param name="suffix"/>
    <xsl:param name="seperator"/>
    <xsl:for-each select="$elements">
      <xsl:value-of select="concat($prefix,@name,$suffix)" />
      <xsl:if test="position() != last()"><xsl:value-of select="$seperator" /></xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="make-name-attribute-list">
    <!-- Turns the $elements list into a separated string with $prefix prepended and $postfix appended to each element. -->
    <xsl:param name="elements"/>
    <xsl:param name="prefix"/>
    <xsl:param name="suffix"/>
    <xsl:param name="seperator"/>
    <xsl:for-each select="$elements">
      <xsl:value-of select="concat($prefix,@name,$suffix)" />
      <xsl:if test="position() != last()"><xsl:value-of select="$seperator" /></xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="make-unique-name-attribute-list">
    <!-- Turns the $elements list into a separated string with $prefix prepended and $postfix appended to each element. -->
    <xsl:param name="elements"/>
    <xsl:param name="prefix"/>
    <xsl:param name="suffix"/>
    <xsl:param name="seperator"/>
    <!-- xsl:for-each select="$elements" -->
    <xsl:for-each select="$elements/@name[generate-id() = generate-id(key('depend-names',.))]">
      <!-- xsl:value-of select="concat($prefix,@name,$suffix)" / -->
      <xsl:value-of select="concat($prefix,.,$suffix)" />
      <xsl:if test="position() != last()"><xsl:value-of select="$seperator" /></xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="concat-with-seperator">
    <!-- If either first or second is not null, return the two seperated by a seperator. -->
    <xsl:param name="first"/>
    <xsl:param name="second"/>
    <xsl:param name="seperator"/>
    <xsl:choose>
      <xsl:when test="not(boolean(normalize-space(string($first))))"><xsl:value-of select="$second" /></xsl:when>
      <xsl:when test="not(boolean(normalize-space(string($second))))"><xsl:value-of select="$first" /></xsl:when>
      <xsl:otherwise><xsl:value-of select="concat($first,$seperator,$second)" /></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="project-list">
    <project name="master-build" default="build-all-projects" basedir="..">
      <property file="build/local.project.properties"/>
      <property file="build/local.properties"/>
      <property file="build/project.properties"/>

      <property name="allproject.dir" value="${{basedir}}"/>

      <property name="build.compiler.emacs" value="true"/>
      <property name="build.log" value="${{allproject.dir}}/build/targetlogs" />
      <property name="build.buildcommand" value="deploy"/>
      <property name="build.cleancommand" value="clean"/>
      <property name="build.fixcommand" value="fix"/>      
      <property name="build.tar.buildcommand" value="tar"/>
      <property name="build.tar.cleancommand" value="tar-clean"/>
      <property name="javadoc.command" value="javadoc"/>

      <target name="init" description="setup tasks done before all others">
      <tstamp />
      </target>

      <xsl:for-each select="project">
        <xsl:choose>
          <xsl:when test="normalize-space(@build)!='false'">

           <xsl:variable name="clean-depend-list">
              <xsl:call-template name="make-name-attribute-list">
                <xsl:with-param name="elements" select="depend[@type='project']" />
                <xsl:with-param name="prefix" select="'clean-'" />
                <xsl:with-param name="seperator" select="', '" />
              </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="clean-depend-list-with-init">
              <xsl:call-template name="concat-with-seperator">
                <xsl:with-param name="first" select="'init'" />
                <xsl:with-param name="second" select="$clean-depend-list" />
                <xsl:with-param name="seperator" select="', '" />
              </xsl:call-template>
            </xsl:variable>

            <target name="clean-only-{@name}" depends="init" description="Clear compiled code only from {@name}">
              <echo message="Cleaning only {@name}"/>
              <ant inheritall="false" dir="${{allproject.dir}}/build" antfile="standard-build.xml" target="${{build.cleancommand}}">
                <property name="allproject.dir" value="${{allproject.dir}}"/>
                <property name="project.name" value="{@name}"/>
                <property name="project.work.dir" value="${{allproject.dir}}/{@directory}"/>
              </ant>
            </target>

            <target name="clean-{@name}" depends="{$clean-depend-list-with-init}, clean-only-{@name}" description="Clear compiled code from {@name} and dependencies" />

           <xsl:variable name="build-depend-list">
              <xsl:call-template name="make-name-attribute-list">
                <xsl:with-param name="elements" select="depend[@type='project']" />
                <xsl:with-param name="prefix" select="'build-'" />
                <xsl:with-param name="seperator" select="', '" />
              </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="build-depend-list-with-init">
              <xsl:call-template name="concat-with-seperator">
                <xsl:with-param name="first" select="'init'" />
                <xsl:with-param name="second" select="$build-depend-list" />
                <xsl:with-param name="seperator" select="', '" />
              </xsl:call-template>
            </xsl:variable>

           <xsl:variable name="project-classpath-list">
              <xsl:call-template name="make-name-attribute-list">
                <xsl:with-param name="elements" select="depend[@type='project']" />
                <xsl:with-param name="suffix" select="'.jar'" />
                <xsl:with-param name="seperator" select="', '" />
              </xsl:call-template>
            </xsl:variable>

           <xsl:variable name="library-classpath-list">
              <xsl:call-template name="make-name-attribute-list">
                <xsl:with-param name="elements" select="depend[@type='library']" />
                <xsl:with-param name="suffix" select="'.jar'" />
                <xsl:with-param name="seperator" select="', '" />
              </xsl:call-template>
            </xsl:variable>

            <target name="build-only-{@name}" depends="init" description="Build only {@name}">
              <echo message="Building only {@name} without dependencies"/>
              <mkdir dir="${{build.log}}"/>
              <ant inheritall="false" dir="${{allproject.dir}}/build" antfile="standard-build.xml" target="${{build.buildcommand}}" output="${{build.log}}/{@name}">
                <property name="allproject.dir" value="${{allproject.dir}}"/>
                <property name="project.name" value="{@name}"/>
                <property name="project.work.dir" value="${{allproject.dir}}/{@directory}"/>
                <xsl:choose>
                    <!-- only include library information if the project depends on at least one library -->
                  <xsl:when test="normalize-space($library-classpath-list)">
                    <property name="compile.library.classpath" value="{$library-classpath-list}"/>
                    <xsl:choose>
                        <!-- include libraries in the bundle if asked -->
                      <xsl:when test="normalize-space(@jar.bundle)='true'">
                        <property name="jar.extra.library.zip" value="{$library-classpath-list}"/>
                      </xsl:when>
                    </xsl:choose>
                  </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <!-- only include other project jars in the classpath if the project depends on at least one other project -->
                  <xsl:when test="normalize-space($project-classpath-list)">
                    <property name="compile.product.classpath" value="{$project-classpath-list}"/>
                    <xsl:choose>
                        <!-- include other project dependencies in the bundle if asked -->
                      <xsl:when test="normalize-space(@jar.bundle)='true'">
                        <property name="jar.extra.product.zip" value="{$project-classpath-list}"/>
                      </xsl:when>
                    </xsl:choose>
                  </xsl:when>
                </xsl:choose>
                <xsl:choose>
                  <xsl:when test="normalize-space(@jar.main-class)">
                    <!-- when main class is specified, bundle main class -->
                    <property name="jar.main-class" value="{@jar.main-class}"/>
                  </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <!-- Pass jar sign request in to standard-build -->
                  <xsl:when test="normalize-space(@jar.sign)='true'">
                    <property name="jar.sign" value="true"/>
                  </xsl:when>
                </xsl:choose>
              </ant>

              <xsl:choose>
                <xsl:when test="normalize-space(@tar.deploy)='true'">
                  <xsl:variable name="files-to-tar">
                    <xsl:call-template name="make-name-attribute-list">
                      <xsl:with-param name="elements" select="tar/file[normalize-space(@executable)!='true']" />
                      <xsl:with-param name="seperator" select="', '" />
                    </xsl:call-template>
                  </xsl:variable>

                  <xsl:variable name="executables-to-tar">
                    <xsl:call-template name="make-name-attribute-list">
                      <xsl:with-param name="elements" select="tar/file[normalize-space(@executable)='true']" />
                      <xsl:with-param name="seperator" select="', '" />
                    </xsl:call-template>
                  </xsl:variable>

                  <ant inheritall="false" dir="${{allproject.dir}}/build" antfile="standard-build.xml" target="${{build.tar.buildcommand}}" output="${{build.log}}/{@name}">
                    <property name="allproject.dir" value="${{allproject.dir}}"/>
                    <property name="project.name" value="{@name}"/>
                    <property name="project.work.dir" value="${{allproject.dir}}/{@directory}"/>
                    <xsl:choose>
                      <xsl:when test="normalize-space(tar/@tar-directory)">
                        <property name="tar.file.dir" value="${{allproject.dir}}/{tar/@tar-directory}"/>
                      </xsl:when>
                    </xsl:choose>
                    <xsl:choose>
                      <xsl:when test="normalize-space($files-to-tar)">
                        <property name="tar.file.names" value="{$files-to-tar}"/>
                      </xsl:when>
                    </xsl:choose>
                    <xsl:choose>
                      <xsl:when test="normalize-space($executables-to-tar)">
                        <property name="tar.file.executable-names" value="{$executables-to-tar}"/>
                      </xsl:when>
                    </xsl:choose>
                  </ant>
                </xsl:when>
              </xsl:choose>

            </target>

            <target name="build-{@name}" depends="{$build-depend-list-with-init}, build-only-{@name}" description="Build {@name} and dependencies"/>

            <target name="clean-build-{@name}"
              depends="clean-{@name}, build-{@name}"
              description="Build {@name} with clean step"
            />

            <target name="clean-build-only-{@name}"
              depends="clean-only-{@name}, build-only-{@name}"
              description="Build only {@name} with clean step"
            />
          </xsl:when>

          <xsl:when test="normalize-space(@javadoc)!='false'">
            <target name="javadoc-{@name}" depends="init" description="Javadoc {@name}">
              <echo message="Javadoc {@name}"/>
              <ant dir="{@directory}" target="${{javadoc.command}}"/>
            </target>
          </xsl:when>
        </xsl:choose>
<!-- Create targets for fixing line ends -->
      <xsl:choose>
        <xsl:when test="normalize-space(@jar.bundle)!='true' and normalize-space(@web.bundle)!='true'">
	 <target name="fix-only-{@name}" depends="init" description="Fix line endings in project {@name}">
	  <echo message="Fixing line ends in project {@name}" /> 
	  <ant inheritall="false" dir="${{allproject.dir}}/build" antfile="standard-build.xml" target="${{build.fixcommand}}">
	  <property name="allproject.dir" value="${{allproject.dir}}" /> 
	  <property name="project.name" value="{@name}" /> 
          <xsl:if test="normalize-space(@build)!='false'">
	   <property name="project.work.dir" value="${{allproject.dir}}/{@directory}" /> 
	  </xsl:if>
          <xsl:if test="normalize-space(@build)='false'">
	   <property name="project.work.dir" value="${{allproject.dir}}/{@name}" /> 
	  </xsl:if>
	  </ant>
	 </target>
       </xsl:when>
      </xsl:choose>
      </xsl:for-each>

          <!-- xsl:with-param name="elements" select="project[not(@build='false')]/depend[@type='project']" / -->
          <!-- xsl:with-param name="elements" select="project[not(@build='false')]" / -->
      <xsl:variable name="build-all-depend-list">
        <xsl:call-template name="make-project-name-attribute-list">
          <xsl:with-param name="elements" select="project[not(@build='false')]" />
          <xsl:with-param name="prefix" select="'build-'" />
          <xsl:with-param name="seperator" select="', '" />
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="build-all-depend-list-with-init">
        <xsl:call-template name="concat-with-seperator">
          <xsl:with-param name="first" select="'init'" />
          <xsl:with-param name="second" select="$build-all-depend-list" />
          <xsl:with-param name="seperator" select="', '" />
        </xsl:call-template>
      </xsl:variable>

      <target name="build-all-projects" description="Build all projects" depends="{$build-all-depend-list-with-init}" />

      <xsl:variable name="clean-all-depend-list">
        <xsl:call-template name="make-project-name-attribute-list">
          <xsl:with-param name="elements" select="project[not(@build='false')]" />
          <xsl:with-param name="prefix" select="'clean-'" />
          <xsl:with-param name="seperator" select="', '" />
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="clean-all-depend-list-with-init">
        <xsl:call-template name="concat-with-seperator">
          <xsl:with-param name="first" select="'init'" />
          <xsl:with-param name="second" select="$clean-all-depend-list" />
          <xsl:with-param name="seperator" select="', '" />
        </xsl:call-template>
      </xsl:variable>

      <target name="clean-all-projects" description="Clean all projects" depends="{$clean-all-depend-list-with-init}" />

      <target name="clean-build-all-projects"
        description="Build all projects after cleaning"
        depends="clean-all-projects, build-all-projects">
        <echo message="clean-build-all-projects"/>
      </target>

      <xsl:variable name="javadoc-library-list">
        <xsl:call-template name="make-unique-name-attribute-list">
          <xsl:with-param name="elements" select="project[not(@build='false')]/depend[@type='library']" />
          <xsl:with-param name="suffix" select="'.jar'" />
          <xsl:with-param name="seperator" select="', '" />
        </xsl:call-template>
      </xsl:variable>

      <path id="javadoc.source.dirs">
        <xsl:for-each select="project[not(@javadoc='false')]" >
          <dirset dir="{@directory}">
            <include name="source"/>
            <include name="source-generated"/>
          </dirset>
        </xsl:for-each>
      </path>

      <target name="javadoc-to-zip" depends="init" description="Create javadoc files for projects">
        <!-- these are pulled from a properties file for only this target, as only this target needs it. -->
        <property file="build/javadoc.properties"/>
        <mkdir dir="${{javadoc.dir}}"/>
        <javadoc
          sourcepathref="javadoc.source.dirs"
          packagenames="${{javadoc.package}}.*"
          destdir="${{javadoc.dir}}"
          use="true"
          maxmemory="${{javadoc.memoryMaximumSize}}"
          windowtitle="${{javadoc.description}}"
          doctitle="${{javadoc.description}}"
          bottom="Copyright &amp;copy; 2002  Robin Warren. All Rights Reserved.">
          <classpath>
            <filelist dir="${{compile.library.dir}}" files="{$javadoc-library-list}"/>
          </classpath>
        </javadoc>
        <zip
          zipfile="${{compile.product.dir}}/javadoc.zip"
          basedir="${{javadoc.dir}}"
          compress="true"
        />
      </target>

      <target name="javadoc-zip-extract" depends="init" description="Extract the javadocs to another destination" if="javadoc.copy.dest">
        <!-- these are pulled from a properties file for only this target, as only this target needs it. -->
        <property file="build/javadoc.properties"/>
        <mkdir dir="${{javadoc.copy.dest}}"/>
        <unzip
          src="${{compile.product.dir}}/javadoc.zip"
          dest="${{javadoc.copy.dest}}"
        />
      </target>

      <target name="javadoc-all-projects" depends="javadoc-to-zip, javadoc-zip-extract" description="Create javadoc files for projects">
      </target>

      <target name="clean-products" depends="init" description="Remove all jars from the products directory"> 
        <!-- these are pulled from a properties file for only this target, as only this target needs it. -->
        <property file="build/javadoc.properties"/>
        <delete>
          <fileset dir="${{compile.product.dir}}" includes="**/*.jar"/>
        </delete>
      </target>

     </project>
    </xsl:template>

</xsl:stylesheet>