package com.aiswift.Config;

import java.util.HashMap;

import javax.sql.DataSource;

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
        return dataSourceUtil.createDataSource("global_multi_tenant");
    }
    
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(),new HashMap<>(), null
        );
    }

    @Bean(name = "globalEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean globalEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("globalDataSource") DataSource dataSource) {    	
        return builder
                .dataSource(dataSource)
                .packages("com.aiswift.Global.Entity")
                .persistenceUnit("globalPU")
                .build();
    }

    @Bean(name = "globalTransactionManager")
    public PlatformTransactionManager globalTransactionManager(
            @Qualifier("globalEntityManagerFactory") EntityManagerFactory entityManagerFactory) { 
        return new JpaTransactionManager(entityManagerFactory);
    }
}
