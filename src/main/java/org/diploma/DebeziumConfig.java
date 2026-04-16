//package org.diploma;
//
//import io.debezium.config.Configuration;
//import io.debezium.embedded.Connect;
//import io.debezium.engine.DebeziumEngine;
//import io.debezium.engine.RecordChangeEvent;
//import io.debezium.engine.format.ChangeEventFormat;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.connect.source.SourceRecord;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@org.springframework.context.annotation.Configuration
//@Slf4j
//public class DebeziumConfig {
//
//    @Value("${debezium.database.hostname}")
//    private String dbHost;
//
//    @Value("${debezium.database.port}")
//    private String dbPort;
//
//    @Value("${debezium.database.user}")
//    private String dbUser;
//
//    @Value("${debezium.database.password}")
//    private String dbPassword;
//
//    @Value("${debezium.database.dbname}")
//    private String dbName;
//
//    @Value("${debezium.database.schema}")
//    private String dbSchema;
//
//    @Value("${debezium.database.server-name}")
//    private String serverName;
//
//    @Value("${debezium.database.server-id}")
//    private String serverId;
//
//    @Value("${debezium.database.plugin-name}")
//    private String pluginName;
//
//    @Value("${debezium.database.include-tables}")
//    private String includeTables;
//
//    @Value("${debezium.database.offset-storage-file}")
//    private String offsetStorageFile;
//
//    @Value("${debezium.database.history-file}")
//    private String historyFile;
//
//    @Bean
//    public Configuration debeziumConfiguration() {
//        return Configuration.create()
//                // Connector configuration
//                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
//                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
//                .with("offset.storage.file.filename", offsetStorageFile)
//                .with("offset.flush.interval.ms", "60000")
//
//                // Database configuration
//                .with("database.hostname", dbHost)
//                .with("database.port", dbPort)
//                .with("database.user", dbUser)
//                .with("database.password", dbPassword)
//                .with("database.dbname", dbName)
//                .with("database.server.name", serverName)
//                .with("database.server.id", serverId)
//                .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
//                .with("database.history.file.filename", historyFile)
//
//                // PostgreSQL specific
//                .with("plugin.name", pluginName)
//                .with("schema.include.list", dbSchema)
//                .with("table.include.list", includeTables)
//
//                // Change data capture configuration
//                .with("include.schema.changes", "false")
//                .with("tombstones.on.delete", "false")
//                .with("decimal.handling.mode", "string")
//
//                // Snapshot configuration
//                .with("snapshot.mode", "initial")
//                .with("snapshot.locking.mode", "none")
//
//                .build();
//    }
//
//    @Bean
//    public ExecutorService executorService() {
//        return Executors.newSingleThreadExecutor();
//    }
//
//    @Bean
//    public DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine(
//            Configuration configuration,
//            ChangeEventHandler changeEventHandler) {
//
//        DebeziumEngine<RecordChangeEvent<SourceRecord>> engine =
//                DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
//                        .using(configuration.asProperties())
//                        .notifying(changeEventHandler::handleChangeEvent)
//                        .using((success, message, error) -> {
//                            if (!success && error != null) {
//                                log.error("Debezium engine error: {}", message, error);
//                            }
//                        })
//                        .build();
//
//        // Start engine in separate thread
//        executorService().submit(() -> {
//            try {
//                engine.run();
//            } catch (Exception e) {
//                log.error("Debezium engine stopped due to error", e);
//            }
//        });
//
//        return engine;
//    }
//}
