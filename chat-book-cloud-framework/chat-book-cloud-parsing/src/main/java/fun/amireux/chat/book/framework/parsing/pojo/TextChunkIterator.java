package fun.amireux.chat.book.framework.parsing.pojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 智能文本块迭代器：在接近目标长度处，寻找自然切分点（如句号、问号等）进行切割。
 * 如果找不到，则在目标长度处硬切分。
 */
public class TextChunkIterator implements Iterator<String>, AutoCloseable {
    private final BufferedReader reader;
    private final int targetChunkSize;
    private final Set<Character> breakPoints; // 自然切分点集合

    // 内部缓冲区和状态
    private final char[] buffer;
    private int bufferPos;
    private int charsInBuffer;
    private boolean eofReached;

    /**
     * 构造函数
     *
     * @param reader          用于读取文本的 BufferedReader
     * @param targetChunkSize 期望的块长度（字符数）
     * @param breakPoints     自然切分点字符集合（如 '。', '？', '！', '；', '，', '\n' 等）
     * @throws IllegalArgumentException 如果 targetChunkSize <= 0
     * @throws IOException              如果初始化读取时发生错误
     */
    public TextChunkIterator(BufferedReader reader, int targetChunkSize, Set<Character> breakPoints) throws IOException {
        if (targetChunkSize <= 0) {
            throw new IllegalArgumentException("Target chunk size must be positive.");
        }
        this.reader = reader;
        this.targetChunkSize = targetChunkSize;
        this.breakPoints = new HashSet<>(breakPoints); // 复制，防止外部修改

        // 缓冲区大小建议大于 targetChunkSize，以便有足够的上下文进行搜索
        this.buffer = new char[Math.max(targetChunkSize * 3, 8192)]; // 至少 8KB 或 3 倍目标大小
        this.bufferPos = 0;
        this.charsInBuffer = 0;
        this.eofReached = false;

        refillBuffer();
    }

    /**
     * 默认构造函数，使用常见的中文和英文标点作为切分点
     */
    public TextChunkIterator(BufferedReader reader, int targetChunkSize) throws IOException {
        this(reader, targetChunkSize, getDefaultBreakPoints());
    }

    /**
     * 默认构造函数
     */
    public TextChunkIterator(BufferedReader reader) throws IOException {
        this(reader, 1000);
    }

    private static Set<Character> getDefaultBreakPoints() {
        Set<Character> defaults = new HashSet<>();
        // 中文标点
        defaults.add('。');
        defaults.add('？');
        defaults.add('！');
        defaults.add('；');
        defaults.add('，');
        defaults.add('、');
        defaults.add('：');
        defaults.add('”');
        defaults.add('”');
        defaults.add('\n');
        defaults.add('\r'); // 换行符
        // 英文标点 (可选，根据文本语言决定)
        defaults.add('.');
        defaults.add('?');
        defaults.add('!');
        defaults.add(';');
        defaults.add(',');
        defaults.add(':');
        return defaults;
    }

    private void refillBuffer() throws IOException {
        if (eofReached || bufferPos < charsInBuffer) {
            return; // 还有数据或已结束
        }
        charsInBuffer = reader.read(buffer);
        if (charsInBuffer == -1) {
            charsInBuffer = 0;
            eofReached = true;
        }
        bufferPos = 0;
    }

    @Override
    public boolean hasNext() {
        try {
            if (bufferPos >= charsInBuffer && eofReached) {
                return false; // 完全结束
            }
            // 只要有数据，就可能构成一个块（即使是硬切分）
            return bufferPos < charsInBuffer;
        } catch (Exception e) {
            throw new RuntimeException("Error in hasNext", e);
        }
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more chunks available");
        }

        try {
            // 1. 确定搜索的起始和结束位置
            int searchStart = bufferPos + targetChunkSize; // 从目标长度处开始向前搜索
            int searchEnd = bufferPos; // 向前搜索到当前位置

            // 2. 确保搜索范围在有效缓冲区内
            int effectiveSearchStart = Math.min(searchStart, charsInBuffer);
            int effectiveSearchEnd = Math.max(searchEnd, 0);

            // 3. 从 searchStart 向前搜索，寻找第一个自然切分点
            int cutPoint = -1;
            for (int i = effectiveSearchStart - 1; i >= effectiveSearchEnd; i--) {
                if (breakPoints.contains(buffer[i])) {
                    cutPoint = i + 1; // 切分点在找到的符号之后
                    break;
                }
            }

            // 4. 如果没找到自然切分点，使用硬切分
            if (cutPoint == -1) {
                cutPoint = Math.min(bufferPos + targetChunkSize, charsInBuffer);
            }

            // 5. 提取块
            String chunk = new String(buffer, bufferPos, cutPoint - bufferPos);
            bufferPos = cutPoint; // 移动指针

            // 6. 尝试填充更多数据（为下一次搜索做准备）
            if (bufferPos >= charsInBuffer && !eofReached) {
                refillBuffer();
            }

            // 去除空格、换行符
            return chunk.trim().replaceAll("\\s+", " ");

        } catch (IOException e) {
            throw new RuntimeException("Error reading chunk", e);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}