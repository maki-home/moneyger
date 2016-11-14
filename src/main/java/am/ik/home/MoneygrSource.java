package am.ik.home;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MoneygrSource {
	String INCOME_OUTPUT = "income-output";
	String OUTCOME_OUTPUT = "outcome-output";

	@Output(INCOME_OUTPUT)
	MessageChannel incomeOutput();

	@Output(OUTCOME_OUTPUT)
	MessageChannel outcomeOutput();
}
