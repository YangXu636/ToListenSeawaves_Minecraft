package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class XyTools {

    public static <T> T GetMemberRandom(List<T> elements){
        return GetMemberWithProbability(elements, new ArrayList<>(Collections.nCopies(elements.size(), 1f)));
    }

    public static <T> T GetMemberWithProbability(List<T> elements, List<Float> probability){
        WeightedRandomPicker<T> wrp = new WeightedRandomPicker<>();
        return wrp.pick(elements, probability.stream().map(Float::doubleValue).toList());
    }

    public static class Reflection {
        public static <T, S> S GetField (Class<T> cls, T instance, String name) throws NoSuchFieldException, IllegalAccessException {
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            Object value = field.get(instance);
            return (S)value;
        }

        public static <T, S> void SetField (Class<T> cls, T instance, S newValue, String name) throws NoSuchFieldException, IllegalAccessException {
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            field.set(instance, newValue);
        }

        public static <T> Object RunMethod (Class<T> cls, T instance, String name, List<?> args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method methodWithArgs = cls.getMethod(name, args.stream().map(Object::getClass).toArray(Class<?>[]::new));
            return methodWithArgs.invoke(instance, args.toArray());
        }
    }

    public static class WeightedRandomPicker<T> {
        // 封装元素和对应的权重
        public record WeightedElement<T>(T element, double weight) {
            public WeightedElement {
                if (weight < 0) {
                    throw new IllegalArgumentException("权重/概率不能为负数：" + weight);
                }
            }
        }

        private T pickInternal(List<WeightedElement<T>> weightedElements) {
            if (weightedElements == null || weightedElements.isEmpty()) {
                throw new IllegalArgumentException("加权元素列表不能为空");
            }
            double totalWeight = 0.0;
            for (WeightedElement<T> we : weightedElements) {
                totalWeight += we.weight();
            }
            if (totalWeight <= 0) {
                throw new IllegalArgumentException("所有元素的总权重必须大于0");
            }
            double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
            double cumulativeWeight = 0.0;
            for (WeightedElement<T> we : weightedElements) {
                cumulativeWeight += we.weight();
                if (cumulativeWeight > randomValue) {
                    return we.element();
                }
            }
            return weightedElements.getFirst().element();
        }

        /**
         * 传入元素列表 + 概率/权重列表，自动封装并抽取
         * @param elements 元素列表（非空）
         * @param weights  概率/权重列表（非空，长度与元素列表一致，值非负）
         * @return 抽中的元素
         */
        public T pick(List<T> elements, List<Double> weights) {
            // 校验参数合法性
            if (elements == null || elements.isEmpty()) {
                throw new IllegalArgumentException("元素列表不能为空");
            }
            if (weights == null || weights.isEmpty()) {
                throw new IllegalArgumentException("权重/概率列表不能为空");
            }
            if (elements.size() != weights.size()) {
                throw new IllegalArgumentException("元素列表长度（" + elements.size() + "）与权重列表长度（" + weights.size() + "）不一致");
            }
            List<WeightedElement<T>> weightedElements = new ArrayList<>();
            for (int i = 0; i < elements.size(); i++) {
                weightedElements.add(new WeightedElement<>(elements.get(i), weights.get(i)));
            }
            return pickInternal(weightedElements);
        }

        /**
         * 传入元素-权重Map（字典），自动封装并抽取
         * @param elementWeightMap 键：元素，值：权重/概率（非空，值非负）
         * @return 抽中的元素
         */
        public T pick(Map<T, Double> elementWeightMap) {
            if (elementWeightMap == null || elementWeightMap.isEmpty()) {
                throw new IllegalArgumentException("元素-权重Map不能为空");
            }
            List<WeightedElement<T>> weightedElements = new ArrayList<>();
            for (Map.Entry<T, Double> entry : elementWeightMap.entrySet()) {
                weightedElements.add(new WeightedElement<>(entry.getKey(), entry.getValue()));
            }
            return pickInternal(weightedElements);
        }

        /**
         * 兼容原始调用方式（手动传入WeightedElement列表）
         * @param weightedElements 加权元素列表
         * @return 抽中的元素
         */
        public T pick(List<WeightedElement<T>> weightedElements) {
            return pickInternal(weightedElements);
        }
    }
}
