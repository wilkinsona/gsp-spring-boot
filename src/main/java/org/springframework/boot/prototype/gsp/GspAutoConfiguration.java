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

import javax.servlet.Servlet;

import org.codehaus.groovy.grails.plugins.web.GroovyPagesGrailsPlugin;
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
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
			return engine;
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
}
