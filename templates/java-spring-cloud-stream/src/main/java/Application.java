{# vim: set ts=4 sw=4 sts=4 noexpandtab : #}
{%- include '.partials/java-package' -%}

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Map;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}
{% for channelName, channel in asyncapi.channels() -%}
{%- set name = [channelName, channel] | functionName -%}
{%- set upperName = name | upperFirst -%}
{%- set payloadClass = [channelName, channel] | payloadClass -%}
{%- set lowerPayloadName = payloadClass | lowerFirst -%}
{%- set topicInfo = [channelName, channel] | topicInfo %}
	// channel: {{ channelName }}
{% for param in topicInfo.params -%}
{%- if param.enum %}
    public static enum {{ param.type }} { {{ param.enum }} }
{% endif -%}
{%- endfor -%}
{%- if channel.hasPublish() %}
	// publisher
{%- set emitterName = name + "EmitterProcessor" %}
	EmitterProcessor<Message<{{payloadClass}}>> {{emitterName}} = EmitterProcessor.create();

	@Bean
	public Supplier<Flux<Message<{{payloadClass}}>>> {{name}}Supplier() {
		return () -> {{emitterName}};
	}
{% endif -%}
{%- if channel.hasSubscribe() %}
	// subscriber
	@Bean
	Consumer<Message<{{payloadClass}}>> {{name}}Consumer() {
		return message -> { };
	}
{% endif %}
{%- endfor %}
}
