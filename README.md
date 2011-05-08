Camel component that delegates Camel thread management to CommonJ workmanagers.

Only thread creation that is controlled by Camel will be handled by this component. 
Libraries that spawn their own threads, will not be wrapped by the Camel workmanager wrapper
eg. worker threads in the Quarz component.

The Timer and Work Manager API is defined in a specification created jointly by Oracle and IBM. 
This API enables concurrent programming of EJBs and Servlets within a Java EE application. 
This API is often referred to as CommonJ.

See http://download.oracle.com/docs/cd/E12840_01/wls/docs103/commonj/commonj.html

To activate thread management using JEE container workmanagers do :  
Spring XML:

	<bean id="workmanagerExecutor" depends-on="camel" class="org.apache.camel.component.commonj.WorkManagerExecutorServiceStrategy">
        <constructor-arg index="0" ref="camel"/>
        <property name="workmanager">
        	<bean class="org.springframework.jndi.JndiObjectFactoryBean">
				<property name="jndiName" value="java:comp/env/myJndiWorkmanager" />
				<property name="resourceRef" value="true" />
			</bean>
        </property>
    </bean>

 	<camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
        <route>
            <from uri="direct:foo"/>
            <to uri="mock:foo"/>
        </route>
    </camelContext>     




