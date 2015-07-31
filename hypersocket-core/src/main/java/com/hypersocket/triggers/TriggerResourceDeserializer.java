package com.hypersocket.triggers;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class TriggerResourceDeserializer extends
		JsonDeserializer<TriggerResource> {

	@Override
	public TriggerResource deserialize(JsonParser jp,
			DeserializationContext ctxt) throws IOException,
			JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		return deserializeTrigger(node);
	}

	private TriggerResource deserializeTrigger(JsonNode node) {

		TriggerResource resource = new TriggerResource();
		resource.setName(node.get("name").asText());
		resource.setHidden(node.get("hidden").asBoolean());
		resource.setResourceCategory(node.get("resourceCategory").asText());
		resource.setSystem(node.get("system").asBoolean());

		Map<String, String> properties = new HashMap<String, String>();

		Iterator<Map.Entry<String,JsonNode>> it = node.get("properties").fields();
		while(it.hasNext()) {
			Map.Entry<String,JsonNode> n = it.next();
			properties.put(n.getKey(), n.getValue().asText());
		}


		resource.setProperties(properties);

		resource.setResult(TriggerResultType.valueOf(node.get("result").asText()));
		
		resource.setEvent(node.get("event").asText());
		resource.setResourceKey(node.get("resourceKey").asText());

		Set<TriggerCondition> conditions = new HashSet<TriggerCondition>();
		Iterator<JsonNode> allConditionIterator = node.get("allConditions")
				.iterator();
		while (allConditionIterator.hasNext()) {
			JsonNode condition = allConditionIterator.next();
			TriggerCondition triggerCondition = new TriggerCondition();

			triggerCondition.setType(TriggerConditionType.valueOf(condition
					.get("type").asText()));
			triggerCondition.setAttributeKey(condition.get("attributeKey")
					.asText());
			triggerCondition.setConditionKey(condition.get("conditionKey")
					.asText());
			triggerCondition.setConditionValue(condition.get("conditionValue")
					.asText());
			triggerCondition.setTrigger(resource);

			conditions.add(triggerCondition);
		}

		Iterator<JsonNode> anyConditionIterator = node.get("anyConditions")
				.iterator();
		while (anyConditionIterator.hasNext()) {
			JsonNode condition = anyConditionIterator.next();
			TriggerCondition triggerCondition = new TriggerCondition();

			triggerCondition.setType(TriggerConditionType.valueOf(condition
					.get("type").asText()));
			triggerCondition.setAttributeKey(condition.get("attributeKey")
					.asText());
			triggerCondition.setConditionKey(condition.get("conditionKey")
					.asText());
			triggerCondition.setConditionValue(condition.get("conditionValue")
					.asText());
			triggerCondition.setTrigger(resource);

			conditions.add(triggerCondition);
		}

		resource.conditions = conditions;

		Set<TriggerResource> childTriggers = new HashSet<TriggerResource>();
		Iterator<JsonNode> childTriggerIterator = node.get("childTriggers")
				.iterator();
		while (childTriggerIterator.hasNext()) {
			try {
				JsonNode childTriggerNode = childTriggerIterator.next();
				TriggerResource childTrigger = deserializeTrigger(childTriggerNode);
				childTriggers.add(childTrigger);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		}

		resource.childTriggers = childTriggers;

		return resource;
	}
}
