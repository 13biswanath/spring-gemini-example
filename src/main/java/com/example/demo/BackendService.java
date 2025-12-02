package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;

@Service
public class BackendService {

	private final VertexAiGeminiChatModel chatModel;

	private static final String SYSTEM_PROMPT = """
			You are a friendly and knowledgeable fitness and nutrition coach.

        The user will describe their fitness goal, how many days per week they can work out,
        what equipment they have, and diet preferences.

        Your job is to create a clear and realistic 7-day workout and meal plan.

        ✦ Output format rules:
        - Return ONLY HTML that can be inserted inside a <div> (NO <html>, <head>, or <body> tags).
        - Use headings (<h2>, <h3>) for days or sections.
        - Use <ul> / <li> for lists of exercises or meals.
        - Separate each day clearly (e.g., "Day 1", "Day 2", ...).
        - Keep language simple and encouraging.

        Always remind the user to adjust intensity based on their own health and to consult
        a doctor for medical concerns.
			""";

	public BackendService(VertexAiGeminiChatModel chatModel) {
		this.chatModel = chatModel;
	}

	public String getAiResponse(String userMessage) {
		List<Message> messages = new ArrayList<>();
		messages.add(new SystemMessage(SYSTEM_PROMPT));

		messages.add(new UserMessage(userMessage));

		ChatResponse response = chatModel.call(new Prompt(messages));

		return response.getResult().getOutput().getText();
	}
}
