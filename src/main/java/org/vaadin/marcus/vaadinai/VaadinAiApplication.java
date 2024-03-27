package org.vaadin.marcus.vaadinai;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CompletableFuture;

// compile with ./mvnw -DskipTests -Pnative -Pproduction native:compile
@SpringBootApplication
public class VaadinAiApplication implements AppShellConfigurator {

	@Route("")
	static class AiChat extends VerticalLayout {
		private VerticalLayout subLayout; // 用于动态显示问答

		public AiChat(ChatClient chatClient) {
			subLayout = new VerticalLayout(); // 初始化布局
			add(subLayout); // 将子布局添加到视图中

			// 创建消息输入组件
			MessageInput messageInput = new MessageInput();
			messageInput.setTooltipText("Type your question here...");
			messageInput.addSubmitListener(e -> {
				String userText = e.getValue();
				subLayout.add(new Paragraph("You: " + userText));

				// 发起到AI服务的请求，并处理响应
				getAIResponse(chatClient, userText);
			});

			setSizeFull(); // 设置布局占满整个页面
			setPadding(false); // 如果需要的话，您可以关闭内边距
			subLayout.getStyle().set("flex-grow", "9"); // 占据90%
			messageInput.getStyle().set("flex-grow", "1"); // 在这里，MessageInput 不会伸展填充额外空间
			messageInput.setHeight("5px");  // 指定 MessageInput 的高度，如果需要固定高度

			add(messageInput); // 将输入框添加到视图中

			setAlignItems(Alignment.STRETCH);
		}

		private void getAIResponse(ChatClient chatClient, String question) {
			CompletableFuture.supplyAsync(() -> chatClient.call(question))
					.thenAccept(aiResponse -> {
						System.out.println("question: " + question + "   aiResponse: " + aiResponse);
						getUI().ifPresent(ui -> ui.access(() -> this.subLayout.add(new Paragraph("AI: " + aiResponse))));
					})
					.join();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(VaadinAiApplication.class, args);
	}
}
