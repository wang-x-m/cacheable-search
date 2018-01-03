package com.cml.learn.cacheablesearch.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import com.cml.learn.cacheablesearch.annotation.SearchCache;
import com.cml.learn.cacheablesearch.cache.ISearchCache;
import com.cml.learn.cacheablesearch.configuration.EnableSearchCacheAutoConfiguration.SearchCacheProperties;
import com.cml.learn.cacheablesearch.key.KeyGenerator;

public class CacheableSearchParamResolver implements HandlerMethodArgumentResolver, BeanFactoryAware, InitializingBean {

	private ISearchCache defaultSearchCacheManager;
	private KeyGenerator defaultKeyGenerator;
	private String defaultCacheToken = "cacheToken";

	@Autowired(required = false)
	private SearchCacheProperties defaultConfig;

	private BeanFactory beanFactory;
	private ServletModelAttributeMethodProcessor defaultResolver;

	public CacheableSearchParamResolver() {
		defaultResolver = new ServletModelAttributeMethodProcessor(true);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(SearchCache.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {

		// 获取参数的注解配置
		SearchCache cacheConfig = retrieveSearchCache(parameter);
		// 缓存的参数字段
		String cacheTokenKey = getCacheTokenKey(cacheConfig);

		String cacheToken = webRequest.getParameter(cacheTokenKey);
		ISearchCache searchCacheResolver = getSearchCache(cacheConfig);

		if (null != cacheToken) {

			Object cacheValue = searchCacheResolver.get(cacheToken);
			// 有缓存，从缓存中获取数据
			if (null != cacheValue) {
				webRequest.setAttribute(cacheTokenKey, cacheToken, NativeWebRequest.SCOPE_REQUEST);
				return cacheValue;
			}
		}

		// 缓存中没有数据,根据参数生成数据
		Object value = defaultResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

		KeyGenerator keyGenerator = getKeyGenerate(cacheConfig);

		// 生成key
		String key = keyGenerator.generateKey();
		// 添加到缓存
		searchCacheResolver.put(key, value);
		webRequest.setAttribute(cacheTokenKey, key, NativeWebRequest.SCOPE_REQUEST);
		return value;
	}

	private String getCacheTokenKey(SearchCache config) {
		return StringUtils.hasText(config.value()) ? config.value() : defaultCacheToken;
	}

	private KeyGenerator getKeyGenerate(SearchCache config) {
		if (StringUtils.hasText(config.keyGeneratorRef())) {
			return beanFactory.getBean(config.keyGeneratorRef(), KeyGenerator.class);
		}
		return defaultKeyGenerator;
	}

	private ISearchCache getSearchCache(SearchCache config) {
		if (StringUtils.hasText(config.cacheImplRef())) {
			return beanFactory.getBean(config.cacheImplRef(), ISearchCache.class);
		}
		return defaultSearchCacheManager;
	}

	private SearchCache retrieveSearchCache(MethodParameter parameter) {
		Method objMethod = parameter.getMethod();
		Annotation[][] anon = objMethod.getParameterAnnotations();

		for (int i = 0; i < anon.length; i++) {
			Annotation[] an = anon[i];
			for (Annotation ann : an) {
				if (ann.annotationType() == SearchCache.class) {
					return (SearchCache) ann;
				}
			}
		}
		return null;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (StringUtils.hasText(defaultConfig.getCacheImplRef())) {
			defaultSearchCacheManager = beanFactory.getBean(defaultConfig.getCacheImplRef(), ISearchCache.class);
		} else {
			defaultSearchCacheManager = beanFactory.getBean(EnableSearchCacheAutoConfiguration.DEFAULT_SEARCCH_CACHE_REF, ISearchCache.class);
		}

		Assert.notNull(defaultSearchCacheManager, "searchCacheManager must not be null!!!");

		if (StringUtils.hasText(defaultConfig.getKeyGeneratorRef())) {
			defaultKeyGenerator = beanFactory.getBean(defaultConfig.getKeyGeneratorRef(), KeyGenerator.class);
		} else {
			defaultKeyGenerator = beanFactory.getBean(EnableSearchCacheAutoConfiguration.DEFAULT_KEY_GENERATE_REF, KeyGenerator.class);
		}

		Assert.notNull(defaultKeyGenerator, "keyGenerator must not be null!!!");
		if (StringUtils.hasText(defaultConfig.getCacheToken())) {
			defaultCacheToken = defaultConfig.getCacheToken();
		}
	}

}