package com.mola.rpc.spring.autoconfig;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mola.rpc.common.annotation.AsyncInvoke;
import com.mola.rpc.common.annotation.OnewayInvoke;
import com.mola.rpc.common.annotation.RpcConsumer;
import com.mola.rpc.common.annotation.RpcProvider;
import com.mola.rpc.common.constants.CommonConstants;
import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.common.utils.ClazzUtil;
import com.mola.rpc.spring.RpcConsumerFactoryBean;
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

    private final Set<String> consumerAlreadyAddedSet = Sets.newHashSet();
    private final Set<String> providerAlreadyAddedSet = Sets.newHashSet();

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
                    // 描述
                    providerMeta.setDescription((String) annotationAttributes.get("description"));
                    // 是否使用协程
                    providerMeta.setInFiber((Boolean) annotationAttributes.get("inFiber"));
                    // 反向模式
                    providerMeta.setReverseMode((Boolean) annotationAttributes.get("reverseMode"));
                    providerMeta.setReverseModeConsumerAddress(Lists.newArrayList((String[]) annotationAttributes.get("reverseModeConsumerAddress")));
                    BeanMetadataAttribute attribute = new BeanMetadataAttribute(CommonConstants.BEAN_DEF_PROVIDER_META, providerMeta);
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
                    if (annotation == null) {
                        continue;
                    }
                    Class<?> consumerClazzType = field.getType();
                    String clazzTypeNameKey = consumerClazzType.getName() + ":" + field.getName();
                    if (consumerAlreadyAddedSet.contains(clazzTypeNameKey)) {
                        throw new RuntimeException("duplicate consumer can not been register, key is " + clazzTypeNameKey);
                    }
                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
                    AbstractBeanDefinition consumerBeanDefinition = builder.getBeanDefinition();
                    consumerBeanDefinition.setBeanClass(RpcConsumerFactoryBean.class);
                    consumerBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(consumerClazzType);
                    consumerBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(field.getName());
                    RpcMetaData clientMeta = RpcMetaData.of(annotation.group(), annotation.version(), consumerClazzType);
                    clientMeta.setClientTimeout(annotation.timeout());
                    clientMeta.setLoadBalanceStrategy(annotation.loadBalanceStrategy());

                    // 消费者bean级别异步方法
                    Set<String> consumerBeanAsyncMethod = Sets.newHashSet(annotation.asyncMethods());
                    // 消费者类级别异步方法，优先级最高
                    consumerBeanAsyncMethod.addAll(ClazzUtil.getMethodNameFilterByAnnotation(consumerClazzType, AsyncInvoke.class));
                    clientMeta.setAsyncExecuteMethods(consumerBeanAsyncMethod);

                    clientMeta.setOnewayExecuteMethods(ClazzUtil.getMethodNameFilterByAnnotation(consumerClazzType, OnewayInvoke.class));

                    clientMeta.setReverseMode(Boolean.valueOf(annotation.reverseMode()));
                    clientMeta.setAppointedAddress(Lists.newArrayList(annotation.appointedAddress()));
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
