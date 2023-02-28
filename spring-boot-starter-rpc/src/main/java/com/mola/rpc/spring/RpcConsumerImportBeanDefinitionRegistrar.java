package com.mola.rpc.spring;

import com.google.common.collect.Sets;
import com.mola.rpc.common.annotation.RpcConsumer;
import com.mola.rpc.common.annotation.RpcProvider;
import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.entity.RpcMetaData;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 19:10
 **/
public class RpcConsumerImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private Set<String> consumerAlreadyAddedSet = Sets.newHashSet();
    private Set<String> providerAlreadyAddedSet = Sets.newHashSet();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        try {
            for (String beanDefinitionName : beanDefinitionRegistry.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanDefinitionName);
                if (!(beanDefinition instanceof ScannedGenericBeanDefinition)) {
                    continue;
                }
                ScannedGenericBeanDefinition scannedGenericBeanDefinition = (ScannedGenericBeanDefinition) beanDefinition;
                AnnotationMetadata metadata = scannedGenericBeanDefinition.getMetadata();
                if (metadata.getAnnotationTypes().contains(RpcProvider.class.getName())) {
                    Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(RpcProvider.class.getName());
                    RpcMetaData providerMeta = RpcMetaData.of((String) annotationAttributes.get("group"),
                            (String) annotationAttributes.get("version"),
                            (Class<?>) annotationAttributes.get("interfaceClazz"));
                    providerMeta.setInFiber((Boolean) annotationAttributes.get("inFiber"));
                    BeanMetadataAttribute attribute = new BeanMetadataAttribute(CommonConstants.BEAN_DEF_PROVIDER_META,providerMeta);
                    scannedGenericBeanDefinition.addMetadataAttribute(attribute);
                    String providerKey = providerMeta.getInterfaceClazz().getName() + ":" + providerMeta.getGroup() + ":" + providerMeta.getVersion();
                    if (providerAlreadyAddedSet.contains(providerKey)) {
                        throw new RuntimeException("duplicate provider can not been register, key is " + providerKey);
                    }
                    providerAlreadyAddedSet.add(providerKey);
                }
                String beanClassName = beanDefinition.getBeanClassName();
                Class<?> clazz = Class.forName(beanClassName);
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    RpcConsumer annotation = field.getAnnotation(RpcConsumer.class);
                    if (null == annotation) {
                        continue;
                    }
                    Class<?> type = field.getType();
                    String clazzTypeNameKey = type.getName() + ":" + field.getName();
                    if (consumerAlreadyAddedSet.contains(clazzTypeNameKey)) {
                        throw new RuntimeException("duplicate consumer can not been register, key is " + clazzTypeNameKey);
                    }
                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
                    AbstractBeanDefinition consumerBeanDefinition = builder.getBeanDefinition();
                    consumerBeanDefinition.setBeanClass(RpcConsumerFactoryBean.class);
                    consumerBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(type);
                    consumerBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(field.getName());
                    RpcMetaData clientMeta = RpcMetaData.of(annotation.group(), annotation.version(), type);
                    clientMeta.setClientTimeout(annotation.timeout());
                    clientMeta.setLoadBalanceStrategy(annotation.loadBalanceStrategy());
                    clientMeta.setAsyncExecuteMethods(Sets.newHashSet(annotation.asyncMethods()));
                    BeanMetadataAttribute attribute = new BeanMetadataAttribute(CommonConstants.BEAN_DEF_CONSUMER_META,
                            clientMeta);
                    consumerBeanDefinition.addMetadataAttribute(attribute);
                    // 添加beanDefinition
                    beanDefinitionRegistry.registerBeanDefinition(field.getName(), consumerBeanDefinition);
                    consumerAlreadyAddedSet.add(clazzTypeNameKey);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
