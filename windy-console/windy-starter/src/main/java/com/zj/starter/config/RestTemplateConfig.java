package com.zj.starter.config;

import com.zj.contract.json.WireJson;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate getTemplate(List<ClientHttpRequestInterceptor> clients) {
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setReadTimeout(10000);
    requestFactory.setConnectTimeout(5000);

    //这种写法既可以指定超时时间也可以重复读取response响应数据
    InterceptingClientHttpRequestFactory interceptorFactory =
        new InterceptingClientHttpRequestFactory(
            new BufferingClientHttpRequestFactory(requestFactory), clients);

    RestTemplate restTemplate = new RestTemplate(interceptorFactory);

    List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
    Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
    while (iterator.hasNext()) {
      HttpMessageConverter<?> converter = iterator.next();
      //原有的String是ISO-8859-1编码 去掉
      if (converter instanceof StringHttpMessageConverter) {
        iterator.remove();
      }
      //由于系统中默认有jackson 在转换json时自动会启用  但是我们不想使用它 可以直接移除
      if (converter instanceof GsonHttpMessageConverter
          || converter instanceof MappingJackson2HttpMessageConverter) {
        iterator.remove();
      }
    }
    messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    //出站统一走契约层的 Jackson 配置，与入站（Spring MVC 默认 Jackson）保持同一套语义
    MappingJackson2HttpMessageConverter jacksonConverter =
        new MappingJackson2HttpMessageConverter(WireJson.mapper());
    jacksonConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
    messageConverters.add(jacksonConverter);

    restTemplate.getMessageConverters()
        .add(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    log.info("load template config done");
    return restTemplate;
  }
}
