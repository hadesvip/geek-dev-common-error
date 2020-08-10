package com.kevin.geek.dev.common.error.concurrent;

import com.kevin.geek.dev.common.error.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * threadLocal 问题
 *
 * @author wangyong
 */
public class ThreadLocalRunner {

    /**
     * user 存储
     */
    private static final ThreadLocal<User> CURRENT_USER_THREAD_LOCAL = ThreadLocal.withInitial(() -> null);

    private static Set<String> userIdSet = new HashSet<>();

    /**
     * invoke
     *
     * @param userId 用户ID
     */
    private static void query(String userId) {
        //设置用户信息之前先查询一次ThreadLocal中的用户信息
        String before = Thread.currentThread().getName() + ":" + CURRENT_USER_THREAD_LOCAL.get();

        //设置用户信息到ThreadLocal
        User user = new User(userId,
                Thread.currentThread().getName(),
                UUID.randomUUID().toString().replaceAll("-", ""));
        CURRENT_USER_THREAD_LOCAL.set(user);

        //设置用户信息之后再查询一次ThreadLocal中的用户信息
        String after = Thread.currentThread().getName() + ":" + CURRENT_USER_THREAD_LOCAL.get();

        //汇总输出两次查询结果
        System.out.println("===================");
        System.out.println(before);
        System.out.println(after);
    }


    /**
     * 正常多线程请求
     * <p>
     */
    @Test
    public void threadTest() {
        userIdSet.forEach(userId -> new Thread(() -> query(userId)).start());
        LockSupport.park();
    }


    /**
     * 线程池的情况
     * TODO 线程池不回收导致threadLocal线程数据错乱
     */
    @Test
    public void threadPoolTest() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        userIdSet.forEach(userId -> executorService.execute(() -> query(userId)));
        executorService.shutdown();
        LockSupport.park();
    }


    @BeforeAll
    static void initData() {
//        ConcurrentHashMap<String, Long> dataMap = LongStream
//                .rangeClosed(1, 10)
//                .boxed()
//                .collect(Collectors
//                        .toConcurrentMap(i -> UUID.randomUUID().toString().replaceAll("-", ""),
//                                Function.identity(),
//                                (o1, o2) -> o1, ConcurrentHashMap::new));
//        dataMap.forEach((k, v) -> System.out.println("key:" + k + ",value:" + v));


        userIdSet = LongStream
                .rangeClosed(1, 3)
                .boxed()
                .map(i -> UUID.randomUUID().toString().replaceAll("-", ""))
                .collect(Collectors.toSet());

//        userIdSet.forEach(System.out::println);

    }
}
