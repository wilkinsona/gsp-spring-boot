/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.prototype.gsp;

import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.grails.commons.DefaultGrailsTagLibClass;
import org.codehaus.groovy.grails.commons.GrailsTagLibClass;
import org.codehaus.groovy.grails.compiler.web.taglib.TagLibraryTransformer;
import org.codehaus.groovy.grails.plugins.web.GroovyPagesGrailsPlugin;
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine;
import org.codehaus.groovy.grails.web.pages.TagLibraryLookup;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@ConditionalOnClass(GroovyPagesGrailsPlugin.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class GspAutoConfiguration {


	@Configuration
	@ConditionalOnMissingBean(GroovyPagesTemplateEngine.class)
	protected static class GspDefaultConfiguration {

		// Bean must be called groovyPagesTemplateEngine so that it can be retrieved by DefaultGrailsApplicationAttributes
		@Bean
		public GroovyPagesTemplateEngine groovyPagesTemplateEngine() {
			GroovyPagesTemplateEngine engine = new GroovyPagesTemplateEngine();
			engine.setTagLibraryLookup(tagLibraryLookup());
			return engine;
		}

		@Bean
		public TagLibraryLookup tagLibraryLookup() {
			return new SpringBootTagLibraryLookup();
		}
	}

	@Configuration
	@ConditionalOnClass({ Servlet.class })
	protected static class GspViewResolverConfiguration {

		@Autowired
		private ResourceLoader resourceLoader = new DefaultResourceLoader();

		@Autowired
		private GroovyPagesTemplateEngine templateEngine;

		@Bean
		@ConditionalOnMissingBean(name = "gspViewResolver")
		public GspViewResolver gspViewResolver() {
			return new GspViewResolver(this.templateEngine, this.resourceLoader);
		}
	}

	private static final class SpringBootTagLibraryLookup extends TagLibraryLookup {

		private final Log logger = LogFactory.getLog(getClass());

		private final GroovyClassLoader classLoader;

		private SpringBootTagLibraryLookup() {
			CompilerConfiguration compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
			compilerConfiguration.addCompilationCustomizers(new ASTTransformationCustomizer(new TagLibraryTransformerASTTransformation()));
			this.classLoader = new GroovyClassLoader(getClass().getClassLoader(), compilerConfiguration, false);
		}

		public void afterPropertiesSet() {
			registerTagLibraries();
			registerTemplateNamespace();
		}

		protected void registerTagLibraries() {
			Resource[] tagLibResources = null;
			try {
				tagLibResources = applicationContext.getResources("classpath:taglib/**/*TagLib.groovy");
			} catch (IOException ex) {
				this.logger.warn("Failed to get taglib classpath resources", ex);
			}

			if (tagLibResources != null) {
				for (Resource tagLibResource: tagLibResources) {
					try {
						registerTagLib(compileTagLibrary(tagLibResource));
					} catch (IOException ex) {
						this.logger.warn("Failed to compile tag library from resource '" + tagLibResource + "'");
					}
				}
			}
		}

		private GrailsTagLibClass compileTagLibrary(Resource tagLibResource) throws IOException {
			Class<?> compiledTagLibrary = classLoader.parseClass(tagLibResource.getFile());
			return new DefaultGrailsTagLibClass(compiledTagLibrary);
		}

		@Override
		protected void putTagLib(Map<String, Object> tags, String name, GrailsTagLibClass taglib) {
			tags.put(name, taglib.newInstance());
		}
	}

	@GroovyASTTransformation
	private static final class TagLibraryTransformerASTTransformation implements ASTTransformation {
		@Override
		public void visit(ASTNode[] nodes, SourceUnit source) {
			for (ClassNode classNode : source.getAST().getClasses()) {
				new TagLibraryTransformer().performInjection(source, classNode);
			}
		}
	}
}
