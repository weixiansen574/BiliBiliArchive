package top.weixiansen574.bilibiliArchive.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.sqlite.SQLiteConfig;
import top.weixiansen574.bilibiliArchive.config.dbvc.MySqlDBVC;
import top.weixiansen574.bilibiliArchive.config.dbvc.master.MasterMySqlDBVC;
import top.weixiansen574.bilibiliArchive.config.dbvc.master.MasterSQLiteDBVC;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@MapperScan(basePackages = "top.weixiansen574.bilibiliArchive.mapper.master", sqlSessionFactoryRef = "masterSqlSessionFactory")
public class MasterDatabaseConfig {

    @Primary
    @Bean(name = "masterDataSource")
    //@ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource(@Value("${spring.datasource.master.jdbc-url}") String jdbcUrl,
                                       @Value("${spring.datasource.master.username:}") String username,
                                       @Value("${spring.datasource.master.password:}") String password,
                                       @Value("${spring.datasource.master.driver-class-name}") String driverClassName) throws SQLException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        if ("org.sqlite.JDBC".equals(driverClassName)){
            //sqlite数据库默认外键约束不启用，需要主动启用
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            dataSource.setDataSourceProperties(config.toProperties());
            new MasterSQLiteDBVC(dataSource.getConnection()).doInit();
        } else {
            new MasterMySqlDBVC(dataSource.getConnection()).doInit();
        }
        //return DataSourceBuilder.create().build();
        return dataSource;
    }

    @Primary
    @Bean(name = "masterSqlSessionFactory")
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("masterDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:/mybatis-config.xml"));
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/master/*.xml"));

        return factoryBean.getObject();
    }

    @Bean("masterTransactionManager")
    @Primary
    public DataSourceTransactionManager masterTransactionManager(@Qualifier("masterDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = "masterSqlSessionTemplate")
    public SqlSessionTemplate masterSqlSessionTemplate(@Qualifier("masterSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
