/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mola.rpc.spring.postprocessor;

import com.mola.rpc.common.annotation.RpcConsumer;
import com.mola.rpc.common.utils.AssertUtil;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.jndi.support.SimpleJndiBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 19:10
 **/
@SuppressWarnings("serial")
public class RpcConsumerInjectBeanProcessor extends InitDestroyAnnotationBeanPostProcessor
        implements InstantiationAwareBeanPostProcessor, BeanFactoryAware, Serializable {

    @Nullable
    private static Class<? extends Annotation> webServiceRefClass;

    @Nullable
    private static Class<? extends Annotation> ejbRefClass;

    private final Set<String> ignoredResourceTypes = new HashSet<>(1);

    private boolean fallbackToDefaultTypeMatch = true;

    private boolean alwaysUseJndiLookup = false;

    private transient BeanFactory jndiFactory = new SimpleJndiBeanFactory();

    @Nullable
    private transient BeanFactory resourceFactory;

    @Nullable
    private transient BeanFactory beanFactory;

    @Nullable
    private transient StringValueResolver embeddedValueResolver;

    private final transient Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);


    /**
     * Create a new CommonAnnotationBeanPostProcessor,
     * with the init and destroy annotation types set to
     * {@link javax.annotation.PostConstruct} and {@link javax.annotation.PreDestroy},
     * respectively.
     */
    public RpcConsumerInjectBeanProcessor() {
        setOrder(Ordered.LOWEST_PRECEDENCE - 3);
        setInitAnnotationType(PostConstruct.class);
        setDestroyAnnotationType(PreDestroy.class);
        ignoreResourceType("javax.xml.ws.WebServiceContext");
    }


    /**
     * Ignore the given resource type when resolving {@code @Resource}
     * annotations.
     * <p>By default, the {@code javax.xml.ws.WebServiceContext} interface
     * will be ignored, since it will be resolved by the JAX-WS runtime.
     * @param resourceType the resource type to ignore
     */
    public void ignoreResourceType(String resourceType) {
        AssertUtil.notNull(resourceType, "Ignored resource type must not be null");
        this.ignoredResourceTypes.add(resourceType);
    }

    /**
     * Set whether to allow a fallback to a type match if no explicit name has been
     * specified. The default name (i.e. the field name or bean property name) will
     * still be checked first; if a bean of that name exists, it will be taken.
     * However, if no bean of that name exists, a by-type resolution of the
     * dependency will be attempted if this flag is "true".
     * <p>Default is "true". Switch this flag to "false" in order to enforce a
     * by-name lookup in all cases, throwing an exception in case of no name match.
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency
     */
    public void setFallbackToDefaultTypeMatch(boolean fallbackToDefaultTypeMatch) {
        this.fallbackToDefaultTypeMatch = fallbackToDefaultTypeMatch;
    }

    /**
     * Set whether to always use JNDI lookups equivalent to standard Java EE 5 resource
     * injection, <b>even for {@code name} attributes and default names</b>.
     * <p>Default is "false": Resource names are used for Spring bean lookups in the
     * containing BeanFactory; only {@code mappedName} attributes point directly
     * into JNDI. Switch this flag to "true" for enforcing Java EE style JNDI lookups
     * in any case, even for {@code name} attributes and default names.
     * @see #setJndiFactory
     * @see #setResourceFactory
     */
    public void setAlwaysUseJndiLookup(boolean alwaysUseJndiLookup) {
        this.alwaysUseJndiLookup = alwaysUseJndiLookup;
    }

    /**
     * Specify the factory for objects to be injected into {@code @Resource} /
     * {@code @WebServiceRef} / {@code @EJB} annotated fields and setter methods,
     * <b>for {@code mappedName} attributes that point directly into JNDI</b>.
     * This factory will also be used if "alwaysUseJndiLookup" is set to "true" in order
     * to enforce JNDI lookups even for {@code name} attributes and default names.
     * <p>The default is a {@link org.springframework.jndi.support.SimpleJndiBeanFactory}
     * for JNDI lookup behavior equivalent to standard Java EE 5 resource injection.
     * @see #setResourceFactory
     * @see #setAlwaysUseJndiLookup
     */
    public void setJndiFactory(BeanFactory jndiFactory) {
        AssertUtil.notNull(jndiFactory, "BeanFactory must not be null");
        this.jndiFactory = jndiFactory;
    }

    /**
     * Specify the factory for objects to be injected into {@code @Resource} /
     * {@code @WebServiceRef} / {@code @EJB} annotated fields and setter methods,
     * <b>for {@code name} attributes and default names</b>.
     * <p>The default is the BeanFactory that this post-processor is defined in,
     * if any, looking up resource names as Spring bean names. Specify the resource
     * factory explicitly for programmatic usage of this post-processor.
     * <p>Specifying Spring's {@link org.springframework.jndi.support.SimpleJndiBeanFactory}
     * leads to JNDI lookup behavior equivalent to standard Java EE 5 resource injection,
     * even for {@code name} attributes and default names. This is the same behavior
     * that the "alwaysUseJndiLookup" flag enables.
     * @see #setAlwaysUseJndiLookup
     */
    public void setResourceFactory(BeanFactory resourceFactory) {
        AssertUtil.notNull(resourceFactory, "BeanFactory must not be null");
        this.resourceFactory = resourceFactory;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        AssertUtil.notNull(beanFactory, "BeanFactory must not be null");
        this.beanFactory = beanFactory;
        if (this.resourceFactory == null) {
            this.resourceFactory = beanFactory;
        }
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }


    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        super.postProcessMergedBeanDefinition(beanDefinition, beanType, beanName);
        InjectionMetadata metadata = findResourceMetadata(beanName, beanType, null);
        metadata.checkConfigMembers(beanDefinition);
    }

    @Override
    public void resetBeanDefinition(String beanName) {
        this.injectionMetadataCache.remove(beanName);
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) {
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        InjectionMetadata metadata = findResourceMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        }
        catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of resource dependencies failed", ex);
        }
        return pvs;
    }

    @Deprecated
    @Override
    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {

        return postProcessProperties(pvs, bean, beanName);
    }


    private InjectionMetadata findResourceMetadata(String beanName, final Class<?> clazz, @Nullable PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    metadata = buildResourceMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildResourceMetadata(final Class<?> clazz) {
        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;

        do {
            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                if (field.isAnnotationPresent(RpcConsumer.class)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        throw new IllegalStateException("@Resource annotation is not supported on static fields");
                    }
                    if (!this.ignoredResourceTypes.contains(field.getType().getName())) {
                        currElements.add(new RpcConsumerElement(field, field, null));
                    }
                }
            });

            ReflectionUtils.doWithLocalMethods(targetClass, method -> {
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                if (method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                    if (bridgedMethod.isAnnotationPresent(RpcConsumer.class)) {
                        if (Modifier.isStatic(method.getModifiers())) {
                            throw new IllegalStateException("@Resource annotation is not supported on static methods");
                        }
                        Class<?>[] paramTypes = method.getParameterTypes();
                        if (paramTypes.length != 1) {
                            throw new IllegalStateException("@Resource annotation requires a single-arg method: " + method);
                        }
                        if (!this.ignoredResourceTypes.contains(paramTypes[0].getName())) {
                            PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                            currElements.add(new RpcConsumerElement(method, bridgedMethod, pd));
                        }
                    }
                }
            });

            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return new InjectionMetadata(clazz, elements);
    }

    /**
     * Obtain a lazily resolving resource proxy for the given name and type,
     * delegating to {@link #getResource} on demand once a method call comes in.
     * @param element the descriptor for the annotated field/method
     * @param requestingBeanName the name of the requesting bean
     * @return the resource object (never {@code null})
     * @since 4.2
     * @see #getResource
     * @see Lazy
     */
    protected Object buildLazyResourceProxy(final LookupElement element, final @Nullable String requestingBeanName) {
        TargetSource ts = new TargetSource() {
            @Override
            public Class<?> getTargetClass() {
                return element.lookupType;
            }
            @Override
            public boolean isStatic() {
                return false;
            }
            @Override
            public Object getTarget() {
                return getResource(element, requestingBeanName);
            }
            @Override
            public void releaseTarget(Object target) {
            }
        };
        ProxyFactory pf = new ProxyFactory();
        pf.setTargetSource(ts);
        if (element.lookupType.isInterface()) {
            pf.addInterface(element.lookupType);
        }
        ClassLoader classLoader = (this.beanFactory instanceof ConfigurableBeanFactory ?
                ((ConfigurableBeanFactory) this.beanFactory).getBeanClassLoader() : null);
        return pf.getProxy(classLoader);
    }

    /**
     * Obtain the resource object for the given name and type.
     * @param element the descriptor for the annotated field/method
     * @param requestingBeanName the name of the requesting bean
     * @return the resource object (never {@code null})
     * @throws NoSuchBeanDefinitionException if no corresponding target resource found
     */
    protected Object getResource(LookupElement element, @Nullable String requestingBeanName)
            throws NoSuchBeanDefinitionException {

        if (StringUtils.hasLength(element.mappedName)) {
            return this.jndiFactory.getBean(element.mappedName, element.lookupType);
        }
        if (this.alwaysUseJndiLookup) {
            return this.jndiFactory.getBean(element.name, element.lookupType);
        }
        if (this.resourceFactory == null) {
            throw new NoSuchBeanDefinitionException(element.lookupType,
                    "No resource factory configured - specify the 'resourceFactory' property");
        }
        return autowireResource(this.resourceFactory, element, requestingBeanName);
    }

    /**
     * Obtain a resource object for the given name and type through autowiring
     * based on the given factory.
     * @param factory the factory to autowire against
     * @param element the descriptor for the annotated field/method
     * @param requestingBeanName the name of the requesting bean
     * @return the resource object (never {@code null})
     * @throws NoSuchBeanDefinitionException if no corresponding target resource found
     */
    protected Object autowireResource(BeanFactory factory, LookupElement element, @Nullable String requestingBeanName)
            throws NoSuchBeanDefinitionException {

        Object resource;
        Set<String> autowiredBeanNames;
        String name = element.name;

        if (factory instanceof AutowireCapableBeanFactory) {
            AutowireCapableBeanFactory beanFactory = (AutowireCapableBeanFactory) factory;
            DependencyDescriptor descriptor = element.getDependencyDescriptor();
            if (this.fallbackToDefaultTypeMatch && element.isDefaultName && !factory.containsBean(name)) {
                autowiredBeanNames = new LinkedHashSet<>();
                resource = beanFactory.resolveDependency(descriptor, requestingBeanName, autowiredBeanNames, null);
                if (resource == null) {
                    throw new NoSuchBeanDefinitionException(element.getLookupType(), "No resolvable resource object");
                }
            }
            else {
                resource = beanFactory.resolveBeanByName(name, descriptor);
                autowiredBeanNames = Collections.singleton(name);
            }
        }
        else {
            resource = factory.getBean(name, element.lookupType);
            autowiredBeanNames = Collections.singleton(name);
        }

        if (factory instanceof ConfigurableBeanFactory) {
            ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) factory;
            for (String autowiredBeanName : autowiredBeanNames) {
                if (requestingBeanName != null && beanFactory.containsBean(autowiredBeanName)) {
                    beanFactory.registerDependentBean(autowiredBeanName, requestingBeanName);
                }
            }
        }

        return resource;
    }


    /**
     * Class representing generic injection information about an annotated field
     * or setter method, supporting @Resource and related annotations.
     */
    protected abstract class LookupElement extends InjectionMetadata.InjectedElement {

        protected String name = "";

        protected boolean isDefaultName = false;

        protected Class<?> lookupType = Object.class;

        @Nullable
        protected String mappedName;

        public LookupElement(Member member, @Nullable PropertyDescriptor pd) {
            super(member, pd);
        }

        /**
         * Return the resource name for the lookup.
         */
        public final String getName() {
            return this.name;
        }

        /**
         * Return the desired type for the lookup.
         */
        public final Class<?> getLookupType() {
            return this.lookupType;
        }

        /**
         * Build a DependencyDescriptor for the underlying field/method.
         */
        public final DependencyDescriptor getDependencyDescriptor() {
            if (this.isField) {
                return new LookupDependencyDescriptor((Field) this.member, this.lookupType);
            }
            else {
                return new LookupDependencyDescriptor((Method) this.member, this.lookupType);
            }
        }
    }


    /**
     * Class representing injection information about an annotated field
     * or setter method, supporting the @Resource annotation.
     */
    private class RpcConsumerElement extends LookupElement {

        private final boolean lazyLookup;

        public RpcConsumerElement(Member member, AnnotatedElement ae, @Nullable PropertyDescriptor pd) {
            super(member, pd);
            RpcConsumer resource = ae.getAnnotation(RpcConsumer.class);
            Lazy lazy = ae.getAnnotation(Lazy.class);
            this.lazyLookup = (lazy != null && lazy.value());
            this.name = (member.getName() != null ? member.getName() : "");
        }

        @Override
        protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
            return (this.lazyLookup ? buildLazyResourceProxy(this, requestingBeanName) :
                    getResource(this, requestingBeanName));
        }
    }

    /**
     * Extension of the DependencyDescriptor class,
     * overriding the dependency type with the specified resource type.
     */
    private static class LookupDependencyDescriptor extends DependencyDescriptor {

        private final Class<?> lookupType;

        public LookupDependencyDescriptor(Field field, Class<?> lookupType) {
            super(field, true);
            this.lookupType = lookupType;
        }

        public LookupDependencyDescriptor(Method method, Class<?> lookupType) {
            super(new MethodParameter(method, 0), true);
            this.lookupType = lookupType;
        }

        @Override
        public Class<?> getDependencyType() {
            return this.lookupType;
        }
    }

}
