package dependency_injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
//@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Inject {
}

/*
Inject 用户自定义注解，题目要求：
ElementType.FIELD: 只负责注入用户自定义Class，都在testClass中出现
ElementType.CONSTRUCTOR: 我们确保每个类都只有一个constructor，带有@Inject注解，且为需要构造实例的constructor，或者有个default constructor
 */
