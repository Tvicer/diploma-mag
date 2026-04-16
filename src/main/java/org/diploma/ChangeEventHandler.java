//package org.diploma;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.debezium.data.Envelope;
//import io.debezium.engine.RecordChangeEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.connect.data.Struct;
//import org.apache.kafka.connect.source.SourceRecord;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//@Slf4j
//public class ChangeEventHandler {
//
//    @Autowired
//    private ChangeDataService changeDataService;
//
//    public void handleChangeEvent(RecordChangeEvent<SourceRecord> event) {
//        SourceRecord record = event.record();
//
//        try {
//            // Получаем значение записи
//            Struct value = (Struct) record.value();
//
//            if (value == null) {
//                return;
//            }
//
//            // Определяем операцию
//            Envelope.Operation operation = Envelope.Operation.forCode(
//                    value.getString("op")
//            );
//
//            // Получаем информацию о таблице
//            Struct source = value.getStruct("source");
//            String table = source.getString("table");
//            String schema = source.getString("schema");
//
//            // Получаем данные до и после изменения
//            Struct before = value.getStruct("before");
//            Struct after = value.getStruct("after");
//
//            // Преобразуем Struct в Map
//            Map<String, Object> beforeData = convertStructToMap(before);
//            Map<String, Object> afterData = convertStructToMap(after);
//
//            // Получаем дополнительные метаданные
//            Long tsMs = value.getInt64("ts_ms");
//            String transactionId = source.getString("txId");
//            Long lsn = source.getInt64("lsn");
//
//            log.info("Change event detected - Operation: {}, Table: {}.{}, Transaction: {}, LSN: {}",
//                    operation, schema, table, transactionId, lsn);
//
//            // Обрабатываем событие в зависимости от типа операции
//            switch (operation) {
//                case CREATE:
//                    handleCreate(table, afterData, source, tsMs);
//                    break;
//                case UPDATE:
//                    handleUpdate(table, beforeData, afterData, source, tsMs);
//                    break;
//                case DELETE:
//                    handleDelete(table, beforeData, source, tsMs);
//                    break;
//                default:
//                    log.info("Unknown operation: {}", operation);
//            }
//
//        } catch (Exception e) {
//            log.error("Error processing change event", e);
//        }
//    }
//
//    private void handleCreate(String table, Map<String, Object> data, Struct source, Long timestamp) {
//        log.info("INSERT into table: {}", table);
//        log.info("Data at {}: {}", timestamp, data);
//
//        // Маршрутизация по типу таблицы
//        switch (table) {
//            case "users":
//                changeDataService.processUserChange("c", data);
//                break;
//            case "orders":
//                changeDataService.processOrderChange("c", data);
//                break;
//            default:
//                log.info("Unhandled table for INSERT: {}", table);
//        }
//    }
//
//    private void handleUpdate(String table, Map<String, Object> before,
//                              Map<String, Object> after, Struct source, Long timestamp) {
//        log.info("UPDATE on table: {}", table);
//        log.info("Before: {}", before);
//        log.info("After: {}", after);
//
//        // Маршрутизация по типу таблицы
//        switch (table) {
//            case "users":
//                changeDataService.processUserChange("u", after);
//                break;
//            case "orders":
//                changeDataService.processOrderChange("u", after);
//                break;
//            default:
//                log.info("Unhandled table for UPDATE: {}", table);
//        }
//
//        // Логируем изменения полей для аудита
//        logFieldChanges(before, after);
//    }
//
//    private void handleDelete(String table, Map<String, Object> data, Struct source, Long timestamp) {
//        log.info("DELETE from table: {}", table);
//        log.info("Deleted data at {}: {}", timestamp, data);
//
//        // Маршрутизация по типу таблицы
//        switch (table) {
//            case "users":
//                changeDataService.processUserChange("d", data);
//                break;
//            case "orders":
//                changeDataService.processOrderChange("d", data);
//                break;
//            default:
//                log.info("Unhandled table for DELETE: {}", table);
//        }
//    }
//
//    /**
//     * Преобразует Struct из Kafka Connect в Map<String, Object>
//     */
//    private Map<String, Object> convertStructToMap(Struct struct) {
//        if (struct == null) {
//            return null;
//        }
//
//        Map<String, Object> result = new HashMap<>();
//
//        // Получаем схему и все поля
//        struct.schema().fields().forEach(field -> {
//            String fieldName = field.name();
//            Object fieldValue = struct.get(fieldName);
//
//            // Рекурсивно обрабатываем вложенные структуры
//            if (fieldValue instanceof Struct) {
//                result.put(fieldName, convertStructToMap((Struct) fieldValue));
//            }
//            // Обрабатываем массивы
//            else if (fieldValue instanceof java.util.List) {
//                result.put(fieldName, convertList((java.util.List<?>) fieldValue));
//            }
//            // Обрабатываем обычные значения
//            else {
//                result.put(fieldName, fieldValue);
//            }
//        });
//
//        return result;
//    }
//
//    /**
//     * Преобразует List, рекурсивно обрабатывая вложенные структуры
//     */
//    private java.util.List<Object> convertList(java.util.List<?> list) {
//        if (list == null) {
//            return null;
//        }
//
//        java.util.List<Object> result = new java.util.ArrayList<>();
//
//        for (Object item : list) {
//            if (item instanceof Struct) {
//                result.add(convertStructToMap((Struct) item));
//            } else if (item instanceof java.util.List) {
//                result.add(convertList((java.util.List<?>) item));
//            } else {
//                result.add(item);
//            }
//        }
//
//        return result;
//    }
//
//    /**
//     * Логирует изменения полей (для аудита)
//     */
//    private void logFieldChanges(Map<String, Object> before, Map<String, Object> after) {
//        if (before == null || after == null) {
//            return;
//        }
//
//        StringBuilder changes = new StringBuilder("Field changes: ");
//        boolean hasChanges = false;
//
//        for (Map.Entry<String, Object> entry : after.entrySet()) {
//            String field = entry.getKey();
//            Object oldValue = before.get(field);
//            Object newValue = entry.getValue();
//
//            if (oldValue == null && newValue == null) {
//                continue;
//            }
//
//            if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
//                if (hasChanges) {
//                    changes.append(", ");
//                }
//                changes.append(field).append(": ").append(oldValue).append(" -> ").append(newValue);
//                hasChanges = true;
//            }
//        }
//
//        if (hasChanges) {
//            log.info(changes.toString());
//        }
//    }
//}