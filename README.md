Camel component that deletages thread management to CommonJ workmanagers.

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
				<property name="jndiName" value="java:comp/myJndiWorkmanager" />
				<property name="resourceRef" value="true" />
			</bean>
        </property>
    </bean>

    <camel:camelContext id="camel">
        <camel:route>
            <camel:from uri="timer://TestTimer?fixedRate=true&amp;period=60000&amp;delay=30000"/>
            <camel:to uri="mock:timer"/>
            <camel:to uri="mock:foo"/>
        </camel:route>
     
    </camel:camelContext>




