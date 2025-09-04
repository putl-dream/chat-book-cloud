package fun.amireux.chat.book.framework.excel.listener;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.function.BiFunction;

@Slf4j
public class GenericMergeStrategy<T> extends AbstractMergeStrategy {
    protected final List<T> dataList;
    protected final int[] mergeColumnIndexes;
    protected final BiFunction<T, Integer, String> valueExtractor;
    // 添加一个标记，确保合并操作只执行一次
    private final Set<Integer> processedSheets = new HashSet<>();

    public GenericMergeStrategy(List<T> dataList, int[] mergeColumnIndexes, BiFunction<T, Integer, String> valueExtractor) {
        this.dataList = dataList;
        this.mergeColumnIndexes = mergeColumnIndexes;
        this.valueExtractor = valueExtractor;
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        // 使用sheet的hashCode作为唯一标识，确保每个sheet只处理一次
        int sheetId = System.identityHashCode(sheet);

        // 数据第一行的 rowIndex 是 4（标题占 1 行，headRowNumber=1）
        // 只在处理第一行第一个单元格时执行合并操作
        if (relativeRowIndex != null && relativeRowIndex == 4 && cell.getColumnIndex() == 0) {
            if (!processedSheets.contains(sheetId)) {
                processedSheets.add(sheetId);
                mergeCells(sheet);
            }
        }
    }

    private void mergeCells(Sheet sheet) {
        if (dataList == null || dataList.isEmpty() || mergeColumnIndexes == null || mergeColumnIndexes.length == 0) {
            log.warn("数据为空或合并列为空，跳过合并操作");
            return;
        }

        log.info("开始合并单元格，共 {} 行数据，合并列：{}", dataList.size(), Arrays.toString(mergeColumnIndexes));

        for (int columnIndex : mergeColumnIndexes) {
            mergeColumn(sheet, columnIndex);
        }
    }

    private void mergeColumn(Sheet sheet, int columnIndex) {
        if (dataList.isEmpty()) return;

        int startRow = 4;
        String currentValue = null;

        for (int i = 0; i < dataList.size(); i++) {
            T item = dataList.get(i);
            String cellValue = valueExtractor.apply(item, columnIndex);
            int currentRow = i + 4;

            if (currentValue == null) {
                currentValue = cellValue;
                startRow = currentRow;
            } else if (!isEqual(currentValue, cellValue)) {
                if (currentRow - startRow > 1) {
                    CellRangeAddress region = new CellRangeAddress(startRow, currentRow - 1, columnIndex, columnIndex);
                    if (isRegionValid(sheet, region)) {
                        sheet.addMergedRegion(region);
                        log.debug("合并区域: {}", region);
                    } else {
//                        log.warn("合并区域冲突，跳过: {}", region);
                    }
                }
                currentValue = cellValue;
                startRow = currentRow;
            }
        }

        // 处理最后一组
        int lastRowIndex = dataList.size() - 1;
        int lastDataRow = lastRowIndex + 4;
        if (lastDataRow > startRow) {
            CellRangeAddress region = new CellRangeAddress(startRow, lastDataRow, columnIndex, columnIndex);
            if (isRegionValid(sheet, region)) {
                sheet.addMergedRegion(region);
                log.debug("合并最后一组: {}", region);
            } else {
                log.warn("最后一组合并冲突，跳过: {}", region);
            }
        }
        log.info("合并完成，共 {} 行数据，合并列：{}", dataList.size(), columnIndex);
    }

    private boolean isEqual(String value1, String value2) {
        return Objects.equals(value1, value2); // null 安全
    }

    private boolean isRegionValid(Sheet sheet, CellRangeAddress newRegion) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress existingRegion = sheet.getMergedRegion(i);
            if (newRegion.intersects(existingRegion)) {
                return false;
            }
        }
        return true;
    }
}
