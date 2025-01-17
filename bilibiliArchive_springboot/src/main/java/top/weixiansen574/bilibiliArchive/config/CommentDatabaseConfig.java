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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.sqlite.SQLiteConfig;
import top.weixiansen574.bilibiliArchive.config.dbvc.comment.CommentMySqlDBVC;
import top.weixiansen574.bilibiliArchive.config.dbvc.comment.CommentSQLiteDBVC;
import top.weixiansen574.bilibiliArchive.config.dbvc.master.MasterMySqlDBVC;


import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@MapperScan(basePackages = "top.weixiansen574.bilibiliArchive.mapper.comment",sqlSessionFactoryRef = "commentSqlSessionFactory")
public class CommentDatabaseConfig {
    @Bean(name = "commentDataSource")
    public DataSource commentDataSource(@Value("${spring.datasource.comment.jdbc-url}") String jdbcUrl,
                                        @Value("${spring.datasource.comment.username:}") String username,
                                        @Value("${spring.datasource.comment.password:}") String password,
                                        @Value("${spring.datasource.comment.maximum-pool-size:#{null}}") Integer maximumPoolSize,
                                       @Value("${spring.datasource.comment.driver-class-name}") String driverClassName) throws Exception {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        if (maximumPoolSize != null){
            dataSource.setMaximumPoolSize(maximumPoolSize);
        }
        if ("org.sqlite.JDBC".equals(driverClassName)){
            //sqlite数据库默认外键约束不启用，需要主动启用
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            dataSource.setDataSourceProperties(config.toProperties());
            //sqlite只能1，否则多线程会报SQLITE_BUSY，database is locked异常
            dataSource.setMaximumPoolSize(1);
            new CommentSQLiteDBVC(dataSource.getConnection()).doInit();
        } else {
            new CommentMySqlDBVC(dataSource.getConnection()).doInit();
        }
        return dataSource;
    }

    @Bean(name = "commentSqlSessionFactory")
    public SqlSessionFactory commentSqlSessionFactory(@Qualifier("commentDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:/mybatis-config.xml"));
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/comment/*.xml"));

        return factoryBean.getObject();
    }

    @Bean("commentTransactionManager")
    public DataSourceTransactionManager commentTransactionManager(@Qualifier("commentDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "commentSqlSessionTemplate")
    public SqlSessionTemplate commentSqlSessionTemplate(@Qualifier("commentSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
