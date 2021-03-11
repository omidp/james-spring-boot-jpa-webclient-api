package org.apache.james.webapi.app;

import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;

/**
 * @author omidp
 *
 */
@Configuration
public class DBConfiguration {

	@Bean
	public DataSource dataSource(WebApiConfiguration conf) {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(conf.getDriverClassName());
		ds.setUrl(conf.getUrl());
		ds.setUsername(conf.getUser());
		ds.setPassword(conf.getPwd());
		ds.setValidationQuery("select 1");
		return ds;
	}

    @Bean
    JdbcTemplate jdbcTemplate(DataSource ds)
    {
        return new JdbcTemplate(ds);
    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory)
//    {
////        return new DataSourceTransactionManager(dataSource());
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		OpenJpaVendorAdapter v = vendor();

		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setJpaVendorAdapter(v);

		HashMap<String, String> properties = new HashMap<>();
		properties.put("openjpa.jdbc.QuerySQLCache", "false");
		properties.put("openjpa.streaming", "false");
		properties.put("openjpa.Log", "DefaultLevel=WARN, Runtime=INFO, Tool=INFO, SQL=TRACE");

		factoryBean.setJpaPropertyMap(properties);
		factoryBean.setPersistenceXmlLocation("classpath:META-INF/custom-persistence.xml");

		return factoryBean;
	}

	@Bean
	OpenJpaVendorAdapter vendor() {
		OpenJpaVendorAdapter v = new OpenJpaVendorAdapter();
		v.setDatabase(Database.POSTGRESQL);
		v.setShowSql(true);
		return v;
	}

}
