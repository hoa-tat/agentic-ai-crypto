package com.hoatat.service;

import com.hoatat.dto.MemoryMessage;
import com.hoatat.memory.AgentMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemoryService {
    private final List<MemoryMessage> shortTermMemory = new ArrayList<>();

    private final VectorStore vectorStore;

    public MemoryService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void add(String role, String content) {
        shortTermMemory.add(
                new MemoryMessage(role, content)
        );

        // tránh memory quá dài
        if (shortTermMemory.size() > 20) {
            shortTermMemory.removeFirst();
        }
    }

    public List<MemoryMessage> getMemory() {
        return shortTermMemory;
    }

    public String asText() {
        return shortTermMemory.stream()
                .map(MemoryMessage::toString)
                .collect(Collectors.joining("\n"));
    }

    public void saveMemory(AgentMemory memory) {
        log.info("=== SAVE MEMORY ===");
        log.info(memory.toText());

        Document document = new Document(memory.toText());

        vectorStore.add(List.of(document));
        log.info("=== SAVED ===");
    }

    public List<Document> search(String query) {

        return vectorStore.similaritySearch(query);
    }
}
