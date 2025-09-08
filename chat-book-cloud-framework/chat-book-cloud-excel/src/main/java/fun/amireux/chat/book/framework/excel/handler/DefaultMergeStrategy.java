package fun.amireux.chat.book.framework.excel.handler;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;
import java.util.function.BiFunction;

/**
 * 默认纵向合并策略：
 * - 支持多列合并，所有列按同一规则保持一致
 * - 自动跳过空数据
 * - 避免跨列冲突（不会产生交叉区域）
 */
@Slf4j
public class DefaultMergeStrategy<T> extends AbstractMergeStrategy {

    private final List<T> dataList;
    private final int[] mergeColumnIndexes;
    private final int headRowNumber;
    private final BiFunction<T, Integer, String> valueExtractor;
    private final Set<Integer> processedSheets = new HashSet<>();

    public DefaultMergeStrategy(List<T> dataList, int[] mergeColumnIndexes, BiFunction<T, Integer, String> valueExtractor, int headRowNumber) {
        this.dataList = Optional.ofNullable(dataList).orElse(Collections.emptyList());
        this.mergeColumnIndexes = Optional.ofNullable(mergeColumnIndexes).orElse(new int[0]);
        this.valueExtractor = Objects.requireNonNull(valueExtractor, "valueExtractor must not be null");
        this.headRowNumber = headRowNumber;
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        int sheetId = System.identityHashCode(sheet);
        if (relativeRowIndex != null && relativeRowIndex == headRowNumber && cell.getColumnIndex() == 0) {
            if (processedSheets.add(sheetId)) { // 只添加一次
                mergeCells(sheet);
            }
        }
    }

    private void mergeCells(Sheet sheet) {
        if (dataList.isEmpty() || mergeColumnIndexes.length == 0) {
            log.warn("数据为空或合并列为空，跳过合并操作");
            return;
        }

        log.info("开始合并单元格，合并列：{}", Arrays.toString(mergeColumnIndexes));

        for (int columnIndex : mergeColumnIndexes) {
            mergeColumn(sheet, columnIndex);
        }

        log.info("合并完成，共 {} 行数据", dataList.size());
    }

    private void mergeColumn(Sheet sheet, int columnIndex) {
        int startRow = headRowNumber;
        String currentValue = null;

        for (int i = 0; i <= dataList.size(); i++) {
            String cellValue = i < dataList.size() ? valueExtractor.apply(dataList.get(i), columnIndex) : null;
            int currentRow = i + headRowNumber;

            if (!Objects.equals(currentValue, cellValue)) {
                if (currentValue != null && currentRow - startRow > 1) {
                    CellRangeAddress region = new CellRangeAddress(startRow, currentRow - 1, columnIndex, columnIndex);
                    if (isRegionValid(sheet, region)) {
                        sheet.addMergedRegion(region);
                        log.debug("合并区域: {}", region);
                    } else {
                        log.warn("合并区域冲突，跳过: {}", region);
                    }
                }
                currentValue = cellValue;
                startRow = currentRow;
            }
        }

        log.info("列 {} 合并完成", columnIndex);
    }

    private boolean isRegionValid(Sheet sheet, CellRangeAddress newRegion) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            if (newRegion.intersects(sheet.getMergedRegion(i))) {
                return false;
            }
        }
        return true;
    }
}