package dependency_injection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * TODO you should complete the class
 */
public class BeanFactoryImpl implements BeanFactory {

    private Properties injectProperties;
    private Properties valueProperties;
    @Override
    public void loadInjectProperties(File file) {
        injectProperties= new Properties();
        try{
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            injectProperties.load(in);
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void loadValueProperties(File file) {
        valueProperties= new Properties();
        try{
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            valueProperties.load(in);
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*
    找实现类
    1.找到 implClazzName
    2.Class.forName(implClazzName) 获取Class类型 implClazz
    确定构造方法
    1.通过implClazz找所有的构造方法，带有@Inject的构造方法
    或 default构造方法（递归的终止条件） getDeclaredConstructors().newInstance();
    构造对象：
    1.找构造方法里所有的Parameter
        如果没有@Value，肯定是用户自定义类 递归 调用 newInstance(classtype)
        如果有@Value:
        boolean,int,String
        boolean[],int[],String[]
        List<?>,Set<?>,Map<?,?>:Boolean,Int,String
        确定数据类型，用过配置文件创建实例 local-value.properties
    2.对于每个Parameter 分别注入值，放在一个Object[]里
    3.调用构造方法构建实例：
     Object[] objects2 = {bObject, cObject, parameterObject1, parameterObject2};
     AA aObject2 = (AA) constructor.newInstance(objects2);
    在已有对象中注入属性：
    1.找当前类里所有属性， getDeclaredFields()
    2.在属性中找所有带有@Value注解的属性
        boolean,int,String
        boolean[],int[],String[]
        List<?>,Set<?>,Map<?,?>:Boolean,Int,String

         listField.setAccessible(true);
         listField.set(aObject, fieldObj);
         listField.setAccessible(false);
    3.在属性中带有@inject 注解的属性，递归newInstance()
    */
    @Override
    public <T> T createInstance(Class<T> clazz) {
        String clazzName = clazz.getName();
        String implClazzName;
        Class<?> implClazz=null;
        if (injectProperties.containsKey(clazzName)){
            implClazzName = injectProperties.getProperty(clazzName);
            try {
                implClazz = Class.forName(implClazzName);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            try {
                implClazz = Class.forName(clazzName);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Constructor<?> constructor = null;
        assert implClazz != null;
        for (Constructor<?> c : implClazz.getDeclaredConstructors()) {
            if (c.getAnnotation(Inject.class) != null) {
                constructor = c;
                break;
            }
        }
        if (constructor == null){
            constructor = implClazz.getDeclaredConstructors()[0];
        }
        Parameter[] parameters = constructor.getParameters();
        Object[] objects = new Object[constructor.getParameterCount()];
        for (int i= 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            if (p.getAnnotation(Value.class) != null){
                Value valueAnnotation = p.getAnnotation(Value.class);
                String annotation = valueAnnotation.value();
                if (valueProperties.containsKey(annotation)){
                    annotation = valueProperties.getProperty(annotation);
                }
                String[] values = annotation.split(valueAnnotation.delimiter());
                if (p.getType()==int.class){
                    for(String value : values){
                        try{
                            int integer = Integer.parseInt(value);
                            objects[i] = integer;
                            break;
                        }catch (Exception e){
//                                e.printStackTrace();
                        }
                    }
                }
                if (p.getType()==boolean.class){
                    for(String value : values){
                        try{
                            boolean bool = Boolean.parseBoolean(value);
                            objects[i] = bool;
                            break;
                        }catch (Exception e){
//                                e.printStackTrace();
                        }
                    }
                }
                if (p.getType()==String.class){
                    objects[i] = values;
                }
                if (p.getType()==int[].class){
                    int[] ints = new int[values.length];
                    for (int j = 0; j < values.length; j++) {
                        ints[j] = Integer.parseInt(values[j]);
                    }
                    objects[i] = ints;
                }if (p.getType()==boolean[].class){
                    boolean[] booleans = new boolean[values.length];
                    for (int j = 0; j < values.length; j++) {
                        booleans[j] = Boolean.parseBoolean(values[j]);
                    }
                    objects[i] = booleans;
                }if (p.getType()==String[].class){
                    objects[i] = values;
                }if (p.getType()==List.class){
                    List<Object> list = new ArrayList<>();
                    for (String value : values) {
                        if (valueProperties.containsKey(value)){
                            value = valueProperties.getProperty(value);
                        }
                        if (value.equals("true")||value.equals("false")){
                            list.add(Boolean.parseBoolean(value));
                        }else if (value.matches("[0-9]+")){
                            list.add(Integer.parseInt(value));
                        }else {
                            list.add(value);
                        }
                    }
                    objects[i] = list;
                }if (p.getType()==Set.class){
                    Set<Object> set = new HashSet<>();
                    for (String value : values) {
                        if (valueProperties.containsKey(value)){
                            value = valueProperties.getProperty(value);
                        }
                        if (value.equals("true")||value.equals("false")){
                            set.add(Boolean.parseBoolean(value));
                        }else if (value.matches("[0-9]+")){
                            set.add(Integer.parseInt(value));
                        }else {
                            set.add(value);
                        }
                    }
                    objects[i] = set;
                }if (p.getType()==Map.class){
                    Map<Object,Object> map = new HashMap<>();
                    for (String value : values) {
                        if (valueProperties.containsKey(value)){
                            value = valueProperties.getProperty(value);
                        }
                        String[] kv = value.split(":");
                        if (kv[1].equals("true")||kv[1].equals("false")){
                            map.put(kv[0],Boolean.parseBoolean(kv[1]));
                        }else if (kv[1].matches("[0-9]+")){
                            map.put(kv[0],Integer.parseInt(kv[1]));
                        }else {
                            map.put(kv[0],kv[1]);
                        }
                    }
                    objects[i] = map;
                }
            }else {
                objects[i] = createInstance(p.getType());
            }
        }
        T object = null;
        try {
            object = (T) constructor.newInstance(objects);
        }catch (Exception e){
            e.printStackTrace();
        }
        Field[] fields = implClazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Value.class) != null) {
                Value valueAnnotation = field.getAnnotation(Value.class);
                String annotation = valueAnnotation.value();
                if (valueProperties.containsKey(annotation)){
                    annotation = valueProperties.getProperty(annotation);
                }
                String[] values = annotation.split(valueAnnotation.delimiter());
                if (field.getType() == int.class) {
                    try {
                        field.setAccessible(true);
                        for(String value : values){
                            try{
                                int integer = Integer.parseInt(value);
                                field.set(object, integer);
                                break;
                            }catch (Exception e){
//                                e.printStackTrace();
                            }
                        }
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType() == boolean.class) {
                    try {
                        field.setAccessible(true);
                        for(String value : values){
                            try{
                                boolean bool = Boolean.parseBoolean(value);
                                field.set(object, bool);
                                break;
                            }catch (Exception e){
//                                e.printStackTrace();
                            }
                        }
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType() == String.class) {
                    try {
                        field.setAccessible(true);
                        field.set(object, values[0]);
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType() == int[].class) {
                    int[] ints = new int[values.length];
                    for (int j = 0; j < values.length; j++) {
                        ints[j] = Integer.parseInt(values[j]);
                    }
                    try {
                        field.setAccessible(true);
                        field.set(object, ints);
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType() == boolean[].class) {
                    boolean[] booleans = new boolean[values.length];
                    for (int j = 0; j < values.length; j++) {
                        booleans[j] = Boolean.parseBoolean(values[j]);
                    }
                    try {
                        field.setAccessible(true);
                        field.set(object, booleans);
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType() == String[].class) {
                    try {
                        field.setAccessible(true);
                        field.set(object, values);
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType() == List.class) {
                    List<Object> list = new ArrayList<>();
                    for (String value : values) {
                        if (valueProperties.containsKey(value)) {
                            value = valueProperties.getProperty(value);
                        }
                        if (value.equals("true") || value.equals("false")) {
                            list.add(Boolean.parseBoolean(value));
                        } else if (value.matches("[0-9]+")) {
                            list.add(Integer.parseInt(value));
                        } else {
                            list.add(value);
                        }
                    }
                    try {
                        field.setAccessible(true);
                        field.set(object, list);
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType() == Set.class) {
                    Set<Object> set = new HashSet<>();
                    for (String value : values) {
                        if (valueProperties.containsKey(value)) {
                            value = valueProperties.getProperty(value);
                        }
                        if (value.equals("true") || value.equals("false")) {
                            set.add(Boolean.parseBoolean(value));
                        } else if (value.matches("[0-9]+")) {
                            set.add(Integer.parseInt(value));
                        } else {
                            set.add(value);
                        }
                    }
                    try {
                        field.setAccessible(true);
                        field.set(object, set);
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType() == Map.class) {
                    Map<Object, Object> map = new HashMap<>();
                    for (String value : values) {
                        if (valueProperties.containsKey(value)) {
                            value = valueProperties.getProperty(value);
                        }
                        String[] kv = value.split(":");
                        if (kv[1].equals("true") || kv[1].equals("false")) {
                            map.put(kv[0], Boolean.parseBoolean(kv[1]));
                        } else if (kv[1].matches("[0-9]+")) {
                            map.put(kv[0], Integer.parseInt(kv[1]));
                        } else {
                            map.put(kv[0], kv[1]);
                        }
                    }
                    try {
                        field.setAccessible(true);
                        field.set(object, map);
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return object;
    }
}