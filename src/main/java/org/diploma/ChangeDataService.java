//package org.diploma;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//@Slf4j
//public class ChangeDataService {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public void processUserChange(String operation, Map<String, Object> data) {
//        if (data == null) {
//            log.warn("Received null data for user operation: {}", operation);
//            return;
//        }
//
//        try {
//            User user = objectMapper.convertValue(data, User.class);
//
//            switch (operation) {
//                case "c":
//                    log.info("New user created: {} (ID: {})", user.getUsername(), user.getId());
//                    sendWelcomeEmail(user);
//                    break;
//                case "u":
//                    log.info("User updated: {} (ID: {})", user.getUsername(), user.getId());
//                    updateUserCache(user);
//                    break;
//                case "d":
//                    log.info("User deleted: {} (ID: {})", user.getUsername(), user.getId());
//                    cleanupUserData(user);
//                    break;
//                default:
//                    log.warn("Unknown operation for user: {}", operation);
//            }
//        } catch (Exception e) {
//            log.error("Error processing user change. Operation: {}, Data: {}", operation, data, e);
//        }
//    }
//
//    public void processOrderChange(String operation, Map<String, Object> data) {
//        if (data == null) {
//            log.warn("Received null data for order operation: {}", operation);
//            return;
//        }
//
//        try {
//            Order order = objectMapper.convertValue(data, Order.class);
//
//            switch (operation) {
//                case "c":
//                    log.info("New order created: {} (Amount: {})",
//                            order.getOrderNumber(), order.getAmount());
//                    updateOrderStatistics(order);
//                    break;
//                case "u":
//                    log.info("Order updated: {} - status: {}",
//                            order.getOrderNumber(), order.getStatus());
//                    notifyOrderStatusChange(order);
//                    break;
//                case "d":
//                    log.info("Order deleted: {}", order.getOrderNumber());
//                    cancelOrderInExternalSystems(order);
//                    break;
//                default:
//                    log.warn("Unknown operation for order: {}", operation);
//            }
//        } catch (Exception e) {
//            log.error("Error processing order change. Operation: {}, Data: {}", operation, data, e);
//        }
//    }
//
//    private void sendWelcomeEmail(User user) {
//        // Имитация отправки email
//        log.info("📧 Sending welcome email to: {} (User: {})", user.getEmail(), user.getUsername());
//    }
//
//    private void updateUserCache(User user) {
//        // Имитация обновления кэша
//        log.info("🔄 Updating cache for user: {}", user.getId());
//    }
//
//    private void cleanupUserData(User user) {
//        // Имитация очистки данных
//        log.info("🧹 Cleaning up data for user: {}", user.getId());
//    }
//
//    private void updateOrderStatistics(Order order) {
//        // Имитация обновления статистики
//        log.info("📊 Updating order statistics for order: {} (Amount: {})",
//                order.getOrderNumber(), order.getAmount());
//    }
//
//    private void notifyOrderStatusChange(Order order) {
//        // Имитация отправки уведомления
//        log.info("🔔 Notifying about order status change: {} -> {}",
//                order.getOrderNumber(), order.getStatus());
//    }
//
//    private void cancelOrderInExternalSystems(Order order) {
//        // Имитация отмены во внешних системах
//        log.info("❌ Cancelling order in external systems: {}", order.getOrderNumber());
//    }
//}
