package com.putl.userservice.util;

import com.fasterxml.jackson.core.JsonGenerator;
   import com.fasterxml.jackson.databind.JsonSerializer;
   import com.fasterxml.jackson.databind.SerializerProvider;

   import java.io.IOException;
   import java.time.LocalDateTime;
   import java.time.format.DateTimeFormatter;
   import java.time.LocalDate;
   import java.time.ZoneId;

   public class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

       private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
       private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
       private static final DateTimeFormatter DAY_OF_WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEE");

       @Override
       public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
           LocalDate today = LocalDate.now(ZoneId.systemDefault());
           LocalDate valueDate = value.toLocalDate();

           if (valueDate.isEqual(today)) {
               // 如果是今天，只返回时间
               gen.writeString(value.format(TIME_FORMATTER));
           } else if (valueDate.isEqual(today.minusDays(1))) {
               // 如果是昨天，返回昨天
               gen.writeString("昨天");
           } else {
               // 其他就返回对应周几
               gen.writeString(valueDate.format(DAY_OF_WEEK_FORMATTER));
           }
       }
   }
   