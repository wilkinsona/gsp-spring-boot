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

import groovy.text.Template;

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;

public class GspViewResolver extends AbstractCachingViewResolver implements Ordered {

	private final GroovyPagesTemplateEngine templateEngine;

	private final ResourceLoader resourceLoader;

	public GspViewResolver(GroovyPagesTemplateEngine templateEngine, ResourceLoader resourceLoader) {
		this.templateEngine = templateEngine;
		this.resourceLoader = resourceLoader;
	}

	@Override
	protected View loadView(String viewName, Locale locale) throws Exception {
		Resource resource = this.resourceLoader.getResource("classpath:templates/" + viewName + ".gsp");
		final Template template = this.templateEngine.createTemplate(resource);
		return new View() {

			@Override
			public String getContentType() {
				return "text/html";
			}

			@Override
			public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
				RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
				try {
					ServletContext servletContext = getServletContext();
					servletContext.setAttribute("org.codehaus.groovy.grails.APPLICATION_CONTEXT",  getApplicationContext());

					RequestContextHolder.setRequestAttributes(new GrailsWebRequest(request, response, servletContext));

					template.make(model).writeTo(response.getWriter());
				} finally {
					RequestContextHolder.setRequestAttributes(requestAttributes);
				}
			}
		};
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 20;
	}
}
