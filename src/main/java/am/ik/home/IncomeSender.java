package am.ik.home;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface IncomeSender {
	@Gateway(requestChannel = MoneygrSource.INCOME_OUTPUT)
	void send(Income income);
}
