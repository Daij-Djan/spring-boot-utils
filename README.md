# spring-boot-utils
Some Components (Services / Individual Helper classes)

1. Camunda/CamundaUserConfiguration<br/>
	=> a component that hooks into the 'boot' process to add a user/pass with admin rights if needed. Useful for any form of embedded running of the bpm engine.<br/>
	I included it here but it recently was also made part of the official Camunda starter package -- so only use this if you use an old version of the starter ... < ~1.4

2. i18n/NamedArgsMessageSourceService <br/>
	=> a service that supersedes Springs MessageSource by supporting named arguments (placeholders) in the form of ${arg}.
	
3. Luis/LuisRecognizerService<br/>
	=> a service that contacts the LUIS api for you. It is configured via the application.properties / yaml.<br/>
	I included a Properties parse, the JSON Model I got from the microsoft android sdk and a sample configuration
	
4. ExchangeProvider
	=> a component, a configratiob bean, an annotation and a spring-aop aspect that work together to provide POOLED instances of ExchangeServer [from Microsoft EWS api] to a method.<br/>
	To use you need is to have a method annotated with @NeedsExchange AND as last parameter make it take an ExchangeService instance.<br/>
	DONT CATCH Exceptions. The aspect will handle ServiceRequestExceptions for you and automatically refresh the server connection upon a timeout!
	
	[I added a small demo for this]  
	
*package names do not fit*