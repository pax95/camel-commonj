Camel component that delegates Camel thread management to CommonJ workmanagers.

Only thread creation that is controlled by Camel will be handled by this component. 
Libraries that spawn their own threads, will not be wrapped eg. worker threads in the Quarz component.

The Timer and Work Manager API is defined in a specification created jointly by Oracle and IBM. 
This API enables concurrent programming of EJBs and Servlets within a Java EE application. 
This API is often referred to as CommonJ.

See http://download.oracle.com/docs/cd/E12840_01/wls/docs103/commonj/commonj.html

To activate thread management using JEE container workmanagers do :  
Spring XML: 

<bean id="workmanager" class="org.springframework.jndi.JndiObjectFactoryBean">
	<!-- note that the JNDI location of the workmangager to use is server and configuration specific-->
	<property name="jndiName" value="wm/default" />
	<property name="resourceRef" value="true" />
</bean>
  
<bean id="workmanagerThreadPoolFactoryBean" class="org.apache.camel.component.commonj.WorkManagerThreadPoolFactory">
	<property name="workmanager" ref="workmanager"/>
</bean>