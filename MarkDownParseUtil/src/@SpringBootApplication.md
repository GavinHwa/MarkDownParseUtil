@SpringBootApplication

等价于
@Configuration
@EnableAutoConfiguration
@ComponentScan

exclude排除指定的类，一般用于排除自定义配置
@SpringBootApplication(exclude=RedisAutoConfiguration.class)

excludeName
排除指定的类名
@SpringBootApplication(excludeName=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration)

scanBasePackages
扫描指定的包到Spring容器中，参数为数组
扫描之后的包注册，没扫描的不会注册
@SpringBootApplication(scanBasePackages="com.blabla.boot.component")

scanBasePackageClasses
扫描特定的类,同级下的类如果是组件也同样会扫描
@SpringBootApplication(scanBasePackageClasses="MyComponent.class")

@SpringBootConfiguration
继承（等效）于@Configuration最终继承于@Component 
在该类中配置bean之后不需要再注册组件
该类也同样是一个Bean

@EnableAutoConfiguration
包含exclude和excludeName方法
帮助SpringBoot应用将所有符合条件的@Configuration配置都加载到当前SpringBoot创建并使用的IoC容器。