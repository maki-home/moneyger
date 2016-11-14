package am.ik.home;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface OutcomeSender {
	@Gateway(requestChannel = MoneygrSource.OUTCOME_OUTPUT)
	void send(Outcome outcome);
}
