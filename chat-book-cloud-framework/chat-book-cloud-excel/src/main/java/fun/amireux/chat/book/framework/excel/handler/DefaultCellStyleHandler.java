package fun.amireux.chat.book.framework.excel.handler;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public class DefaultCellStyleHandler implements CellWriteHandler {
 
    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        // 获取单元格
        Cell cell = context.getCell();
        CellStyle cellStyle = cell.getCellStyle();
 
        // 设置水平和垂直居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
 
        // 设置样式回到单元格
        cell.setCellStyle(cellStyle);
    }
}