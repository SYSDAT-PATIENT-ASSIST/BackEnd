package dk.patientassist.persistence;

import java.util.Properties;

import dk.patientassist.config.Mode;
import dk.patientassist.persistence.ent.Bed;
import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.persistence.ent.Section;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.utilities.Utils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManagerFactory;

/**
 * Patient Assist
 */
public class HibernateConfig {
    private static final Logger logger = LoggerFactory.getLogger(HibernateConfig.class);
    private static EntityManagerFactory emf;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null)
            throw new RuntimeException("No EntityManagerFactory Instance");
        return emf;
    }

    private static void getAnnotationConfiguration(Configuration configuration) {
        configuration.addAnnotatedClass(Employee.class);
        configuration.addAnnotatedClass(Section.class);
        configuration.addAnnotatedClass(Role.class);
        configuration.addAnnotatedClass(Bed.class);
    }

    public static void init(Mode mode) {
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();
            setBaseProperties(props);

            if (System.getenv("DEPLOYED") != null)
                setDeployedProperties(props);
            else if (mode == Mode.DEV)
                setDevProperties(props);
            else if (mode == Mode.TEST)
                setTestProperties(props);

            configuration.setProperties(props);
            getAnnotationConfiguration(configuration);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
            emf = sf.unwrap(EntityManagerFactory.class);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void setBaseProperties(Properties props) {
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.current_session_context_class", "thread");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.use_sql_comments", "false");
    }

    private static void setDevProperties(Properties props) {
        props.put("hibernate.hikari_leakDetectionThreshold", "10000"); // leak detection
        props.setProperty("hibernate.connection.url",
                Utils.getConfigProperty("DB_CONN_STR") + Utils.getConfigProperty("DB_NAME"));
        props.setProperty("hibernate.connection.username", Utils.getConfigProperty("DB_USER"));
        props.setProperty("hibernate.connection.password", Utils.getConfigProperty("DB_PW"));
    }

    private static void setDeployedProperties(Properties props) {
        props.setProperty("hibernate.connection.url", System.getenv("DB_CONN_STR") + System.getenv("DB_NAME_GARDEN"));
        props.setProperty("hibernate.connection.username", System.getenv("DB_USER"));
        props.setProperty("hibernate.connection.password", System.getenv("DB_PW"));
    }

    private static void setTestProperties(Properties props) {
        props.put("hibernate.connection.driver_class", "org.testcontainers.jdbc.ContainerDatabaseDriver");
        props.put("hibernate.connection.url", "jdbc:tc:postgresql:15.3-alpine3.18:///test_db");
        props.put("hibernate.connection.username", "postgres");
        props.put("hibernate.connection.password", "postgres");
        props.put("hibernate.archive.autodetect", "class");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
    }

    @SuppressWarnings("unused")
    private void logProps() {
        logger.info("DB_CONN_STR: {}, DB_NAME: {}, DB_USER: {}, DB_PW: {}%n", Utils.getConfigProperty("DB_CONN_STR"),
                Utils.getConfigProperty("DB_NAME"), Utils.getConfigProperty("DB_USER"),
                Utils.getConfigProperty("DB_PW"));
    }
}
