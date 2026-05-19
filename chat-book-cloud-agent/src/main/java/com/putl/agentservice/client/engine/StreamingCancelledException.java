package com.putl.agentservice.client.engine;

/**
 * 流式取消异常
 * <p>当AI流式响应被用户或系统取消时抛出的运行时异常</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
public class StreamingCancelledException extends RuntimeException {

    /**
     * 构造默认的流式取消异常
     */
    public StreamingCancelledException() {
        super("Streaming invocation cancelled");
    }

    /**
     * 构造带有指定消息的流式取消异常
     *
     * @param message 异常描述信息
     */
    public StreamingCancelledException(String message) {
        super(message);
    }

    /**
     * 重写fillInStackTrace方法以提高性能
     * <p>由于此异常通常用于控制流程而非真正的错误，因此不填充堆栈跟踪信息</p>
     *
     * @return 当前异常实例
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
