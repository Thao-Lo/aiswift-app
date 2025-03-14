package com.aiswift.Config;



import java.util.HashMap;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.aiswift.MultiTenancy.DataSourceUtil;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.aiswift.Global.Repository",
    entityManagerFactoryRef = "globalEntityManagerFactory",
    transactionManagerRef = "globalTransactionManager"
)
public class DataSourceConfig {

    
    @Autowired
    private DataSourceUtil dataSourceUtil;

    @Bean(name = "globalDataSource")
    @Primary
    public DataSource globalDataSource() {
    	System.out.println("this is global datasource");
        return dataSourceUtil.createDataSource("global_multi_tenant");
    }
    
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(),new HashMap<>(), null
        );
    }

    @Bean(name = "globalEntityManagerFactory")
//    @DependsOn("flyway") // Flyway must run before Hibernate
    public LocalContainerEntityManagerFactoryBean globalEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("globalDataSource") DataSource dataSource) {
    	System.out.println(" I am in global entity manager factory");
        return builder
                .dataSource(dataSource)
                .packages("com.aiswift.Global.Entity")
                .persistenceUnit("globalPU")
                .build();
    }

    @Bean(name = "globalTransactionManager")
    public PlatformTransactionManager globalTransactionManager(
            @Qualifier("globalEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    	System.out.println(" I am in global transaction manager");

        return new JpaTransactionManager(entityManagerFactory);
    }

    @PostConstruct
    public void runFlywayMigration() {
    	System.out.println("I am in run flyway migration");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSourceUtil.createDataSource("global_multi_tenant"))
                .locations("classpath:db/migration/global")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }
}
