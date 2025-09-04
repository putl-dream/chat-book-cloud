package fun.amireux.chat.book.framework.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractListener<T> implements ReadListener<T> {
    // 每次处理100条数据
    protected static final int BATCH_COUNT = 100;
    // 存储所有解析到的数据
    protected List<T> allDataList = ListUtils.newArrayList();

    @Override
    public void invoke(T data, AnalysisContext analysisContext) {
        // 处理合并单元格情况
        handleMergeCells(data);

        // 添加到总数据列表
        allDataList.add(data);

        // 更新最后非空数据
        updateLastNonEmptyData(data);

        // 批量处理（如果需要）
        if (allDataList.size() % BATCH_COUNT == 0) {
            log.info("已解析 {} 条数据", allDataList.size());
        }
    }

    public List<T> getCachedDataList() {
        return allDataList;
    }

    protected abstract void handleMergeCells(T data);

    protected abstract void updateLastNonEmptyData(T data);

    /**
     * 判断字符串是否为空
     *
     * @param str 待判断字符串
     * @return 是否为空
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成！总共解析到 {} 条数据", allDataList.size());
    }

    public void reset() {
        allDataList.clear();
    }
}
