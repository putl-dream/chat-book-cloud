package com.putl.agentservice.client.engine;

/**
 * 流式控制接口
 * <p>用于管理AI流式响应的生命周期，支持取消操作和状态检查</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
public interface StreamingControl {

    /**
     * 检查流式响应是否已被取消
     *
     * @return true表示已取消，false表示未取消
     */
    boolean isCancelled();

    /**
     * 注册取消操作的回调函数
     *
     * @param action 当流式响应被取消时执行的操作
     */
    void onCancel(Runnable action);

    /**
     * 如果流式响应已取消，则抛出异常
     *
     * @throws StreamingCancelledException 当流式响应被取消时抛出
     */
    default void throwIfCancelled() {
        if (isCancelled()) {
            throw new StreamingCancelledException();
        }
    }

    /**
     * 创建一个无操作的流式控制实例
     *
     * @return 不执行任何操作的流式控制对象
     */
    static StreamingControl noop() {
        return NoopHolder.INSTANCE;
    }

    /**
     * 无操作流式控制的持有者类
     * <p>使用静态内部类实现单例模式，提供默认的无操作实现</p>
     */
    final class NoopHolder {
        private static final StreamingControl INSTANCE = new StreamingControl() {
            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public void onCancel(Runnable action) {
                // no-op
            }
        };

        private NoopHolder() {
        }
    }
}
