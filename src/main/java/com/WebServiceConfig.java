package com;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
	@Bean
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		return new ServletRegistrationBean(servlet, "/service/*");
	}

	@Bean(name = "GetGeneralInfo")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema GenerallnfoSchema) {
		DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
		wsdl11Definition.setPortTypeName("GetGeneralInfoPort");
		wsdl11Definition.setLocationUri("https://tmsthws10.com/GeneralInfoPROD/service");
		wsdl11Definition.setTargetNamespace("GetGeneralInfo");
		wsdl11Definition.setSchema(GenerallnfoSchema);
		return wsdl11Definition;
	}

	@Bean
	public XsdSchema GenerallnfoSchema() {
		return new SimpleXsdSchema(new ClassPathResource("GeneralInfo.xsd"));
	}

}
