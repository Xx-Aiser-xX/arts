package org.example.arts.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageImplDeserializer extends JsonDeserializer<PageImpl<?>> {

    @Override
    public PageImpl<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        JavaType pageType = ctxt.getContextualType();
        JavaType contentType = null;

        if (pageType != null && pageType.getRawClass().equals(PageImpl.class) && pageType.containedTypeCount() > 0) {
            contentType = pageType.containedType(0);
        }
        if (contentType == null) {
            contentType = ctxt.getTypeFactory().constructType(Object.class);
        }
        ArrayNode contentNode = (ArrayNode) node.get("content");
        List<Object> content = new ArrayList<>();
        if (contentNode != null && contentNode.isArray()) {
            for (JsonNode element : contentNode) {
                content.add(mapper.treeToValue(element, contentType));
            }
        }
        JsonNode pageableNode = node.get("pageable");
        Pageable pageable;
        if (pageableNode != null) {
            int pageNumber = pageableNode.get("pageNumber").asInt();
            int pageSize = pageableNode.get("pageSize").asInt();
            pageable = PageRequest.of(pageNumber, pageSize);
        } else {
            pageable = Pageable.unpaged();
        }
        long totalElements = node.get("totalElements").asLong();

        return new PageImpl<>(content, pageable, totalElements);
    }
}