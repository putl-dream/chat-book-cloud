package fun.amireux.chat.book.langchain.tools.weather;

import dev.langchain4j.agent.tool.Tool;
import fun.amireux.chat.book.langchain.tools.AiTool;

@AiTool
public class WeatherTool {

    @Tool("获取天气信息")
    public String getWeather() {
        return "今天昆明的天气多云";
    }
}
