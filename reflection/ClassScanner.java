package info.pich.executor.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by dominikpich on 24/01/2017.
 */
public class ClassScanner {
    @SuppressWarnings("unchecked")
    public static final List find(String basePackage, Class type) {
        basePackage = basePackage == null ? "" : basePackage;
        type = type == null ? Object.class : type;

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(type));

        List classes = new ArrayList();
        Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
        for (BeanDefinition candidate : candidates)
            try {
                Class cls = ClassUtils.resolveClassName(candidate.getBeanClassName(),
                        ClassUtils.getDefaultClassLoader());
                classes.add((Class) cls);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        return classes;
    }

    public static final List find(String basePackage) {
        return find(basePackage, null);
    }

    public static final List find(Class type) {
        return find(null, type);
    }

    public static final List find() {
        return find(null, null);
    }
}
