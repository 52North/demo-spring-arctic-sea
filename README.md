# demo-spring-arctic-sea

Demo to show how to use Spring and arctic-sea to query the SOS

Run the ```org.n52.demo.DemoApplication``` in the ```/app``` module


## Important configs

[DemoApplication.java](https://github.com/52North/demo-spring-arctic-sea/blob/main/app/src/main/java/org/n52/demo/DemoApplication.java):
  
  * ```@ImportResource({"classpath*:/spring/arctic-sea.xml"})``` : some beans from arctic-sea
  * ```ctx.getBean(LifecycleBeanPostProcessor.class);``` : The processsor that load the decoder/encoder into the appropriate repositories

[spring/arctic-sea.xml](https://github.com/52North/demo-spring-arctic-sea/blob/main/app/src/main/resources/spring/arctic-sea.xml):

 * Defines some beans, e.g. the decoder and encoder repositories

[application.yml](https://github.com/52North/demo-spring-arctic-sea/blob/main/app/src/main/resources/application.yml):

  * define the host, port and path of the SOS
